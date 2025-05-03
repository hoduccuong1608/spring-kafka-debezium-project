-- Create factory table to store factory information
CREATE TABLE factory (
  id SERIAL PRIMARY KEY,
  factory_name VARCHAR(255) NOT NULL UNIQUE -- Unique name of the factory
);

-- Create line table to store production line information, linked to a factory
CREATE TABLE line (
  id SERIAL PRIMARY KEY,
  line_name VARCHAR(255) NOT NULL,
  factory_id INTEGER REFERENCES factory(id),
  CONSTRAINT unique_line_name_factory UNIQUE (line_name, factory_id) -- Ensure line name is unique within a factory
);

-- Create station table to store station information, linked to a line
CREATE TABLE station (
  id SERIAL PRIMARY KEY,
  station_name VARCHAR(255) NOT NULL,
  line_id INTEGER REFERENCES line(id),
  CONSTRAINT unique_station_name_line UNIQUE (station_name, line_id) -- Ensure station name is unique within a line
);

-- Create product table to store product information
CREATE TABLE product (
  id SERIAL PRIMARY KEY,
  product_code VARCHAR(255) NOT NULL UNIQUE, -- Unique product code
  product_name VARCHAR(255) NOT NULL UNIQUE, -- Unique product name
  version VARCHAR(50), -- Product version
  category VARCHAR(100), -- Product category
  standard_production_time BIGINT -- Standard time to produce (in seconds)
);

-- Create equipment table to store equipment information, linked to a station
CREATE TABLE equipment (
  id SERIAL PRIMARY KEY,
  serial_number VARCHAR(255) NOT NULL UNIQUE, -- Unique serial number
  type VARCHAR(100), -- Equipment type
  station_id INTEGER REFERENCES station(id), -- Reference to station
  status VARCHAR(50), -- Current status (e.g., Operational, Down)
  downtime BIGINT -- Total downtime in seconds
);

-- Create worker table to store worker information
CREATE TABLE worker (
  id SERIAL PRIMARY KEY,
  worker_name VARCHAR(255) NOT NULL UNIQUE, -- Unique worker name
  role VARCHAR(100), -- Worker role
  department VARCHAR(100), -- Department
  skill_level VARCHAR(50) -- Skill level (e.g., Junior, Senior)
);

-- Create shift table to store shift information
CREATE TABLE shift (
  id SERIAL PRIMARY KEY,
  shift_code VARCHAR(255) NOT NULL UNIQUE, -- Unique shift code
  start_time TIME, -- Shift start time
  end_time TIME -- Shift end time
);

-- Create error_type table to store error types
CREATE TABLE error_type (
  id SERIAL PRIMARY KEY,
  error_code VARCHAR(255) NOT NULL UNIQUE, -- Unique error code
  description TEXT -- Error description
);

-- Create work_order table to store work order information
CREATE TABLE work_order (
  id SERIAL PRIMARY KEY,
  work_order_name VARCHAR(255) NOT NULL UNIQUE, -- Unique work order name
  product_id INTEGER REFERENCES product(id), -- Reference to product
  quantity_ordered INTEGER NOT NULL, -- Ordered quantity
  start_date TIMESTAMP, -- Start date of work order
  due_date TIMESTAMP, -- Due date of work order
  status VARCHAR(50), -- Status (e.g., In Progress, Completed)
  factory_id INTEGER REFERENCES factory(id) -- Reference to factory
);

-- Create product_log table to store production logs from data-generator (source for CDC)
CREATE TABLE product_log (
  id SERIAL PRIMARY KEY,
  serial_number VARCHAR(255),
  timestamp TIMESTAMP NOT NULL,
  product_name VARCHAR(255),        -- thay vì product_id
  station_name VARCHAR(255),        -- thay vì station_id
  line_name VARCHAR(255),           -- thay vì line_id
  factory_name VARCHAR(255),        -- thay vì factory_id
  equipment_serial VARCHAR(255),
  worker_name VARCHAR(255),         -- thay vì worker_id
  shift_code VARCHAR(255),          -- thay vì shift_id
  error_code VARCHAR(255),          -- giữ nguyên, có thể giữ reference nếu bảng error_type có
  work_order_name VARCHAR(255),     -- thay vì work_order_id
  status VARCHAR(255)
);



-- Create publication for logical replication (used by Debezium to capture changes from product_log)
CREATE PUBLICATION factory_pub FOR TABLE product_log;

-- Insert sample data for factory
INSERT INTO factory (factory_name) VALUES 
  ('Factory A'), 
  ('Factory B');

-- Insert sample data for line
INSERT INTO line (line_name, factory_id) VALUES 
  ('Line 1', 1), 
  ('Line 2', 1), 
  ('Line 3', 2);

-- Insert sample data for station
INSERT INTO station (station_name, line_id) VALUES 
  ('Station 1', 1), 
  ('Station 2', 1), 
  ('Station 3', 2), 
  ('Station 4', 3);

-- Insert sample data for product
INSERT INTO product (product_code, product_name, version, category, standard_production_time) VALUES 
  ('P001', 'Product X', '1.0', 'Electronics', 3600),
  ('P002', 'Product Y', '2.0', 'Automotive', 5400),
  ('P003', 'Product Z', '1.1', 'Consumer Goods', 1800);

-- Insert sample data for equipment
INSERT INTO equipment (serial_number, type, station_id, status, downtime) VALUES 
  ('EQ123', 'Machine', 1, 'Operational', 0),
  ('EQ124', 'Conveyor', 2, 'Operational', 0),
  ('EQ125', 'Robot', 3, 'Down', 7200),
  ('EQ126', 'Machine', 4, 'Operational', 0);

-- Insert sample data for worker
INSERT INTO worker (worker_name, role, department, skill_level) VALUES 
  ('John Doe', 'Operator', 'Production', 'Senior'),
  ('Jane Smith', 'Technician', 'Maintenance', 'Junior'),
  ('Bob Johnson', 'Supervisor', 'Production', 'Senior');

-- Insert sample data for shift
INSERT INTO shift (shift_code, start_time, end_time) VALUES 
  ('SHIFT001', '08:00:00', '16:00:00'),
  ('SHIFT002', '16:00:00', '00:00:00'),
  ('SHIFT003', '00:00:00', '08:00:00');

-- Insert sample data for error_type
INSERT INTO error_type (error_code, description) VALUES 
  ('ERR001', 'Machine Failure'),
  ('ERR002', 'Material Defect'),
  ('ERR003', 'Operator Error');

-- Insert sample data for work_order
INSERT INTO work_order (work_order_name, product_id, quantity_ordered, start_date, due_date, status, factory_id) VALUES 
  ('WO001', 1, 1000, '2025-05-01 00:00:00', '2025-05-31 23:59:59', 'In Progress', 1),
  ('WO002', 2, 500, '2025-05-01 00:00:00', '2025-06-15 23:59:59', 'In Progress', 1),
  ('WO003', 3, 2000, '2025-05-01 00:00:00', '2025-05-20 23:59:59', 'In Progress', 2);

