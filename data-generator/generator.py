import psycopg2
import time
import random
import os
from datetime import datetime

# Kết nối PostgreSQL
conn = None
while conn is None:
    try:
        conn = psycopg2.connect(
            host=os.getenv("POSTGRES_HOST", "postgres"),
            port=5432,
            database="sensor_db",
            user="admin",
            password="password"
        )
    except Exception as e:
        print(f"Failed to connect to PostgreSQL: {e}")
        time.sleep(5)

cur = conn.cursor()

# Danh sách giả lập
sensors = [f"SENSOR{i:03d}" for i in range(1, 6)]
locations = ["Room A", "Room B", "Room C"]

# Tạo dữ liệu giả lập
while True:
    try:
        sensor_id = random.choice(sensors)
        location = random.choice(locations)
        temperature = round(random.uniform(20, 35), 1)
        humidity = round(random.uniform(30, 80), 1)
        timestamp = datetime.utcnow().isoformat() + 'Z'

        query = """
        INSERT INTO sensor_readings (sensor_id, location, temperature, humidity, timestamp)
        VALUES (%s, %s, %s, %s, %s)
        """
        cur.execute(query, (sensor_id, location, temperature, humidity, timestamp))
        conn.commit()
        print(f"Inserted reading: sensor_id={sensor_id}, location={location}, temp={temperature}, humidity={humidity}")
        time.sleep(0.1)  # 10 bản ghi mỗi giây
    except Exception as e:
        print(f"Error inserting data: {e}")
        conn.rollback()
        time.sleep(1)