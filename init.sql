-- Factory table
CREATE TABLE IF NOT EXISTS factory (
  id SERIAL PRIMARY KEY,
  factory_name VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_factory_name ON factory(factory_name);

-- Line table
CREATE TABLE IF NOT EXISTS line (
  id SERIAL PRIMARY KEY,
  line_name VARCHAR(255) NOT NULL,
  factory_id INTEGER REFERENCES factory(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT unique_line_name_factory UNIQUE (line_name, factory_id)
);
CREATE INDEX IF NOT EXISTS idx_line_name_factory_id ON line(line_name, factory_id);

-- Station table
CREATE TABLE IF NOT EXISTS station (
  id SERIAL PRIMARY KEY,
  station_name VARCHAR(255) NOT NULL,
  line_id INTEGER REFERENCES line(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT unique_station_name_line UNIQUE (station_name, line_id)
);
CREATE INDEX IF NOT EXISTS idx_station_name_line_id ON station(station_name, line_id);

-- Product table
CREATE TABLE IF NOT EXISTS product (
  id SERIAL PRIMARY KEY,
  product_code VARCHAR(255) NOT NULL UNIQUE,
  product_name VARCHAR(255) NOT NULL UNIQUE,
  version VARCHAR(50),
  category VARCHAR(100),
  standard_production_time BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_product_name ON product(product_name);

-- Equipment table
CREATE TABLE IF NOT EXISTS equipment (
  id SERIAL PRIMARY KEY,
  serial_number VARCHAR(255) NOT NULL UNIQUE,
  type VARCHAR(100),
  station_id INTEGER REFERENCES station(id),
  status VARCHAR(50),
  downtime BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_equipment_serial_number ON equipment(serial_number);

-- Worker table
CREATE TABLE IF NOT EXISTS worker (
  id SERIAL PRIMARY KEY,
  worker_name VARCHAR(255) NOT NULL UNIQUE,
  role VARCHAR(100),
  department VARCHAR(100),
  skill_level VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_worker_name ON worker(worker_name);

-- Shift table
CREATE TABLE IF NOT EXISTS shift (
  id SERIAL PRIMARY KEY,
  shift_code VARCHAR(255) NOT NULL UNIQUE,
  start_time TIME,
  end_time TIME,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_shift_code ON shift(shift_code);

-- Error_type table
CREATE TABLE IF NOT EXISTS error_type (
  id SERIAL PRIMARY KEY,
  error_code VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_error_code ON error_type(error_code);

-- Work_order table
CREATE TABLE IF NOT EXISTS work_order (
  id SERIAL PRIMARY KEY,
  work_order_name VARCHAR(255) NOT NULL UNIQUE,
  product_id INTEGER REFERENCES product(id),
  quantity_ordered INTEGER NOT NULL,
  start_date TIMESTAMP,
  due_date TIMESTAMP,
  status VARCHAR(50),
  factory_id INTEGER REFERENCES factory(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_work_order_name ON work_order(work_order_name);

-- Product_log table (source for CDC)
CREATE TABLE IF NOT EXISTS product_log (
  id BIGSERIAL PRIMARY KEY,
  serial_number VARCHAR(255),
  timestamp TIMESTAMP NOT NULL,
  product_name VARCHAR(255),
  station_name VARCHAR(255),
  line_name VARCHAR(255),
  factory_name VARCHAR(255),
  equipment_serial VARCHAR(255),
  worker_name VARCHAR(255),
  shift_code VARCHAR(255),
  error_code VARCHAR(255),
  work_order_name VARCHAR(255),
  status VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add created_at column to existing tables if not exists
-- DO $$ 
-- BEGIN
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'factory' AND column_name = 'created_at') THEN
--     ALTER TABLE factory ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'line' AND column_name = 'created_at') THEN
--     ALTER TABLE line ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'station' AND column_name = 'created_at') THEN
--     ALTER TABLE station ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'product' AND column_name = 'created_at') THEN
--     ALTER TABLE product ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'equipment' AND column_name = 'created_at') THEN
--     ALTER TABLE equipment ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'worker' AND column_name = 'created_at') THEN
--     ALTER TABLE worker ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'shift' AND column_name = 'created_at') THEN
--     ALTER TABLE shift ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'error_type' AND column_name = 'created_at') THEN
--     ALTER TABLE error_type ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'created_at') THEN
--     ALTER TABLE work_order ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
--   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'product_log' AND column_name = 'created_at') THEN
--     ALTER TABLE product_log ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--   END IF;
-- END $$;

-- Log and summary tables (unchanged)
CREATE TABLE IF NOT EXISTS production_logs (
    log_id BIGSERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    product_name VARCHAR(100),
    factory_name VARCHAR(100),
    line_name VARCHAR(100),
    station_name VARCHAR(100),
    shift_code VARCHAR(50),
    work_order_name VARCHAR(50),
    status VARCHAR(50),
    operation VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_production_logs_event_timestamp ON production_logs(event_timestamp);
CREATE INDEX IF NOT EXISTS idx_production_logs_factory_name ON production_logs(factory_name);
CREATE INDEX IF NOT EXISTS idx_production_logs_line_name ON production_logs(line_name);

CREATE TABLE IF NOT EXISTS equipment_logs (
    log_id BIGSERIAL PRIMARY KEY,
    equipment_serial VARCHAR(50) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    status VARCHAR(50),
    error_code VARCHAR(50),
    line_name VARCHAR(100),
    station_name VARCHAR(100),
    operation VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_equipment_logs_event_timestamp ON equipment_logs(event_timestamp);
CREATE INDEX IF NOT EXISTS idx_equipment_logs_equipment_serial ON equipment_logs(equipment_serial);

CREATE TABLE IF NOT EXISTS error_logs (
    log_id BIGSERIAL PRIMARY KEY,
    error_code VARCHAR(50) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    product_name VARCHAR(100),
    station_name VARCHAR(100),
    equipment_serial VARCHAR(50),
    worker_name VARCHAR(100),
    shift_code VARCHAR(50),
    operation VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_error_logs_event_timestamp ON error_logs(event_timestamp);
CREATE INDEX IF NOT EXISTS idx_error_logs_error_code ON error_logs(error_code);

CREATE TABLE IF NOT EXISTS worker_logs (
    log_id BIGSERIAL PRIMARY KEY,
    worker_name VARCHAR(100) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    shift_code VARCHAR(50),
    product_name VARCHAR(100),
    error_code VARCHAR(50),
    output_count INT DEFAULT 1,
    operation VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_worker_logs_event_timestamp ON worker_logs(event_timestamp);
CREATE INDEX IF NOT EXISTS idx_worker_logs_worker_name ON worker_logs(worker_name);

CREATE TABLE IF NOT EXISTS production_summary (
    summary_id BIGSERIAL PRIMARY KEY,
    summary_date DATE NOT NULL,
    factory_name VARCHAR(100),
    line_name VARCHAR(100),
    product_name VARCHAR(100),
    shift_code VARCHAR(50),
    work_order_name VARCHAR(50),
    output_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_production_summary_summary_date ON production_summary(summary_date);

CREATE TABLE IF NOT EXISTS equipment_summary (
    summary_id BIGSERIAL PRIMARY KEY,
    summary_date DATE NOT NULL,
    equipment_serial VARCHAR(50),
    running_seconds BIGINT DEFAULT 0,
    stopped_seconds BIGINT DEFAULT 0,
    maintenance_seconds BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_equipment_summary_summary_date ON equipment_summary(summary_date);

CREATE TABLE IF NOT EXISTS error_summary (
    summary_id BIGSERIAL PRIMARY KEY,
    summary_date DATE NOT NULL,
    error_code VARCHAR(50),
    product_name VARCHAR(100),
    station_name VARCHAR(100),
    equipment_serial VARCHAR(50),
    worker_name VARCHAR(100),
    shift_code VARCHAR(50),
    error_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_error_summary_summary_date ON error_summary(summary_date);

CREATE TABLE IF NOT EXISTS worker_summary (
    summary_id BIGSERIAL PRIMARY KEY,
    summary_date DATE NOT NULL,
    worker_name VARCHAR(100),
    shift_code VARCHAR(50),
    output_count BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_worker_summary_summary_date ON worker_summary(summary_date);

-- Publication for logical replication
DO $$ 
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'factory_pub') THEN
    CREATE PUBLICATION factory_pub FOR TABLE product_log;
  END IF;
END $$;

-- Insert sample data for factory
INSERT INTO factory (factory_name) VALUES 
  ('Factory A'), 
  ('Factory B')
ON CONFLICT (factory_name) DO NOTHING;

-- Insert sample data for line
INSERT INTO line (line_name, factory_id) VALUES 
  ('Line 1', (SELECT id FROM factory WHERE factory_name = 'Factory A')), 
  ('Line 2', (SELECT id FROM factory WHERE factory_name = 'Factory A')), 
  ('Line 3', (SELECT id FROM factory WHERE factory_name = 'Factory B'))
ON CONFLICT (line_name, factory_id) DO NOTHING;

-- Insert sample data for station
INSERT INTO station (station_name, line_id) VALUES 
  ('Station 1', (SELECT id FROM line WHERE line_name = 'Line 1' AND factory_id = (SELECT id FROM factory WHERE factory_name = 'Factory A'))), 
  ('Station 2', (SELECT id FROM line WHERE line_name = 'Line 1' AND factory_id = (SELECT id FROM factory WHERE factory_name = 'Factory A'))), 
  ('Station 3', (SELECT id FROM line WHERE line_name = 'Line 2' AND factory_id = (SELECT id FROM factory WHERE factory_name = 'Factory A'))), 
  ('Station 4', (SELECT id FROM line WHERE line_name = 'Line 3' AND factory_id = (SELECT id FROM factory WHERE factory_name = 'Factory B')))
ON CONFLICT (station_name, line_id) DO NOTHING;

-- Insert sample data for product
INSERT INTO product (product_code, product_name, version, category, standard_production_time) VALUES 
  ('P001', 'Product X', '1.0', 'Electronics', 3600),
  ('P002', 'Product Y', '2.0', 'Automotive', 5400),
  ('P003', 'Product Z', '1.1', 'Consumer Goods', 1800)
ON CONFLICT (product_code) DO NOTHING;

-- Insert sample data for equipment
INSERT INTO equipment (serial_number, type, station_id, status, downtime) VALUES 
  ('EQ123', 'Machine', (SELECT id FROM station WHERE station_name = 'Station 1' AND line_id = (SELECT id FROM line WHERE line_name = 'Line 1')), 'Operational', 0),
  ('EQ124', 'Conveyor', (SELECT id FROM station WHERE station_name = 'Station 2' AND line_id = (SELECT id FROM line WHERE line_name = 'Line 1')), 'Operational', 0),
  ('EQ125', 'Robot', (SELECT id FROM station WHERE station_name = 'Station 3' AND line_id = (SELECT id FROM line WHERE line_name = 'Line 2')), 'Down', 7200),
  ('EQ126', 'Machine', (SELECT id FROM station WHERE station_name = 'Station 4' AND line_id = (SELECT id FROM line WHERE line_name = 'Line 3')), 'Operational', 0)
ON CONFLICT (serial_number) DO NOTHING;

-- Insert sample data for worker
INSERT INTO worker (worker_name, role, department, skill_level) VALUES 
  ('John Doe', 'Operator', 'Production', 'Senior'),
  ('Jane Smith', 'Technician', 'Maintenance', 'Junior'),
  ('Bob Johnson', 'Supervisor', 'Production', 'Senior')
ON CONFLICT (worker_name) DO NOTHING;

-- Insert sample data for shift
INSERT INTO shift (shift_code, start_time, end_time) VALUES 
  ('SHIFT001', '08:00:00', '16:00:00'),
  ('SHIFT002', '16:00:00', '00:00:00'),
  ('SHIFT003', '00:00:00', '08:00:00')
ON CONFLICT (shift_code) DO NOTHING;

-- Insert sample data for error_type
INSERT INTO error_type (error_code, description) VALUES 
  ('ERR001', 'Machine Failure'),
  ('ERR002', 'Material Defect'),
  ('ERR003', 'Operator Error')
ON CONFLICT (error_code) DO NOTHING;

-- Insert sample data for work_order
INSERT INTO work_order (work_order_name, product_id, quantity_ordered, start_date, due_date, status, factory_id) VALUES 
  ('WO001', (SELECT id FROM product WHERE product_name = 'Product X'), 1000, '2025-05-01 00:00:00', '2025-05-31 23:59:59', 'In Progress', (SELECT id FROM factory WHERE factory_name = 'Factory A')),
  ('WO002', (SELECT id FROM product WHERE product_name = 'Product Y'), 500, '2025-05-01 00:00:00', '2025-06-15 23:59:59', 'In Progress', (SELECT id FROM factory WHERE factory_name = 'Factory A')),
  ('WO003', (SELECT id FROM product WHERE product_name = 'Product Z'), 2000, '2025-05-01 00:00:00', '2025-05-20 23:59:59', 'In Progress', (SELECT id FROM factory WHERE factory_name = 'Factory B'))
ON CONFLICT (work_order_name) DO NOTHING;

-- Insert sample data for product_log (fixed to match existing data)
INSERT INTO product_log (serial_number, timestamp, product_name, station_name, line_name, factory_name, equipment_serial, worker_name, shift_code, error_code, work_order_name, status) VALUES 
  ('SN001', '2025-05-03 12:00:00', 'Product X', 'Station 1', 'Line 1', 'Factory A', 'EQ123', 'John Doe', 'SHIFT001', NULL, 'WO001', 'Completed'),
  ('SN002', '2025-05-03 12:01:00', 'Product Y', 'Station 2', 'Line 1', 'Factory A', 'EQ124', 'Jane Smith', 'SHIFT001', 'ERR001', 'WO002', 'In Progress'),
  ('SN003', '2025-05-03 12:02:00', 'Product Z', 'Station 3', 'Line 2', 'Factory A', 'EQ125', 'Bob Johnson', 'SHIFT002', NULL, 'WO003', 'Completed')
ON CONFLICT (id) DO NOTHING;