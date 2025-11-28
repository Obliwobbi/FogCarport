-- ============================================
-- Fog Carport Database Schema (Without BOM)
-- ============================================

-- Drop tables in reverse order of dependencies to avoid foreign key conflicts
DROP TABLE IF EXISTS materials_lines CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
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

-- Orders Table (now includes total_price from BOM)
CREATE TABLE orders
(
    order_id      SERIAL PRIMARY KEY,
    order_date    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    status        VARCHAR(50)              NOT NULL DEFAULT 'NY ORDRE',
    delivery_date TIMESTAMP WITH TIME ZONE DEFAULT (NOW() + INTERVAL '1 year'),
    drawing_id    INT,
    carport_id    INT                      NOT NULL,
    customer_id   INT                      NOT NULL,
    total_price   DECIMAL(12, 2)           NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_drawing FOREIGN KEY (drawing_id) REFERENCES drawings (drawing_id) ON DELETE SET NULL,
    CONSTRAINT fk_carport FOREIGN KEY (carport_id) REFERENCES carports (carport_id) ON DELETE CASCADE,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE
);

-- Materials Lines Table (now references orders directly)
CREATE TABLE materials_lines
(
    line_id       SERIAL PRIMARY KEY,
    order_id      INT            NOT NULL,
    material_id   INT NULL,
    material_name VARCHAR(100)   NOT NULL,
    unit_type     VARCHAR(50)    NOT NULL,
    quantity      INT            NOT NULL,
    unit_price    DECIMAL(10, 2) NOT NULL,
    line_price    DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_material FOREIGN KEY (material_id) REFERENCES materials (id) ON DELETE SET NULL
);

-- ============================================
-- Create Indexes for Performance
-- ============================================

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_order_date ON orders (order_date);
CREATE INDEX idx_orders_drawing ON orders (drawing_id);
CREATE INDEX idx_orders_carport ON orders (carport_id);
CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_materials_lines_order ON materials_lines (order_id);
CREATE INDEX idx_materials_lines_material ON materials_lines (material_id);
CREATE INDEX idx_customers_email ON customers (email);
CREATE INDEX idx_customers_zipcode ON customers (zipcode);
CREATE INDEX idx_employees_email ON employees (email);
CREATE INDEX idx_materials_name ON materials (name);

-- ============================================
-- Sample Data
-- ============================================

-- Insert sample employees
INSERT INTO employees (name, email, phone, is_admin)
VALUES ('Admin User', 'admin@fogcarport.dk', '+45 12345678', TRUE),
       ('Sales Person', 'sales@fogcarport.dk', '+45 23456789', FALSE);

