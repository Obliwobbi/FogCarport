-- ============================================
-- Fog Carport Database Schema
-- ============================================

-- Drop tables in reverse order of dependencies to avoid foreign key conflicts
DROP TABLE IF EXISTS materials_lines CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS bills_of_materials CASCADE;
DROP TABLE IF EXISTS drawings CASCADE;
DROP TABLE IF EXISTS carports CASCADE;
DROP TABLE IF EXISTS materials CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS employees CASCADE;

-- ============================================
-- Create Tables
-- ============================================

-- Employees Table
CREATE TABLE employees
(
    employee_id SERIAL PRIMARY KEY,
    name        VARCHAR(100)        NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    phone       VARCHAR(20),
    is_admin    BOOLEAN DEFAULT FALSE
);

-- Customers Table
CREATE TABLE customers
(
    customer_id  SERIAL PRIMARY KEY,
    firstname    VARCHAR(100)        NOT NULL,
    lastname     VARCHAR(100)        NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    phone        VARCHAR(20),
    street       VARCHAR(100),
    house_number VARCHAR(10),
    zipcode      INT,
    city         VARCHAR(100)
);

-- Materials Table
CREATE TABLE materials
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100)   NOT NULL,
    description     VARCHAR(255),
    unit            INT            NOT NULL,
    unit_type       VARCHAR(50)    NOT NULL,
    material_length DECIMAL(10, 2),
    material_width  DECIMAL(10, 2),
    material_height DECIMAL(10, 2),
    price           DECIMAL(10, 2) NOT NULL
);

-- Carports Table
CREATE TABLE carports
(
    carport_id      SERIAL PRIMARY KEY,
    width           DECIMAL(10, 2) NOT NULL,
    length          DECIMAL(10, 2) NOT NULL,
    height          DECIMAL(10, 2) NOT NULL,
    with_shed       BOOLEAN DEFAULT FALSE,
    shed_width      DECIMAL(10, 2),
    shed_length     DECIMAL(10, 2),
    customer_wishes VARCHAR(250)
);

-- Drawings Table
CREATE TABLE drawings
(
    drawing_id   SERIAL PRIMARY KEY,
    drawing_data TEXT      NOT NULL,
    accepted     BOOLEAN            DEFAULT FALSE,
    );

-- Bills of Materials Table
CREATE TABLE bills_of_materials
(
    bom_id       SERIAL PRIMARY KEY,
    total_price  DECIMAL(12, 2) NOT NULL
);

-- Orders Table
CREATE TABLE orders
(
    order_id      SERIAL PRIMARY KEY,
    order_date    TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT now(),
    status        VARCHAR(50) NOT NULL DEFAULT 'AFVENTER ACCEPT',
    delivery_date TIMESTAMP WITH TIME ZONE,
    drawing_id    INT,
    carport_id    INT         NOT NULL,
    bom_id        INT,
    customer_id   INT         NOT NULL,
    CONSTRAINT fk_drawing FOREIGN KEY (drawing_id) REFERENCES drawings (drawing_id) ON DELETE SET NULL,
    CONSTRAINT fk_carport FOREIGN KEY (carport_id) REFERENCES carports (carport_id) ON DELETE CASCADE,
    CONSTRAINT fk_bom FOREIGN KEY (bom_id) REFERENCES bills_of_materials (bom_id) ON DELETE SET NULL
);

-- Materials Lines Table (junction table between BOM and Materials)
CREATE TABLE materials_lines
(
    line_id       SERIAL PRIMARY KEY,
    bom_id        INT            NOT NULL,
    material_id   INT NULL,
    material_name VARCHAR(100)   NOT NULL,
    unit_type     VARCHAR(50)    NOT NULL,
    quantity      INT            NOT NULL,
    unit_price    DECIMAL(10, 2) NOT NULL,
    line_price    DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_bom FOREIGN KEY (bom_id) REFERENCES bills_of_materials (bom_id) ON DELETE CASCADE,
    CONSTRAINT fk_material FOREIGN KEY (material_id) REFERENCES materials (id) ON DELETE SET NULL
);

-- ============================================
-- Create Indexes for Performance
-- ============================================

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_order_date ON orders (order_date);
CREATE INDEX idx_orders_drawing ON orders (drawing_id);
CREATE INDEX idx_orders_carport ON orders (carport_id);
CREATE INDEX idx_orders_bom ON orders (bom_id);
CREATE INDEX idx_materials_lines_bom ON materials_lines (bom_id);
CREATE INDEX idx_materials_lines_material ON materials_lines (material_id);
CREATE INDEX idx_customers_email ON customers (email);
CREATE INDEX idx_customers_zipcode ON customers (zipcode);
CREATE INDEX idx_employees_email ON employees (email);
CREATE INDEX idx_materials_name ON materials (name);

-- ============================================
-- Sample Data (Optional - Comment out if not needed)
-- ============================================

-- Insert sample employees
INSERT INTO employees (name, email, phone, is_admin)
VALUES ('Admin User', 'admin@fogcarport.dk', '+45 12345678', TRUE),
       ('Sales Person', 'sales@fogcarport.dk', '+45 23456789', FALSE);

-- Insert sample materials (REPLACE WITH YOUR ACTUAL MATERIALS!)
-- Note: unit = quantity per unit (e.g., 1 for single items, 200 for a pack of 200 screws)
-- Dimensions are in cm where applicable, NULL for items without dimensions
INSERT INTO materials (name, description, unit, unit_type, material_length, material_width, material_height, price)
VALUES ('Brædt 25x200', '25x200 mm. trykimp. Brædt', 1, 'stk', 540.00, 20.00, 2.50, 300.00),
       ('Brædt 25x125', '25x125 mm. trykimp. Brædt', 1, 'stk', 360.00, 12.50, 2.50, 200.00),
       ('Lægte 38x73', '38x73 mm. Lægte ubh.', 1, 'stk', 420.00, 7.30, 3.80, 150.00),
       ('Reglar 45x95', '45x95 mm. Reglar ub.', 1, 'stk', 480.00, 9.50, 4.50, 250.00),
       ('Stolpe 97x97', '97x97 mm. trykimp. Stolpe', 1, 'stk', 300.00, 9.70, 9.70, 400.00),
       ('Bundskruer', 'Plastmo bundskruer 200 stk.', 200, 'pakke', NULL, NULL, NULL, 150.00),
       ('Hulbånd', 'Hulbånd 1x20 mm. 10 meter', 1, 'rulle', 1000.00, 2.00, 0.10, 75.00),
       ('Universal højre', 'Universal 190 mm højre', 1, 'stk', 19.00, 4.00, 0.50, 15.00),
       ('Universal venstre', 'Universal 190 mm venstre', 1, 'stk', 19.00, 4.00, 0.50, 15.00),
       ('Skruer 4.5x60', '4,5 x 60 mm. skruer 200 stk.', 200, 'pakke', NULL, NULL, NULL, 120.00),
       ('Beslagskruer', '4,0 x 50 mm. beslagskruer 250 stk.', 250, 'pakke', NULL, NULL, NULL, 100.00),
       ('Bræddebolt', 'Bræddebolt 10 x 120 mm.', 1, 'stk', 12.00, 1.00, 1.00, 5.00),
       ('Firkantskiver', 'Firkantskiver 40x40x11mm', 1, 'stk', NULL, 4.00, 1.10, 3.00),
       ('Tagplade', 'Plastmo Ecolite blåtonet', 1, 'stk', 600.00, 109.00, 0.50, 450.00);