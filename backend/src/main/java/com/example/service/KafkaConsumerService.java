package com.example.service;

import com.example.dto.DebeziumMessage;
import com.example.dto.DebeziumPayload;
import com.example.dto.ProductLogData;
import com.example.entity.*;
import com.example.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import java.sql.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dịch vụ xử lý log từ Kafka, lưu vào Redis, database và gửi qua WebSocket.
 */
@Service
@Slf4j
public class KafkaConsumerService {
    // Các hằng số định nghĩa prefix cho Redis và topic WebSocket
    private static final String REDIS_KEY_PREFIX = "product_log:"; // Prefix cho key lưu log trong Redis
    private static final String REDIS_PRODUCTION_PREFIX = "production:"; // Prefix cho hash production trong Redis
    private static final String REDIS_CACHE_PREFIX = "cache:"; // Prefix cho cache ID trong Redis
    private static final String WEBSOCKET_PRODUCTION_TOPIC = "/topic/production"; // Topic WebSocket cho production
    private static final String WEBSOCKET_EQUIPMENT_TOPIC = "/topic/equipment"; // Topic WebSocket cho equipment
    private static final String WEBSOCKET_ERROR_TOPIC = "/topic/errors"; // Topic WebSocket cho error
    private static final String WEBSOCKET_WORKER_TOPIC = "/topic/worker"; // Topic WebSocket cho worker
    private static final String WEBSOCKET_SUMMARY_TOPIC = "/topic/summary"; // Topic WebSocket cho summary
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of("UTC")); // Định dạng timestamp
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC")); // Định dạng ngày
    private static final int BATCH_SIZE = 100; // Kích thước batch tối đa (100 log)
    private static final long BATCH_TIMEOUT_MS = 1000; // Thời gian chờ batch (1 giây)
    private static final long WEBSOCKET_FLUSH_INTERVAL_MS = 1000; // Tần suất gửi WebSocket (1 giây)

    // Các dependency được inject
    private final RedisTemplate<String, Object> redisTemplate; // Template để tương tác với Redis
    private final SimpMessagingTemplate messagingTemplate; // Template để gửi WebSocket
    private final FactoryRepository factoryRepository; // Repository cho Factory
    private final LineRepository lineRepository; // Repository cho Line
    private final StationRepository stationRepository; // Repository cho Station
    private final ProductRepository productRepository; // Repository cho Product
    private final EquipmentRepository equipmentRepository; // Repository cho Equipment
    private final WorkerRepository workerRepository; // Repository cho Worker
    private final ShiftRepository shiftRepository; // Repository cho Shift
    private final ErrorTypeRepository errorTypeRepository; // Repository cho ErrorType
    private final WorkOrderRepository workOrderRepository; // Repository cho WorkOrder
    private final ProductionLogRepository productionLogRepository; // Repository cho ProductionLog
    private final EquipmentLogRepository equipmentLogRepository; // Repository cho EquipmentLog
    private final ErrorLogRepository errorLogRepository; // Repository cho ErrorLog
    private final WorkerLogRepository workerLogRepository; // Repository cho WorkerLog
    private final ProductionSummaryRepository productionSummaryRepository; // Repository cho ProductionSummary
    private final EquipmentSummaryRepository equipmentSummaryRepository; // Repository cho EquipmentSummary
    private final ErrorSummaryRepository errorSummaryRepository; // Repository cho ErrorSummary
    private final WorkerSummaryRepository workerSummaryRepository; // Repository cho WorkerSummary
    private final ObjectMapper objectMapper; // ObjectMapper để parse JSON

    // Hàng đợi và executor để xử lý batch
    private final BlockingQueue<BatchItem> batchQueue = new ArrayBlockingQueue<>(1000); // Hàng đợi cho log
    private final ExecutorService batchExecutor = Executors.newSingleThreadExecutor(); // Thread xử lý batch
    private final ScheduledExecutorService websocketScheduler = Executors.newScheduledThreadPool(1); // Scheduler cho WebSocket

    // Buffer và trạng thái cho WebSocket
    private final Map<String, ArrayNode> webSocketBuffer = new ConcurrentHashMap<>(); // Buffer dữ liệu WebSocket
    private final Set<String> dirtyTopics = ConcurrentHashMap.newKeySet(); // Các topic có dữ liệu mới
    private final AtomicBoolean isFlushingWebSocket = new AtomicBoolean(false); // Trạng thái gửi WebSocket

    // Cache ID cho các thực thể
    private final Map<String, Integer> factoryNameToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> lineNameToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> stationNameToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> productNameToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> equipmentSerialToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> workerNameToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> shiftCodeToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> errorCodeToId = new ConcurrentHashMap<>();
    private final Map<String, Integer> workOrderNameToId = new ConcurrentHashMap<>();

    /**
     * Lớp lưu trữ thông tin batch để xử lý log và summary.
     */
    @Data
    private static class BatchItem {
        private ProductLog productLog; // Dữ liệu log
        private String operation; // Operation (c, u, d, r)
        private Integer productId; // ID sản phẩm
        private String equipmentState; // Trạng thái thiết bị
        private String date; // Ngày tóm tắt
    }

    /**
     * Constructor với các dependency được inject.
     */
    public KafkaConsumerService(
            RedisTemplate<String, Object> redisTemplate,
            SimpMessagingTemplate messagingTemplate,
            FactoryRepository factoryRepository,
            LineRepository lineRepository,
            StationRepository stationRepository,
            ProductRepository productRepository,
            EquipmentRepository equipmentRepository,
            WorkerRepository workerRepository,
            ShiftRepository shiftRepository,
            ErrorTypeRepository errorTypeRepository,
            WorkOrderRepository workOrderRepository,
            ProductionLogRepository productionLogRepository,
            EquipmentLogRepository equipmentLogRepository,
            ErrorLogRepository errorLogRepository,
            WorkerLogRepository workerLogRepository,
            ProductionSummaryRepository productionSummaryRepository,
            EquipmentSummaryRepository equipmentSummaryRepository,
            ErrorSummaryRepository errorSummaryRepository,
            WorkerSummaryRepository workerSummaryRepository,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.factoryRepository = factoryRepository;
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.productRepository = productRepository;
        this.equipmentRepository = equipmentRepository;
        this.workerRepository = workerRepository;
        this.shiftRepository = shiftRepository;
        this.errorTypeRepository = errorTypeRepository;
        this.workOrderRepository = workOrderRepository;
        this.productionLogRepository = productionLogRepository;
        this.equipmentLogRepository = equipmentLogRepository;
        this.errorLogRepository = errorLogRepository;
        this.workerLogRepository = workerLogRepository;
        this.productionSummaryRepository = productionSummaryRepository;
        this.equipmentSummaryRepository = equipmentSummaryRepository;
        this.errorSummaryRepository = errorSummaryRepository;
        this.workerSummaryRepository = workerSummaryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Khởi tạo: Bắt đầu thread xử lý batch và lập lịch gửi WebSocket.
     */
    @PostConstruct
    public void init() {
        batchExecutor.execute(this::processBatchQueue); // Khởi động thread xử lý batch
        websocketScheduler.scheduleAtFixedRate(this::tryFlushWebSocketBuffer, WEBSOCKET_FLUSH_INTERVAL_MS,
                WEBSOCKET_FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS); // Lập lịch gửi WebSocket mỗi 1 giây
        log.info("KafkaConsumerService khởi tạo thành công");
    }

    /**
     * Dọn dẹp: Tắt executor, lưu batch còn lại và gửi WebSocket cuối cùng.
     */
    @PreDestroy
    public void destroy() {
        batchExecutor.shutdown(); // Tắt thread batch
        websocketScheduler.shutdown(); // Tắt scheduler WebSocket
        flushRemainingBatch(); // Lưu batch còn lại
        flushWebSocketBuffer(); // Gửi WebSocket cuối cùng
        log.info("KafkaConsumerService tắt thành công");
    }

    /**
     * Lắng nghe log từ Kafka topic và xử lý.
     */
    @KafkaListener(
            topics = "dbz_.public.product_log",
            groupId = "product_log_group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenProductLog(ConsumerRecord<String, String> record) {
        try {
            // Kiểm tra record hợp lệ
            if (record == null || record.value() == null) {
                log.error("Nhận được record hoặc giá trị null từ Kafka topic: {}", record);
                return;
            }

            log.debug("Nhận message Kafka: {}", record.value());
            // Parse JSON thành DebeziumMessage
            DebeziumMessage debeziumMessage = objectMapper.readValue(record.value(), DebeziumMessage.class);
            log.debug("Parsed DebeziumMessage: {}", debeziumMessage);

            // Kiểm tra payload
            DebeziumPayload payload = debeziumMessage.getPayload();
            if (payload == null) {
                log.warn("Payload null trong message: {}", record.value());
                return;
            }

            // Lấy dữ liệu sau thay đổi (after)
            ProductLogData data = payload.getAfter();
            if (data == null) {
                log.warn("Không có dữ liệu 'after' hợp lệ trong payload: {}", payload);
                return;
            }

            // Kiểm tra ID sản phẩm
            if (data.getId() == null || data.getId() == 0) {
                log.error("ID sản phẩm không hợp lệ trong dữ liệu: {}", data);
                return;
            }

            // Định dạng thời gian
            String formattedTimestamp = TIMESTAMP_FORMATTER.format(data.getTimestamp());
            String date = DATE_FORMATTER.format(data.getTimestamp());

            // Resolve ID cho các thực thể
            Integer factoryId = resolveFactory(data.getFactoryName());
            Integer lineId = resolveLine(data.getLineName(), factoryId);
            Integer stationId = resolveStation(data.getStationName(), lineId);
            Integer productId = resolveProduct(data.getProductName());
            Integer equipmentId = resolveEquipment(data.getEquipmentSerial(), stationId);
            Integer workerId = resolveWorker(data.getWorkerName());
            Integer shiftId = resolveShift(data.getShiftCode());
            Integer errorTypeId = resolveErrorType(data.getErrorCode());
            Integer workOrderId = resolveWorkOrder(data.getWorkOrderName(), productId, factoryId);

            // Xác định trạng thái thiết bị
            String equipmentState = mapToEquipmentState(data.getStatus(), data.getErrorCode());

            // Tạo thông điệp log
            String logMessage = generateLogMessage(
                    payload.getOperation(), data.getId(), data.getProductName(), data.getStationName(),
                    data.getLineName(), data.getFactoryName(), data.getWorkerName(),
                    data.getShiftCode(), data.getErrorCode(), data.getWorkOrderName(),
                    data.getStatus(), formattedTimestamp,
                    data.getCreatedAt() != null ? TIMESTAMP_FORMATTER.format(data.getCreatedAt()) : "N/A"
            );
            log.info(logMessage);

            // Chuyển đổi sang ProductLog để lưu
            ProductLog productLog = convertToProductLog(data);

            // Lưu vào Redis
            storeInRedis(productLog, payload.getOperation());

            // Thêm vào hàng đợi batch để lưu database
            BatchItem batchItem = new BatchItem();
            batchItem.setProductLog(productLog);
            batchItem.setOperation(payload.getOperation());
            batchItem.setProductId(productId);
            batchItem.setEquipmentState(equipmentState);
            batchItem.setDate(date);
            batchQueue.offer(batchItem);

            // Thêm dữ liệu vào buffer WebSocket
            bufferWebSocketData(productLog, equipmentState, date);

        } catch (Exception e) {
            log.error("Lỗi xử lý message Kafka: {}", record, e);
        }
    }

    /**
     * Xử lý hàng đợi batch: Lấy tối đa 100 log hoặc đợi 1 giây, sau đó lưu vào database.
     */
    private void processBatchQueue() {
        List<BatchItem> batch = new ArrayList<>();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                batch.clear();
                batch.add(batchQueue.take()); // Lấy log đầu tiên (chờ nếu queue rỗng)
                batchQueue.drainTo(batch, BATCH_SIZE - 1); // Lấy tối đa 99 log nữa
                storeBatchInDatabase(batch); // Lưu batch vào database
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread xử lý batch bị gián đoạn");
                break;
            } catch (Exception e) {
                log.error("Lỗi xử lý batch: {}", batch, e);
            }

            // Nếu batch chưa đủ, đợi 1 giây để tích lũy thêm log
            if (batch.size() < BATCH_SIZE) {
                try {
                    Thread.sleep(BATCH_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Thread xử lý batch bị gián đoạn trong lúc sleep");
                    break;
                }
            }
        }
    }

    /**
     * Lưu batch log và summary vào database trong một transaction.
     */
    @Transactional
    private void storeBatchInDatabase(List<BatchItem> batch) {
        try {
            // Khởi tạo danh sách cho các loại log và summary
            List<ProductionLog> productionLogs = new ArrayList<>();
            List<EquipmentLog> equipmentLogs = new ArrayList<>();
            List<ErrorLog> errorLogs = new ArrayList<>();
            List<WorkerLog> workerLogs = new ArrayList<>();
            List<ProductionSummary> productionSummaries = new ArrayList<>();
            List<EquipmentSummary> equipmentSummaries = new ArrayList<>();
            List<ErrorSummary> errorSummaries = new ArrayList<>();
            List<WorkerSummary> workerSummaries = new ArrayList<>();

            // Duyệt qua batch để tạo các bản ghi
            for (BatchItem item : batch) {
                ProductLog data = item.getProductLog();
                String operation = item.getOperation();
                String equipmentState = item.getEquipmentState();
                String date = item.getDate();

                // Tạo ProductionLog
                ProductionLog prodLog = new ProductionLog();
                prodLog.setProductId(data.getId());
                prodLog.setEventTimestamp(data.getTimestamp());
                prodLog.setProductName(data.getProductName());
                prodLog.setFactoryName(data.getFactoryName());
                prodLog.setLineName(data.getLineName());
                prodLog.setStationName(data.getStationName());
                prodLog.setShiftCode(data.getShiftCode());
                prodLog.setWorkOrderName(data.getWorkOrderName());
                prodLog.setStatus(data.getStatus());
                prodLog.setOperation(operation);
                productionLogs.add(prodLog);

                // Tạo EquipmentLog nếu có equipmentSerial
                if (data.getEquipmentSerial() != null) {
                    EquipmentLog equipLog = new EquipmentLog();
                    equipLog.setEquipmentSerial(data.getEquipmentSerial());
                    equipLog.setEventTimestamp(data.getTimestamp());
                    equipLog.setStatus(equipmentState);
                    equipLog.setErrorCode(data.getErrorCode());
                    equipLog.setLineName(data.getLineName());
                    equipLog.setStationName(data.getStationName());
                    equipLog.setOperation(operation);
                    equipmentLogs.add(equipLog);
                }

                // Tạo ErrorLog nếu có errorCode
                if (data.getErrorCode() != null) {
                    ErrorLog errorLog = new ErrorLog();
                    errorLog.setErrorCode(data.getErrorCode());
                    errorLog.setEventTimestamp(data.getTimestamp());
                    errorLog.setProductName(data.getProductName());
                    errorLog.setStationName(data.getStationName());
                    errorLog.setEquipmentSerial(data.getEquipmentSerial());
                    errorLog.setWorkerName(data.getWorkerName());
                    errorLog.setShiftCode(data.getShiftCode());
                    errorLog.setOperation(operation);
                    errorLogs.add(errorLog);
                }

                // Tạo WorkerLog nếu có workerName
                if (data.getWorkerName() != null) {
                    WorkerLog workerLog = new WorkerLog();
                    workerLog.setWorkerName(data.getWorkerName());
                    workerLog.setEventTimestamp(data.getTimestamp());
                    workerLog.setShiftCode(data.getShiftCode());
                    workerLog.setProductName(data.getProductName());
                    workerLog.setErrorCode(data.getErrorCode());
                    workerLog.setOutputCount(data.getErrorCode() == null && "Completed".equals(data.getStatus()) ? 1 : 0);
                    workerLog.setOperation(operation);
                    workerLogs.add(workerLog);
                }

                // Tạo ProductionSummary
                ProductionSummary prodSummary = new ProductionSummary();
                prodSummary.setSummaryDate(Date.valueOf(date));
                prodSummary.setFactoryName(data.getFactoryName());
                prodSummary.setLineName(data.getLineName());
                prodSummary.setProductName(data.getProductName());
                prodSummary.setShiftCode(data.getShiftCode());
                prodSummary.setWorkOrderName(data.getWorkOrderName());
                prodSummary.setOutputCount("Completed".equals(data.getStatus()) ? 1L : 0L);
                productionSummaries.add(prodSummary);

                // Tạo EquipmentSummary nếu có equipmentSerial
                if (data.getEquipmentSerial() != null) {
                    EquipmentSummary equipSummary = new EquipmentSummary();
                    equipSummary.setSummaryDate(Date.valueOf(date));
                    equipSummary.setEquipmentSerial(data.getEquipmentSerial());
                    if ("Running".equals(equipmentState)) {
                        equipSummary.setRunningSeconds(60L);
                    } else if ("Stopped".equals(equipmentState)) {
                        equipSummary.setStoppedSeconds(60L);
                    } else if ("Maintenance".equals(equipmentState)) {
                        equipSummary.setMaintenanceSeconds(60L);
                    }
                    equipSummary.setErrorCount(data.getErrorCode() != null ? 1L : 0L);
                    equipmentSummaries.add(equipSummary);
                }

                // Tạo ErrorSummary nếu có errorCode
                if (data.getErrorCode() != null) {
                    ErrorSummary errorSummary = new ErrorSummary();
                    errorSummary.setSummaryDate(Date.valueOf(date));
                    errorSummary.setErrorCode(data.getErrorCode());
                    errorSummary.setProductName(data.getProductName());
                    errorSummary.setStationName(data.getStationName());
                    errorSummary.setEquipmentSerial(data.getEquipmentSerial());
                    errorSummary.setWorkerName(data.getWorkerName());
                    errorSummary.setShiftCode(data.getShiftCode());
                    errorSummary.setErrorCount(1L);
                    errorSummaries.add(errorSummary);
                }

                // Tạo WorkerSummary nếu có workerName
                if (data.getWorkerName() != null) {
                    WorkerSummary workerSummary = new WorkerSummary();
                    workerSummary.setSummaryDate(Date.valueOf(date));
                    workerSummary.setWorkerName(data.getWorkerName());
                    workerSummary.setShiftCode(data.getShiftCode());
                    workerSummary.setOutputCount("Completed".equals(data.getStatus()) ? 1L : 0L);
                    workerSummary.setErrorCount(data.getErrorCode() != null ? 1L : 0L);
                    workerSummaries.add(workerSummary);
                }
            }

            // Lưu batch vào database
            if (!productionLogs.isEmpty()) {
                productionLogRepository.saveAll(productionLogs);
            }
            if (!equipmentLogs.isEmpty()) {
                equipmentLogRepository.saveAll(equipmentLogs);
            }
            if (!errorLogs.isEmpty()) {
                errorLogRepository.saveAll(errorLogs);
            }
            if (!workerLogs.isEmpty()) {
                workerLogRepository.saveAll(workerLogs);
            }
            if (!productionSummaries.isEmpty()) {
                productionSummaryRepository.saveAll(productionSummaries);
            }
            if (!equipmentSummaries.isEmpty()) {
                equipmentSummaryRepository.saveAll(equipmentSummaries);
            }
            if (!errorSummaries.isEmpty()) {
                errorSummaryRepository.saveAll(errorSummaries);
            }
            if (!workerSummaries.isEmpty()) {
                workerSummaryRepository.saveAll(workerSummaries);
            }

            log.debug("Đã lưu batch {} log vào database", batch.size());
        } catch (Exception e) {
            log.error("Lỗi lưu batch vào database: {}", batch, e);
        }
    }

    /**
     * Lưu các log còn lại trong queue khi tắt ứng dụng.
     */
    private void flushRemainingBatch() {
        List<BatchItem> remaining = new ArrayList<>();
        batchQueue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            storeBatchInDatabase(remaining);
            log.info("Đã lưu batch còn lại với {} log", remaining.size());
        }
    }

    /**
     * Thêm dữ liệu vào buffer WebSocket và kiểm tra kích thước buffer.
     */
    private void bufferWebSocketData(ProductLog data, String equipmentState, String date) {
        try {
            // Dữ liệu production
            ObjectNode prodData = JsonNodeFactory.instance.objectNode();
            prodData.put("factory", data.getFactoryName());
            prodData.put("line", data.getLineName());
            prodData.put("station", data.getStationName());
            prodData.put("product", data.getProductName());
            prodData.put("shift", data.getShiftCode());
            prodData.put("workOrder", data.getWorkOrderName());
            prodData.put("status", data.getStatus());

            // Tính % hoàn thành cho work order
            if (data.getWorkOrderName() != null) {
                WorkOrder workOrder = workOrderRepository.findByWorkOrderName(data.getWorkOrderName());
                if (workOrder != null) {
                    String prodHashKey = REDIS_PRODUCTION_PREFIX + (data.getFactoryName() != null ? data.getFactoryName() : "unknown");
                    String prodField = String.format("%s:%s:%s:%s:%s",
                            data.getLineName() != null ? data.getLineName() : "unknown",
                            data.getStationName() != null ? data.getStationName() : "unknown",
                            data.getProductName() != null ? data.getProductName() : "unknown",
                            data.getShiftCode() != null ? data.getShiftCode() : "unknown",
                            data.getWorkOrderName());
                    Object countObj = redisTemplate.opsForHash().get(prodHashKey, prodField);
                    long actualCount = countObj != null ? Long.parseLong(countObj.toString()) : 0;
                    double completion = (double) actualCount / workOrder.getQuantityOrdered() * 100;
                    prodData.put("completion", String.format("%.2f", completion));
                }
            }

            // Thêm vào buffer và đánh dấu topic
            ArrayNode prodBuffer = webSocketBuffer.computeIfAbsent(WEBSOCKET_PRODUCTION_TOPIC, k -> JsonNodeFactory.instance.arrayNode());
            prodBuffer.add(prodData);
            dirtyTopics.add(WEBSOCKET_PRODUCTION_TOPIC);

            // Dữ liệu equipment
            if (data.getEquipmentSerial() != null) {
                ObjectNode equipData = JsonNodeFactory.instance.objectNode();
                equipData.put("equipmentSerial", data.getEquipmentSerial());
                equipData.put("state", equipmentState);
                equipData.put("errorCode", data.getErrorCode());
                equipData.put("line", data.getLineName());
                equipData.put("station", data.getStationName());
                ArrayNode equipBuffer = webSocketBuffer.computeIfAbsent(WEBSOCKET_EQUIPMENT_TOPIC, k -> JsonNodeFactory.instance.arrayNode());
                equipBuffer.add(equipData);
                dirtyTopics.add(WEBSOCKET_EQUIPMENT_TOPIC);
            }

            // Dữ liệu error
            if (data.getErrorCode() != null) {
                ObjectNode errorData = JsonNodeFactory.instance.objectNode();
                errorData.put("errorCode", data.getErrorCode());
                errorData.put("product", data.getProductName());
                errorData.put("station", data.getStationName());
                errorData.put("equipment", data.getEquipmentSerial());
                errorData.put("worker", data.getWorkerName());
                errorData.put("shift", data.getShiftCode());
                ArrayNode errorBuffer = webSocketBuffer.computeIfAbsent(WEBSOCKET_ERROR_TOPIC, k -> JsonNodeFactory.instance.arrayNode());
                errorBuffer.add(errorData);
                dirtyTopics.add(WEBSOCKET_ERROR_TOPIC);
            }

            // Dữ liệu worker
            if (data.getWorkerName() != null) {
                ObjectNode workerData = JsonNodeFactory.instance.objectNode();
                workerData.put("worker", data.getWorkerName());
                workerData.put("shift", data.getShiftCode());
                workerData.put("product", data.getProductName());
                workerData.put("errorCode", data.getErrorCode());
                workerData.put("output", "Completed".equals(data.getStatus()) ? 1 : 0);
                ArrayNode workerBuffer = webSocketBuffer.computeIfAbsent(WEBSOCKET_WORKER_TOPIC, k -> JsonNodeFactory.instance.arrayNode());
                workerBuffer.add(workerData);
                dirtyTopics.add(WEBSOCKET_WORKER_TOPIC);
            }

            // Dữ liệu summary
            ObjectNode summaryData = JsonNodeFactory.instance.objectNode();
            summaryData.put("date", date);
            summaryData.put("factory", data.getFactoryName());
            summaryData.put("line", data.getLineName());
            summaryData.put("product", data.getProductName());
            summaryData.put("output", "Completed".equals(data.getStatus()) ? 1 : 0);
            summaryData.put("errorCount", data.getErrorCode() != null ? 1 : 0);
            ArrayNode summaryBuffer = webSocketBuffer.computeIfAbsent(WEBSOCKET_SUMMARY_TOPIC, k -> JsonNodeFactory.instance.arrayNode());
            summaryBuffer.add(summaryData);
            dirtyTopics.add(WEBSOCKET_SUMMARY_TOPIC);

            // Kiểm tra kích thước buffer, gửi ngay nếu đầy
            dirtyTopics.forEach(topic -> {
                ArrayNode buffer = webSocketBuffer.get(topic);
                if (buffer != null && buffer.size() >= BATCH_SIZE) {
                    flushWebSocketBufferForTopic(topic);
                }
            });
        } catch (Exception e) {
            log.error("Lỗi thêm dữ liệu vào buffer WebSocket: {}", data, e);
        }
    }

    /**
     * Thử gửi buffer WebSocket nếu có dữ liệu mới.
     */
    private void tryFlushWebSocketBuffer() {
        if (!dirtyTopics.isEmpty() && isFlushingWebSocket.compareAndSet(false, true)) {
            try {
                flushWebSocketBuffer();
            } finally {
                isFlushingWebSocket.set(false);
            }
        }
    }

    /**
     * Gửi tất cả buffer WebSocket cho các topic có dữ liệu mới.
     */
    private void flushWebSocketBuffer() {
        dirtyTopics.forEach(this::flushWebSocketBufferForTopic);
        dirtyTopics.clear();
    }

    /**
     * Gửi buffer WebSocket cho một topic cụ thể.
     */
    private void flushWebSocketBufferForTopic(String topic) {
        ArrayNode data = webSocketBuffer.get(topic);
        if (data != null && !data.isEmpty()) {
            try {
                messagingTemplate.convertAndSend(topic, data);
                data.removeAll();
                log.debug("Đã gửi dữ liệu WebSocket cho topic: {}", topic);
            } catch (Exception e) {
                log.error("Lỗi gửi dữ liệu WebSocket cho topic: {}", topic, e);
            }
        }
    }

    /**
     * Chuyển đổi ProductLogData thành ProductLog để lưu vào Redis và database.
     */
    private ProductLog convertToProductLog(ProductLogData data) {
        ProductLog productLog = new ProductLog();
        productLog.setId(data.getId());
        productLog.setSerialNumber(data.getSerialNumber());
        productLog.setTimestamp(data.getTimestamp());
        productLog.setProductName(data.getProductName());
        productLog.setStationName(data.getStationName());
        productLog.setLineName(data.getLineName());
        productLog.setFactoryName(data.getFactoryName());
        productLog.setEquipmentSerial(data.getEquipmentSerial());
        productLog.setWorkerName(data.getWorkerName());
        productLog.setShiftCode(data.getShiftCode());
        productLog.setErrorCode(data.getErrorCode());
        productLog.setWorkOrderName(data.getWorkOrderName());
        productLog.setStatus(data.getStatus());
        productLog.setCreatedAt(data.getCreatedAt());
        return productLog;
    }

    /**
     * Lưu log vào Redis với TTL 1 giờ.
     */
    private void storeInRedis(ProductLog data, String operation) {
        try {
            if (data == null || data.getId() == null) {
                log.error("Dữ liệu không hợp lệ để lưu vào Redis: {}", data);
                return;
            }
            String key = REDIS_KEY_PREFIX + data.getId();
            log.debug("Lưu vào Redis: key={}, data={}", key, data);
            redisTemplate.opsForValue().set(key, data, 1, TimeUnit.HOURS); // Lưu log với TTL 1 giờ

            // Cập nhật hash production
            String prodHashKey = REDIS_PRODUCTION_PREFIX + (data.getFactoryName() != null ? data.getFactoryName() : "unknown");
            String prodField = String.format("%s:%s:%s:%s:%s",
                    data.getLineName() != null ? data.getLineName() : "unknown",
                    data.getStationName() != null ? data.getStationName() : "unknown",
                    data.getProductName() != null ? data.getProductName() : "unknown",
                    data.getShiftCode() != null ? data.getShiftCode() : "unknown",
                    data.getWorkOrderName() != null ? data.getWorkOrderName() : "unknown");
            redisTemplate.opsForHash().increment(prodHashKey, prodField, 1);
            redisTemplate.expire(prodHashKey, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Lỗi lưu dữ liệu vào Redis: {}", data, e);
        }
    }

    /**
     * Tạo thông điệp log chi tiết.
     * Nếu chỉ xử lý INSERT (op="c"), có thể bỏ các case "u", "d", "r".
     */
    private String generateLogMessage(
            String operation, Long productId, String productName, String stationName,
            String lineName, String factoryName, String workerName, String shiftCode,
            String errorCode, String workOrderName, String status, String timestamp, String createdAt
    ) {
        String action;
        switch (operation) {
            case "r":
                action = "đọc từ snapshot";
                break;
            case "c":
                action = "tạo mới";
                break;
            case "u":
                action = "cập nhật";
                break;
            case "d":
                action = "xóa";
                break;
            default:
                action = "xử lý (operation không xác định)";
        }

        String baseMessage = String.format(
                "Sản phẩm ID: %d, Tên: %s %s tại Trạm: %s, Dây chuyền: %s, Nhà máy: %s " +
                        "bởi Công nhân: %s (Ca: %s) trên Lệnh sản xuất: %s với Trạng thái: %s tại %s, Tạo lúc: %s",
                productId, productName != null ? productName : "Không xác định", action,
                stationName != null ? stationName : "Không xác định",
                lineName != null ? lineName : "Không xác định",
                factoryName != null ? factoryName : "Không xác định",
                workerName != null ? workerName : "Không xác định",
                shiftCode != null ? shiftCode : "N/A",
                workOrderName != null ? workOrderName : "N/A",
                status != null ? status : "Không xác định", timestamp, createdAt
        );

        if (errorCode != null) {
            baseMessage += String.format(". Phát hiện lỗi: %s", errorCode);
            log.warn("Phát hiện lỗi cho Sản phẩm ID: {} - Mã lỗi: {}", productId, errorCode);
        }

        return baseMessage;
    }

    /**
     * Ánh xạ trạng thái thiết bị dựa trên status và errorCode.
     */
    private String mapToEquipmentState(String status, String errorCode) {
        if (errorCode != null) {
            return "Stopped"; // Dừng nếu có lỗi
        }
        switch (status != null ? status.toLowerCase() : "unknown") {
            case "in progress":
                return "Running"; // Đang chạy
            case "maintenance":
                return "Maintenance"; // Bảo trì
            default:
                return "Stopped"; // Dừng
        }
    }

    /**
     * Resolve ID cho Factory, tạo mới nếu không tồn tại.
     */
    private Integer resolveFactory(String factoryName) {
        if (factoryName == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "factory:" + factoryName;
        Integer factoryId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (factoryId != null) {
            factoryNameToId.put(factoryName, factoryId);
            return factoryId;
        }
        factoryId = factoryNameToId.get(factoryName);
        if (factoryId != null) {
            return factoryId;
        }
        Factory factory = factoryRepository.findByFactoryName(factoryName);
        if (factory == null) {
            factory = new Factory();
            factory.setFactoryName(factoryName);
            factory = factoryRepository.save(factory);
        }
        factoryId = factory.getId();
        factoryNameToId.put(factoryName, factoryId);
        redisTemplate.opsForValue().set(cacheKey, factoryId, 24, TimeUnit.HOURS);
        return factoryId;
    }

    /**
     * Resolve ID cho Line, tạo mới nếu không tồn tại.
     */
    private Integer resolveLine(String lineName, Integer factoryId) {
        if (lineName == null || factoryId == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "line:" + lineName + ":" + factoryId;
        Integer lineId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (lineId != null) {
            lineNameToId.put(lineName + ":" + factoryId, lineId);
            return lineId;
        }
        lineId = lineNameToId.get(lineName + ":" + factoryId);
        if (lineId != null) {
            return lineId;
        }
        Line line = lineRepository.findByLineNameAndFactoryId(lineName, factoryId);
        if (line == null) {
            line = new Line();
            line.setLineName(lineName);
            line.setFactoryId(factoryId);
            line = lineRepository.save(line);
        }
        lineId = line.getId();
        lineNameToId.put(lineName + ":" + factoryId, lineId);
        redisTemplate.opsForValue().set(cacheKey, lineId, 24, TimeUnit.HOURS);
        return lineId;
    }

    /**
     * Resolve ID cho Station, tạo mới nếu không tồn tại.
     */
    private Integer resolveStation(String stationName, Integer lineId) {
        if (stationName == null || lineId == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "station:" + stationName + ":" + lineId;
        Integer stationId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (stationId != null) {
            stationNameToId.put(stationName + ":" + lineId, stationId);
            return stationId;
        }
        stationId = stationNameToId.get(stationName + ":" + lineId);
        if (stationId != null) {
            return stationId;
        }
        Station station = stationRepository.findByStationNameAndLineId(stationName, lineId);
        if (station == null) {
            station = new Station();
            station.setStationName(stationName);
            station.setLineId(lineId);
            station = stationRepository.save(station);
        }
        stationId = station.getId();
        stationNameToId.put(stationName + ":" + lineId, stationId);
        redisTemplate.opsForValue().set(cacheKey, stationId, 24, TimeUnit.HOURS);
        return stationId;
    }

    /**
     * Resolve ID cho Product, tạo mới nếu không tồn tại.
     */
    private Integer resolveProduct(String productName) {
        if (productName == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "product:" + productName;
        Integer productId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (productId != null) {
            productNameToId.put(productName, productId);
            return productId;
        }
        productId = productNameToId.get(productName);
        if (productId != null) {
            return productId;
        }
        Product product = productRepository.findByProductName(productName);
        if (product == null) {
            product = new Product();
            product.setProductName(productName);
            product.setProductCode("AUTO_" + productName.hashCode());
            product = productRepository.save(product);
        }
        productId = product.getId();
        productNameToId.put(productName, productId);
        redisTemplate.opsForValue().set(cacheKey, productId, 24, TimeUnit.HOURS);
        return productId;
    }

    /**
     * Resolve ID cho Equipment, tạo mới nếu không tồn tại.
     */
    private Integer resolveEquipment(String serialNumber, Integer stationId) {
        if (serialNumber == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "equipment:" + serialNumber;
        Integer equipmentId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (equipmentId != null) {
            equipmentSerialToId.put(serialNumber, equipmentId);
            return equipmentId;
        }
        equipmentId = equipmentSerialToId.get(serialNumber);
        if (equipmentId != null) {
            return equipmentId;
        }
        Equipment equipment = equipmentRepository.findBySerialNumber(serialNumber);
        if (equipment == null) {
            equipment = new Equipment();
            equipment.setSerialNumber(serialNumber);
            equipment.setType("Unknown");
            equipment.setStationId(stationId);
            equipment.setStatus("Operational");
            equipment.setDowntime(0L);
            equipment = equipmentRepository.save(equipment);
        }
        equipmentId = equipment.getId();
        equipmentSerialToId.put(serialNumber, equipmentId);
        redisTemplate.opsForValue().set(cacheKey, equipmentId, 24, TimeUnit.HOURS);
        return equipmentId;
    }

    /**
     * Resolve ID cho Worker, tạo mới nếu không tồn tại.
     */
    private Integer resolveWorker(String workerName) {
        if (workerName == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "worker:" + workerName;
        Integer workerId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (workerId != null) {
            workerNameToId.put(workerName, workerId);
            return workerId;
        }
        workerId = workerNameToId.get(workerName);
        if (workerId != null) {
            return workerId;
        }
        Worker worker = workerRepository.findByWorkerName(workerName);
        if (worker == null) {
            worker = new Worker();
            worker.setWorkerName(workerName);
            worker.setRole("Operator");
            worker.setDepartment("Production");
            worker.setSkillLevel("Junior");
            worker = workerRepository.save(worker);
        }
        workerId = worker.getId();
        workerNameToId.put(workerName, workerId);
        redisTemplate.opsForValue().set(cacheKey, workerId, 24, TimeUnit.HOURS);
        return workerId;
    }

    /**
     * Resolve ID cho Shift, tạo mới nếu không tồn tại.
     */
    private Integer resolveShift(String shiftCode) {
        if (shiftCode == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "shift:" + shiftCode;
        Integer shiftId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (shiftId != null) {
            shiftCodeToId.put(shiftCode, shiftId);
            return shiftId;
        }
        shiftId = shiftCodeToId.get(shiftCode);
        if (shiftId != null) {
            return shiftId;
        }
        Shift shift = shiftRepository.findByShiftCode(shiftCode);
        if (shift == null) {
            shift = new Shift();
            shift.setShiftCode(shiftCode);
            shift.setStartTime(java.time.LocalTime.of(0, 0));
            shift.setEndTime(java.time.LocalTime.of(23, 59));
            shift = shiftRepository.save(shift);
        }
        shiftId = shift.getId();
        shiftCodeToId.put(shiftCode, shiftId);
        redisTemplate.opsForValue().set(cacheKey, shiftId, 24, TimeUnit.HOURS);
        return shiftId;
    }

    /**
     * Resolve ID cho ErrorType, tạo mới nếu không tồn tại.
     */
    private Integer resolveErrorType(String errorCode) {
        if (errorCode == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "error_type:" + errorCode;
        Integer errorTypeId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (errorTypeId != null) {
            errorCodeToId.put(errorCode, errorTypeId);
            return errorTypeId;
        }
        errorTypeId = errorCodeToId.get(errorCode);
        if (errorTypeId != null) {
            return errorTypeId;
        }
        ErrorType errorType = errorTypeRepository.findByErrorCode(errorCode);
        if (errorType == null) {
            errorType = new ErrorType();
            errorType.setErrorCode(errorCode);
            errorType.setDescription("Unknown error");
            errorType = errorTypeRepository.save(errorType);
        }
        errorTypeId = errorType.getId();
        errorCodeToId.put(errorCode, errorTypeId);
        redisTemplate.opsForValue().set(cacheKey, errorTypeId, 24, TimeUnit.HOURS);
        return errorTypeId;
    }

    /**
     * Resolve ID cho WorkOrder, tạo mới nếu không tồn tại.
     */
    private Integer resolveWorkOrder(String workOrderName, Integer productId, Integer factoryId) {
        if (workOrderName == null || productId == null || factoryId == null) return null;
        String cacheKey = REDIS_CACHE_PREFIX + "work_order:" + workOrderName;
        Integer workOrderId = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (workOrderId != null) {
            workOrderNameToId.put(workOrderName, workOrderId);
            return workOrderId;
        }
        workOrderId = workOrderNameToId.get(workOrderName);
        if (workOrderId != null) {
            return workOrderId;
        }
        WorkOrder workOrder = workOrderRepository.findByWorkOrderName(workOrderName);
        if (workOrder == null) {
            workOrder = new WorkOrder();
            workOrder.setWorkOrderName(workOrderName);
            workOrder.setProductId(productId);
            workOrder.setQuantityOrdered(1000);
            workOrder.setStartDate(java.time.LocalDateTime.now());
            workOrder.setDueDate(java.time.LocalDateTime.now().plusDays(30));
            workOrder.setStatus("In Progress");
            workOrder.setFactoryId(factoryId);
            workOrder = workOrderRepository.save(workOrder);
        }
        workOrderId = workOrder.getId();
        workOrderNameToId.put(workOrderName, workOrderId);
        redisTemplate.opsForValue().set(cacheKey, workOrderId, 24, TimeUnit.HOURS);
        return workOrderId;
    }
}