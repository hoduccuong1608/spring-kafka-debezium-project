package com.example.sensormonitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DebeziumPayload {
    private SensorReading before;
    private SensorReading after;
    private Source source;
    private Transaction transaction;
    private String op;

    @JsonProperty("ts_ms")
    private Long tsMs;

    @JsonProperty("ts_us")
    private Long tsUs;

    @JsonProperty("ts_ns")
    private Long tsNs;

    // Getters, setters, toString
    public SensorReading getBefore() {
        return before;
    }

    public void setBefore(SensorReading before) {
        this.before = before;
    }

    public SensorReading getAfter() {
        return after;
    }

    public void setAfter(SensorReading after) {
        this.after = after;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public Long getTsMs() {
        return tsMs;
    }

    public void setTsMs(Long tsMs) {
        this.tsMs = tsMs;
    }

    public Long getTsUs() {
        return tsUs;
    }

    public void setTsUs(Long tsUs) {
        this.tsUs = tsUs;
    }

    public Long getTsNs() {
        return tsNs;
    }

    public void setTsNs(Long tsNs) {
        this.tsNs = tsNs;
    }

    @Override
    public String toString() {
        return "DebeziumPayload{" +
                "before=" + before +
                ", after=" + after +
                ", source=" + source +
                ", transaction=" + transaction +
                ", op='" + op + '\'' +
                ", tsMs=" + tsMs +
                ", tsUs=" + tsUs +
                ", tsNs=" + tsNs +
                '}';
    }
}

class Source {
    private String version;
    private String connector;
    private String name;

    @JsonProperty("ts_ms")
    private Long tsMs;
    private String snapshot;
    private String db;
    private String sequence;

    @JsonProperty("ts_us")
    private Long tsUs;

    @JsonProperty("ts_ns")
    private Long tsNs;
    private String schema;
    private String table;

    @JsonProperty("txId")
    private Long txId;
    private Long lsn;
    private Long xmin;

    // Getters, setters, toString
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTsMs() {
        return tsMs;
    }

    public void setTsMs(Long tsMs) {
        this.tsMs = tsMs;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public Long getTsUs() {
        return tsUs;
    }

    public void setTsUs(Long tsUs) {
        this.tsUs = tsUs;
    }

    public Long getTsNs() {
        return tsNs;
    }

    public void setTsNs(Long tsNs) {
        this.tsNs = tsNs;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Long getTxId() {
        return txId;
    }

    public void setTxId(Long txId) {
        this.txId = txId;
    }

    public Long getLsn() {
        return lsn;
    }

    public void setLsn(Long lsn) {
        this.lsn = lsn;
    }

    public Long getXmin() {
        return xmin;
    }

    public void setXmin(Long xmin) {
        this.xmin = xmin;
    }

    @Override
    public String toString() {
        return "Source{" +
                "version='" + version + '\'' +
                ", connector='" + connector + '\'' +
                ", name='" + name + '\'' +
                ", tsMs=" + tsMs +
                ", snapshot='" + snapshot + '\'' +
                ", db='" + db + '\'' +
                ", sequence='" + sequence + '\'' +
                ", tsUs=" + tsUs +
                ", tsNs=" + tsNs +
                ", schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                ", txId=" + txId +
                ", lsn=" + lsn +
                ", xmin=" + xmin +
                '}';
    }
}

class Transaction {
    private String id;

    @JsonProperty("total_order")
    private Long totalOrder;

    @JsonProperty("data_collection_order")
    private Long dataCollectionOrder;

    // Getters, setters, toString
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTotalOrder() {
        return totalOrder;
    }

    public void setTotalOrder(Long totalOrder) {
        this.totalOrder = totalOrder;
    }

    public Long getDataCollectionOrder() {
        return dataCollectionOrder;
    }

    public void setDataCollectionOrder(Long dataCollectionOrder) {
        this.dataCollectionOrder = dataCollectionOrder;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", totalOrder=" + totalOrder +
                ", dataCollectionOrder=" + dataCollectionOrder +
                '}';
    }
}