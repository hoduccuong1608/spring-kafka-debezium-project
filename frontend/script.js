const tempCtx = document.getElementById('tempChart').getContext('2d');
const humidityCtx = document.getElementById('humidityChart').getContext('2d');

const tempChart = new Chart(tempCtx, {
    type: 'line',
    data: {
        datasets: [
            { label: 'Room A', data: [], borderColor: 'red', fill: false },
            { label: 'Room B', data: [], borderColor: 'blue', fill: false },
            { label: 'Room C', data: [], borderColor: 'green', fill: false }
        ]
    },
    options: {
        scales: {
            x: {
                type: 'time',
                time: {
                    unit: 'second',
                    tooltipFormat: 'yyyy-MM-dd HH:mm:ss', // Định dạng cho tooltip
                    displayFormats: {
                        second: 'HH:mm:ss' // Định dạng hiển thị trên trục x
                    }
                }
            },
            y: { beginAtZero: false, min: 15, max: 40 }
        }
    }
});

const humidityChart = new Chart(humidityCtx, {
    type: 'line',
    data: {
        datasets: [
            { label: 'Room A', data: [], borderColor: 'red', fill: false },
            { label: 'Room B', data: [], borderColor: 'blue', fill: false },
            { label: 'Room C', data: [], borderColor: 'green', fill: false }
        ]
    },
    options: {
        scales: {
            x: {
                type: 'time',
                time: {
                    unit: 'second',
                    tooltipFormat: 'yyyy-MM-dd HH:mm:ss', // Định dạng cho tooltip
                    displayFormats: {
                        second: 'HH:mm:ss' // Định dạng hiển thị trên trục x
                    }
                }
            },
            y: { beginAtZero: false, min: 20, max: 90 }
        }
    }
});

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/sensors', function (message) {
        const reading = JSON.parse(message.body);
        updateCharts(reading);
    });
});

function updateCharts(reading) {
    const locationIndex = { 'Room A': 0, 'Room B': 1, 'Room C': 2 };
    const idx = locationIndex[reading.location];

    // Cập nhật nhiệt độ
    tempChart.data.datasets[idx].data.push({
        x: new Date(reading.timestamp),
        y: reading.temperature
    });
    if (tempChart.data.datasets[idx].data.length > 100) {
        tempChart.data.datasets[idx].data.shift();
    }

    // Cập nhật độ ẩm
    humidityChart.data.datasets[idx].data.push({
        x: new Date(reading.timestamp),
        y: reading.humidity
    });
    if (humidityChart.data.datasets[idx].data.length > 100) {
        humidityChart.data.datasets[idx].data.shift();
    }

    tempChart.update();
    humidityChart.update();
}