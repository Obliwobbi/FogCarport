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
    -- NY ORDRE (Order 1)
    ('2024-01-20 09:00:00', 'NY ORDRE', '2024-02-20 10:00:00', 4, 4, 4, 4488.00),

    -- AFVENTER ACCEPT (Order 2)
    ('2024-01-15 10:30:00', 'AFVENTER ACCEPT', '2024-02-15 10:00:00', 1, 1, 1, 4330.00),

    -- BETALT (Order 3)
    ('2024-01-10 14:20:00', 'BETALT', '2024-02-10 12:00:00', 2, 2, 2, 7965.00),

    -- AFSENDT (Order 4)
    ('2024-01-05 09:15:00', 'AFSENDT', '2024-02-01 08:00:00', 3, 3, 3, 6935.00),

    -- AFSLUTTET (Order 5)
    ('2023-12-20 11:00:00', 'AFSLUTTET', '2024-01-20 14:00:00', 5, 5, 5, 9493.00);

-- Insert material lines directly for orders
INSERT INTO materials_lines (order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
VALUES
    -- Order 2 (AFVENTER ACCEPT)
    (2, 1, 'Brædt 25x200', 'stk', 10, 79.00, 790.00),
    (2, 4, 'Reglar 45x95', 'stk', 8, 32.00, 256.00),
    (2, 11, 'Stolpe 97x97', 'stk', 4, 129.00, 516.00),
    (2, 15, 'Plastmo Ecolite 600', 'stk', 15, 159.00, 2385.00),
    (2, 17, 'Bundskruer', 'pakke', 2, 129.00, 258.00),
    (2, 25, 'Skruer 4.5x60', 'pakke', 3, 45.00, 135.00),

    -- Order 3 (BETALT)
    (3, 1, 'Brædt 25x200', 'stk', 12, 79.00, 948.00),
    (3, 3, 'Brædt 25x125', 'stk', 10, 48.00, 480.00),
    (3, 4, 'Reglar 45x95', 'stk', 12, 32.00, 384.00),
    (3, 11, 'Stolpe 97x97', 'stk', 6, 129.00, 774.00),
    (3, 15, 'Plastmo Ecolite 600', 'stk', 20, 159.00, 3180.00),
    (3, 17, 'Bundskruer', 'pakke', 3, 129.00, 387.00),
    (3, 25, 'Skruer 4.5x60', 'pakke', 5, 45.00, 225.00),

    -- Order 4 (AFSENDT)
    (4, 1, 'Brædt 25x200', 'stk', 15, 79.00, 1185.00),
    (4, 3, 'Brædt 25x125', 'stk', 12, 48.00, 576.00),
    (4, 4, 'Reglar 45x95', 'stk', 15, 32.00, 480.00),
    (4, 11, 'Stolpe 97x97', 'stk', 8, 129.00, 1032.00),
    (4, 15, 'Plastmo Ecolite 600', 'stk', 25, 159.00, 3975.00),
    (4, 25, 'Skruer 4.5x60', 'pakke', 4, 45.00, 180.00),
    (4, 17, 'Bundskruer', 'pakke', 4, 129.00, 516.00),
    (4, 18, 'Hulbånd 1x20 mm', 'rulle', 3, 49.00, 147.00),

    -- Order 1 (NY ORDRE)
    (1, 1, 'Brædt 25x200', 'stk', 11, 79.00, 869.00),
    (1, 4, 'Reglar 45x95', 'stk', 10, 32.00, 320.00),
    (1, 11, 'Stolpe 97x97', 'stk', 5, 129.00, 645.00),
    (1, 15, 'Plastmo Ecolite 600', 'stk', 18, 159.00, 2862.00),
    (1, 17, 'Bundskruer', 'pakke', 3, 129.00, 387.00),
    (1, 25, 'Skruer 4.5x60', 'pakke', 5, 45.00, 225.00),

    -- Order 5 (AFSLUTTET)
    (5, 1, 'Brædt 25x200', 'stk', 18, 79.00, 1422.00),
    (5, 3, 'Brædt 25x125', 'stk', 15, 48.00, 720.00),
    (5, 5, 'Lægte 38x73', 'stk', 20, 29.00, 580.00),
    (5, 4, 'Reglar 45x95', 'stk', 18, 32.00, 576.00),
    (5, 11, 'Stolpe 97x97', 'stk', 10, 129.00, 1290.00),
    (5, 15, 'Plastmo Ecolite 600', 'stk', 30, 159.00, 4770.00),
    (5, 17, 'Bundskruer', 'pakke', 5, 129.00, 645.00),
    (5, 25, 'Skruer 4.5x60', 'pakke', 6, 45.00, 270.00);
