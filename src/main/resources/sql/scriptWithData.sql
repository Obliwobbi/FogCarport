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
    drawing_data TEXT NOT NULL,
    accepted     BOOLEAN DEFAULT FALSE
);

-- Bills of Materials Table
CREATE TABLE bills_of_materials
(
    bom_id      SERIAL PRIMARY KEY,
    total_price DECIMAL(12, 2) NOT NULL
);

-- Orders Table
CREATE TABLE orders
(
    order_id      SERIAL PRIMARY KEY,
    order_date    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    status        VARCHAR(50)              NOT NULL DEFAULT 'NY ORDRE',
    delivery_date TIMESTAMP WITH TIME ZONE,
    drawing_id    INT,
    carport_id    INT                      NOT NULL,
    bom_id        INT,
    customer_id   INT                      NOT NULL,
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
-- ============================================
-- Comprehensive Test Data with All Order Statuses
-- ============================================

-- Insert test customers
INSERT INTO customers (firstname, lastname, email, phone, street, house_number, zipcode, city)
VALUES ('Anders', 'Andersen', 'anders@example.com', '+45 12345678', 'Hovedgaden', '10', 2000, 'Frederiksberg'),
       ('Bente', 'Bentsen', 'bente@example.com', '+45 23456789', 'Vestergade', '25', 8000, 'Aarhus'),
       ('Christian', 'Christensen', 'christian@example.com', '+45 34567890', 'Østergade', '5', 5000, 'Odense'),
       ('Dorte', 'Dahl', 'dorte@example.com', '+45 45678901', 'Nørregade', '15', 1000, 'København'),
       ('Erik', 'Eriksen', 'erik@example.com', '+45 56789012', 'Søndergade', '30', 9000, 'Aalborg');

-- Insert test carports
INSERT INTO carports (width, length, height, with_shed, shed_width, shed_length, customer_wishes)
VALUES (600, 780, 210, FALSE, NULL, NULL, 'Standard carport uden skur'),
       (600, 600, 210, TRUE, 300, 210, 'Carport med skur til haveredskaber'),
       (780, 600, 240, TRUE, 300, 240, 'Stor carport med skur'),
       (600, 780, 210, FALSE, NULL, NULL, 'Ønsker sort carport'),
       (780, 780, 240, TRUE, 400, 240, 'Ekstra stort skur til værktøj');

-- Insert test drawings
INSERT INTO drawings (drawing_data, accepted)
VALUES ('SVG drawing data for standard carport 600x780...', TRUE),
       ('SVG drawing data for carport with shed 600x600...', TRUE),
       ('SVG drawing data for large carport 780x600...', TRUE),
       ('SVG drawing data for black carport 600x780...', FALSE),
       ('SVG drawing data for extra large shed 780x780...', TRUE);

-- Insert test bills of materials
INSERT INTO bills_of_materials (total_price)
VALUES (15000.00),
       (22000.00),
       (28000.00),
       (18000.00),
       (32000.00);

-- Insert test orders with all 5 status types
INSERT INTO orders (order_date, status, delivery_date, drawing_id, carport_id, bom_id, customer_id)
VALUES
    -- NY ORDRE: Brand new order, just created
    ('2024-01-20 09:00:00', 'NY ORDRE', '2024-02-20 10:00:00', 4, 4, NULL, 4),

    -- AFVENTER ACCEPT: Waiting for customer approval
    ('2024-01-15 10:30:00', 'AFVENTER ACCEPT', '2024-02-15 10:00:00', 1, 1, 1, 1),

    -- BETALT: Customer has paid, ready for production
    ('2024-01-10 14:20:00', 'BETALT', '2024-02-10 12:00:00', 2, 2, 2, 2),

    -- AFSENDT: Order has been shipped
    ('2024-01-05 09:15:00', 'AFSENDT', '2024-02-01 08:00:00', 3, 3, 3, 3),

    -- AFSLUTTET: Order completed and delivered
    ('2023-12-20 11:00:00', 'AFSLUTTET', '2024-01-20 14:00:00', 5, 5, 5, 5);

-- Insert material lines for the BOMs
INSERT INTO materials_lines (bom_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
VALUES
    -- BOM 1 (Order 2 - AFVENTER ACCEPT)
    (1, 1, 'Brædt 25x200', 'stk', 10, 300.00, 3000.00),
    (1, 4, 'Reglar 45x95', 'stk', 8, 250.00, 2000.00),
    (1, 5, 'Stolpe 97x97', 'stk', 4, 400.00, 1600.00),
    (1, 14, 'Tagplade', 'stk', 15, 450.00, 6750.00),
    (1, 6, 'Bundskruer', 'pakke', 2, 150.00, 300.00),

    -- BOM 2 (Order 3 - BETALT)
    (2, 1, 'Brædt 25x200', 'stk', 12, 300.00, 3600.00),
    (2, 2, 'Brædt 25x125', 'stk', 10, 200.00, 2000.00),
    (2, 4, 'Reglar 45x95', 'stk', 12, 250.00, 3000.00),
    (2, 5, 'Stolpe 97x97', 'stk', 6, 400.00, 2400.00),
    (2, 14, 'Tagplade', 'stk', 20, 450.00, 9000.00),
    (2, 6, 'Bundskruer', 'pakke', 3, 150.00, 450.00),

    -- BOM 3 (Order 4 - AFSENDT)
    (3, 1, 'Brædt 25x200', 'stk', 15, 300.00, 4500.00),
    (3, 2, 'Brædt 25x125', 'stk', 12, 200.00, 2400.00),
    (3, 4, 'Reglar 45x95', 'stk', 15, 250.00, 3750.00),
    (3, 5, 'Stolpe 97x97', 'stk', 8, 400.00, 3200.00),
    (3, 14, 'Tagplade', 'stk', 25, 450.00, 11250.00),
    (3, 10, 'Skruer 4.5x60', 'pakke', 4, 120.00, 480.00),

    -- BOM 4 (if needed for other orders)
    (4, 1, 'Brædt 25x200', 'stk', 11, 300.00, 3300.00),
    (4, 4, 'Reglar 45x95', 'stk', 10, 250.00, 2500.00),
    (4, 5, 'Stolpe 97x97', 'stk', 5, 400.00, 2000.00),
    (4, 14, 'Tagplade', 'stk', 18, 450.00, 8100.00),

    -- BOM 5 (Order 5 - AFSLUTTET)
    (5, 1, 'Brædt 25x200', 'stk', 18, 300.00, 5400.00),
    (5, 2, 'Brædt 25x125', 'stk', 15, 200.00, 3000.00),
    (5, 3, 'Lægte 38x73', 'stk', 20, 150.00, 3000.00),
    (5, 4, 'Reglar 45x95', 'stk', 18, 250.00, 4500.00),
    (5, 5, 'Stolpe 97x97', 'stk', 10, 400.00, 4000.00),
    (5, 14, 'Tagplade', 'stk', 30, 450.00, 13500.00);