-- Insert sample materials
INSERT INTO materials (name, description, unit, unit_type, material_length, material_width, material_height, price)
VALUES ('25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 360, 200, 25, 79.00),
       ('25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 540, 200, 25, 118.00),
       ('25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 360, 125, 25, 48.00),
       ('25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 540, 125, 25, 72.00),
       ('38x73 mm Lægte ubeh.', 'Z til bagside af dør', 1, 'stk', 420, 73, 38, 29.00),
       ('45x95 mm Reglar ubh.', 'Løsholter til skur gavle', 1, 'stk', 270, 95, 45, 32.00),
       ('45x95 mm Reglar ubh.', 'Løsholter til skur sider', 1, 'stk', 240, 95, 45, 28.00),
       ('45x195 mm Spærtræ ubh.', 'Remme i sider – sadles ned i stolper', 1, 'stk', 600, 195, 45, 115.00),
       ('45x195 mm Spærtræ ubh.', 'Remme i sider – skur del, deles', 1, 'stk', 480, 195, 45, 96.00),
       ('45x195 mm Spærtræ ubh.', 'Spær, monteres på rem', 1, 'stk', 600, 195, 45, 115.00),
       ('97x97 mm trykimpr. Stolpe', 'Stolper nedgraves 90 cm', 1, 'stk', 300, 97, 97, 129.00),
       ('19x100 mm trykimpr. Brædt', 'Beklædning af skur 1 på 2', 1, 'stk', 210, 100, 19, 18.00),
       ('19x100 mm trykimpr. Brædt', 'Vandbrædt på stern', 1, 'stk', 540, 100, 19, 54.00),
       ('19x100 mm trykimpr. Brædt', 'Vandbrædt på stern', 1, 'stk', 360, 100, 19, 36.00),
       ('Plastmo Ecolite blåtonet', 'Tagplader monteres på spær', 1, 'stk', 600, NULL, NULL, 159.00),
       ('Plastmo Ecolite blåtonet', 'Tagplader monteres på spær', 1, 'stk', 360, NULL, NULL, 109.00),

-- HARDWARE / BESLAG / SKRUER
       ('Plastmo bundskruer', 'Skruer til tagplader', 200, 'pakke', NULL, NULL, NULL, 129.00),
       ('Hulbånd 1x20 mm', 'Til vindkryds på spær', 1, 'rulle', NULL, NULL, NULL, 49.00),
       ('Universal beslag højre 190 mm', 'Til montering af spær på rem', 1, 'stk', NULL, NULL, NULL, 12.00),
       ('Universal beslag venstre 190 mm', 'Til montering af spær på rem', 1, 'stk', NULL, NULL, NULL, 12.00),
       ('Bræddebolt 10x120 mm', 'Til montering af rem på stolper', 1, 'stk', NULL, NULL, NULL, 4.50),
       ('Firkantskiver 40x40x11 mm', 'Til montering af rem på stolper', 1, 'stk', NULL, NULL, NULL, 1.50),
       ('Beslagskruer 4.0x50 mm', 'Til montering af universalbeslag + hulbånd', 250, 'pakke', NULL, NULL, NULL, 39.00),
       ('Skruer 4.5x50 mm', 'Til montering af inderste beklædning', 300, 'pakke', NULL, NULL, NULL, 49.00),
       ('Skruer 4.5x60 mm', 'Til montering af stern & vandbrædt', 200, 'pakke', NULL, NULL, NULL, 45.00),
       ('Skruer 4.5x70 mm', 'Til montering af yderste beklædning', 400, 'pakke', NULL, NULL, NULL, 59.00),
       ('Stalddørsgreb 50x75 mm', 'Lås til skurdør', 1, 'sæt', NULL, NULL, NULL, 89.00),
       ('T-hængsel 390 mm', 'Til skurdør', 1, 'stk', NULL, NULL, NULL, 35.00),
       ('Vinkelbeslag 3 mm', 'Til montering af løsholter i skur', 1, 'stk', NULL, NULL, NULL, 5.00);

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

-- Insert test orders with all 5 status types
INSERT INTO orders (order_date, status, delivery_date, drawing_id, carport_id, customer_id, total_price)
VALUES
    -- NY ORDRE: Brand new order, just created
    ('2024-01-20 09:00:00', 'NY ORDRE', '2024-02-20 10:00:00', 4, 4, 4, 18000.00),

    -- AFVENTER ACCEPT: Waiting for customer approval
    ('2024-01-15 10:30:00', 'AFVENTER ACCEPT', '2024-02-15 10:00:00', 1, 1, 1, 15000.00),

    -- BETALT: Customer has paid, ready for production
    ('2024-01-10 14:20:00', 'BETALT', '2024-02-10 12:00:00', 2, 2, 2, 22000.00),

    -- AFSENDT: Order has been shipped
    ('2024-01-05 09:15:00', 'AFSENDT', '2024-02-01 08:00:00', 3, 3, 3, 28000.00),

    -- AFSLUTTET: Order completed and delivered
    ('2023-12-20 11:00:00', 'AFSLUTTET', '2024-01-20 14:00:00', 5, 5, 5, 32000.00);

-- Insert material lines directly for orders
INSERT INTO materials_lines (order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
VALUES
    -- Order 2 (AFVENTER ACCEPT) - total: 15000.00
    (2, 1, 'Brædt 25x200', 'stk', 10, 300.00, 3000.00),
    (2, 4, 'Reglar 45x95', 'stk', 8, 250.00, 2000.00),
    (2, 5, 'Stolpe 97x97', 'stk', 4, 400.00, 1600.00),
    (2, 14, 'Tagplade', 'stk', 15, 450.00, 6750.00),
    (2, 6, 'Bundskruer', 'pakke', 2, 150.00, 300.00),
    (2, 10, 'Skruer 4.5x60', 'pakke', 3, 120.00, 360.00),

    -- Order 3 (BETALT) - total: 22000.00
    (3, 1, 'Brædt 25x200', 'stk', 12, 300.00, 3600.00),
    (3, 2, 'Brædt 25x125', 'stk', 10, 200.00, 2000.00),
    (3, 4, 'Reglar 45x95', 'stk', 12, 250.00, 3000.00),
    (3, 5, 'Stolpe 97x97', 'stk', 6, 400.00, 2400.00),
    (3, 14, 'Tagplade', 'stk', 20, 450.00, 9000.00),
    (3, 6, 'Bundskruer', 'pakke', 3, 150.00, 450.00),
    (3, 10, 'Skruer 4.5x60', 'pakke', 5, 120.00, 600.00),

    -- Order 4 (AFSENDT) - total: 28000.00
    (4, 1, 'Brædt 25x200', 'stk', 15, 300.00, 4500.00),
    (4, 2, 'Brædt 25x125', 'stk', 12, 200.00, 2400.00),
    (4, 4, 'Reglar 45x95', 'stk', 15, 250.00, 3750.00),
    (4, 5, 'Stolpe 97x97', 'stk', 8, 400.00, 3200.00),
    (4, 14, 'Tagplade', 'stk', 25, 450.00, 11250.00),
    (4, 10, 'Skruer 4.5x60', 'pakke', 4, 120.00, 480.00),
    (4, 6, 'Bundskruer', 'pakke', 4, 150.00, 600.00),
    (4, 7, 'Hulbånd', 'rulle', 3, 75.00, 225.00),

    -- Order 1 (NY ORDRE) - total: 18000.00
    (1, 1, 'Brædt 25x200', 'stk', 11, 300.00, 3300.00),
    (1, 4, 'Reglar 45x95', 'stk', 10, 250.00, 2500.00),
    (1, 5, 'Stolpe 97x97', 'stk', 5, 400.00, 2000.00),
    (1, 14, 'Tagplade', 'stk', 18, 450.00, 8100.00),
    (1, 6, 'Bundskruer', 'pakke', 3, 150.00, 450.00),
    (1, 10, 'Skruer 4.5x60', 'pakke', 5, 120.00, 600.00),

    -- Order 5 (AFSLUTTET) - total: 32000.00
    (5, 1, 'Brædt 25x200', 'stk', 18, 300.00, 5400.00),
    (5, 2, 'Brædt 25x125', 'stk', 15, 200.00, 3000.00),
    (5, 3, 'Lægte 38x73', 'stk', 20, 150.00, 3000.00),
    (5, 4, 'Reglar 45x95', 'stk', 18, 250.00, 4500.00),
    (5, 5, 'Stolpe 97x97', 'stk', 10, 400.00, 4000.00),
    (5, 14, 'Tagplade', 'stk', 30, 450.00, 13500.00),
    (5, 6, 'Bundskruer', 'pakke', 5, 150.00, 750.00),
    (5, 10, 'Skruer 4.5x60', 'pakke', 6, 120.00, 720.00);