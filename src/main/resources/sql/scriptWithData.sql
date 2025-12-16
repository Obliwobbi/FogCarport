-- ============================================
-- Fog Carport Database Schema
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
    password    VARCHAR(255)        NOT NULL,
    phone       VARCHAR(20)
);

-- Customers Table
CREATE TABLE customers
(
    customer_id  SERIAL PRIMARY KEY,
    firstname    VARCHAR(100)        NOT NULL,
    lastname     VARCHAR(100)        NOT NULL,
    email        VARCHAR(100)        NOT NULL,
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
    drawing_data TEXT NOT NULL
);

-- Orders Table
CREATE TABLE orders
(
    order_id      SERIAL PRIMARY KEY,
    order_date    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    status        VARCHAR(50)              NOT NULL DEFAULT 'NEW',
    delivery_date TIMESTAMP WITH TIME ZONE DEFAULT (NOW() + INTERVAL '1 year'),
    drawing_id    INT,
    carport_id    INT                      NOT NULL,
    customer_id   INT                      NOT NULL,
    total_price   DECIMAL(12, 2)           NOT NULL DEFAULT 0.00,
    employee_id   INT,
    CONSTRAINT fk_drawing FOREIGN KEY (drawing_id) REFERENCES drawings (drawing_id) ON DELETE SET NULL,
    CONSTRAINT fk_carport FOREIGN KEY (carport_id) REFERENCES carports (carport_id) ON DELETE CASCADE,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES employees (employee_id) ON DELETE CASCADE
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
INSERT INTO employees (name, email, password, phone)
VALUES ('Jesper Person', 'jp@fogcarport.dk', '$2a$12$1vWgGmcO1F0M00OCx2rk9OFEC2Iq3TDbQR7BT01.VyoMZ/2IDVOEi', '+45 23456789'),
       ('Toby Person', 'tp@fogcarport.dk', '$2a$12$9czTxMIL0LOE8blHpzBbl.D/j/hHb1JOLhMXtEQw/.QcvV3zLQ9SO','+45 23456790');

-- Insert sample materials
INSERT INTO materials (name, description, unit, unit_type, material_length, material_width, material_height, price)
VALUES ('25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 360, 20, 2.5, 79.00),
       ('25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 540, 20, 2.5, 118.00),
       ('25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 360, 12.5, 2.5, 48.00),
       ('25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 540, 12.5, 2.5, 72.00),
       ('38x73 mm Lægte ubeh.', 'Z til bagside af dør', 1, 'stk', 420, 7.3, 3.8, 29.00),
       ('45x95 mm Reglar ubh.', 'Løsholter til skur gavle', 1, 'stk', 270, 9.5, 4.5, 32.00),
       ('45x95 mm Reglar ubh.', 'Løsholter til skur sider', 1, 'stk', 240, 9.5, 4.5, 28.00),
       ('45x195 mm Spærtræ ubh.', 'Remme i sider – sadles ned i stolper', 1, 'stk', 600, 19.5, 4.5, 115.00),
       ('45x195 mm Spærtræ ubh.', 'Remme i sider – skur del, deles', 1, 'stk', 480, 19.5, 4.5, 96.00),
       ('45x195 mm Spærtræ ubh.', 'Spær, monteres på rem', 1, 'stk', 600, 19.5, 4.5, 115.00),
       ('45x195 mm Spærtræ ubh.', 'Spær, monteres på rem', 1, 'stk', 480, 19.5, 4.5, 96.00),
       ('97x97 mm trykimpr. Stolpe', 'Stolper nedgraves 90 cm', 1, 'stk', 300, 9.7, 9.7, 129.00),
       ('19x100 mm trykimpr. Brædt', 'Beklædning af skur 1 på 2', 1, 'stk', 210, 10, 1.9, 18.00),
       ('19x100 mm trykimpr. Brædt', 'Vandbrædt på stern', 1, 'stk', 540, 10, 1.9, 54.00),
       ('19x100 mm trykimpr. Brædt', 'Vandbrædt på stern', 1, 'stk', 360, 10, 1.9, 36.00),
       ('Plastmo Ecolite blåtonet', 'Tagplader monteres på spær', 1, 'stk', 600, NULL, NULL, 159.00),
       ('Plastmo Ecolite blåtonet', 'Tagplader monteres på spær', 1, 'stk', 360, NULL, NULL, 109.00),

-- HARDWARE / BESLAG / SKRUER
       ('Plastmo bundskruer', 'Skruer til tagplader', 200, 'pakke(r)', NULL, NULL, NULL, 129.00),
       ('Hulbånd 1x20 mm', 'Til vindkryds på spær', 1, 'rulle(r)', NULL, NULL, NULL, 49.00),
       ('Universal beslag højre 190 mm', 'Til montering af spær på rem', 1, 'stk', NULL, NULL, NULL, 12.00),
       ('Universal beslag venstre 190 mm', 'Til montering af spær på rem', 1, 'stk', NULL, NULL, NULL, 12.00),
       ('Bræddebolt 10x120 mm', 'Til montering af rem på stolper', 1, 'stk', NULL, NULL, NULL, 4.50),
       ('Firkantskiver 40x40x11 mm', 'Til montering af rem på stolper', 1, 'stk', NULL, NULL, NULL, 1.50),
       ('Beslagskruer 4.0x50 mm', 'Til montering af universalbeslag + hulbånd', 250, 'pakke(r)', NULL, NULL, NULL, 39.00),
       ('Skruer 4.5x50 mm', 'Til montering af inderste beklædning', 300, 'pakke(r)', NULL, NULL, NULL, 49.00),
       ('Skruer 4.5x60 mm', 'Til montering af stern & vandbrædt', 200, 'pakke(r)', NULL, NULL, NULL, 45.00),
       ('Skruer 4.5x70 mm', 'Til montering af yderste beklædning', 400, 'pakke(r)', NULL, NULL, NULL, 59.00),
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
       (600, 780, 240, TRUE, 510, 210, 'Ekstra stort skur til værktøj');

-- Insert test drawings
INSERT INTO drawings (drawing_data)
VALUES ('SVG drawing data for standard carport 600x780...'),
       ('SVG drawing data for carport with shed 600x600...'),
       ('SVG drawing data for large carport 780x600...'),
       ('SVG drawing data for black carport 600x780...'),
       ('SVG drawing data for extra large shed 780x780...');

-- Insert test orders with all 5 status types
INSERT INTO orders (order_date, status, delivery_date, drawing_id, carport_id, customer_id, total_price, employee_id)
VALUES
    -- NY ORDRE (Order 1)
    ('2024-01-20 09:00:00', 'NEW', '2024-02-20 10:00:00', 4, 4, 4, 0.00, NULL),

    -- AFVENTER ACCEPT (Order 2)
    ('2024-01-15 10:30:00', 'PENDING', '2024-02-15 10:00:00', 1, 1, 1, 0.00, 1),

    -- BETALT (Order 3)
    ('2024-01-10 14:20:00', 'PAID', '2024-02-10 12:00:00', 2, 2, 2, 8143.00, 2),

    -- AFSENDT (Order 4)
    ('2024-01-05 09:15:00', 'IN_TRANSIT', '2024-02-01 08:00:00', 3, 3, 3, 8407.00, 2),

    -- AFSLUTTET (Order 5)
    ('2023-12-20 11:00:00', 'DONE', '2024-01-20 14:00:00', 5, 5, 5, 12269.00, 1);

INSERT INTO materials_lines (order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
VALUES
    -- ORDER 2
    (2, 12, '97x97 mm trykimpr. Stolpe', 'stk', 6, 129.00, 774.00),
    (2, 9, '45x195 mm Spærtræ ubh.', 'stk', 4, 96.00, 384.00),
    (2, 22, 'Bræddebolt 10x120 mm', 'stk', 16, 4.50, 72.00),
    (2, 23, 'Firkantskiver 40x40x11 mm', 'stk', 16, 1.50, 24.00),
    (2, 11, '45x195 mm Spærtræ ubh.', 'stk', 15, 96.00, 1440.00),
    (2, 20, 'Universal beslag højre 190 mm', 'stk', 15, 12.00, 180.00),
    (2, 21, 'Universal beslag venstre 190 mm', 'stk', 15, 12.00, 180.00),
    (2, 19, 'Hulbånd 1x20 mm', 'rulle(r)', 2, 49.00, 98.00),
    (2, 2, '25x200 mm trykimpr. Brædt', 'stk', 2, 118.00, 236.00),
    (2, 4, '25x125 mm trykimpr. Brædt', 'stk', 2, 72.00, 144.00),
    (2, 14, '19x100 mm trykimpr. Brædt', 'stk', 2, 54.00, 108.00),
    (2, 1, '25x200 mm trykimpr. Brædt', 'stk', 2, 79.00, 158.00),
    (2, 3, '25x125 mm trykimpr. Brædt', 'stk', 2, 48.00, 96.00),
    (2, 15, '19x100 mm trykimpr. Brædt', 'stk', 2, 36.00, 72.00),
    (2, 1, '25x200 mm trykimpr. Brædt', 'stk', 4, 79.00, 316.00),
    (2, 3, '25x125 mm trykimpr. Brædt', 'stk', 2, 48.00, 96.00),
    (2, 15, '19x100 mm trykimpr. Brædt', 'stk', 2, 36.00, 72.00),
    (2, 16, 'Plastmo Ecolite blåtonet', 'stk', 6, 159.00, 954.00),
    (2, 17, 'Plastmo Ecolite blåtonet', 'stk', 6, 109.00, 654.00),
    (2, 18, 'Plastmo bundskruer', 'pakke(r)', 3, 129.00, 387.00),
    (2, 24, 'Beslagskruer 4.0x50 mm', 'pakke(r)', 1, 39.00, 39.00),

    -- ORDER 3
    (3, 12, '97x97 mm trykimpr. Stolpe', 'stk', 10, 129.00, 1290.00),
    (3, 8, '45x195 mm Spærtræ ubh.', 'stk', 2, 115.00, 230.00),
    (3, 22, 'Bræddebolt 10x120 mm', 'stk', 20, 4.50, 90.00),
    (3, 23, 'Firkantskiver 40x40x11 mm', 'stk', 20, 1.50, 30.00),
    (3, 11, '45x195 mm Spærtræ ubh.', 'stk', 12, 96.00, 1152.00),
    (3, 20, 'Universal beslag højre 190 mm', 'stk', 12, 12.00, 144.00),
    (3, 21, 'Universal beslag venstre 190 mm', 'stk', 12, 12.00, 144.00),
    (3, 19, 'Hulbånd 1x20 mm', 'rulle(r)', 2, 49.00, 98.00),
    (3, 1, '25x200 mm trykimpr. Brædt', 'stk', 4, 79.00, 316.00),
    (3, 3, '25x125 mm trykimpr. Brædt', 'stk', 4, 48.00, 192.00),
    (3, 15, '19x100 mm trykimpr. Brædt', 'stk', 4, 36.00, 144.00),
    (3, 1, '25x200 mm trykimpr. Brædt', 'stk', 4, 79.00, 316.00),
    (3, 3, '25x125 mm trykimpr. Brædt', 'stk', 2, 48.00, 96.00),
    (3, 15, '19x100 mm trykimpr. Brædt', 'stk', 2, 36.00, 72.00),
    (3, 18, 'Plastmo bundskruer', 'pakke(r)', 3, 129.00, 387.00),
    (3, 7, '45x95 mm Reglar ubh.', 'stk', 12, 28.00, 336.00),
    (3, 30, 'Vinkelbeslag 3 mm', 'stk', 24, 5.00, 120.00),
    (3, 13, '19x100 mm trykimpr. Brædt', 'stk', 138, 18.00, 2484.00),
    (3, 25, 'Skruer 4.5x50 mm', 'pakke(r)', 2, 49.00, 98.00),
    (3, 27, 'Skruer 4.5x70 mm', 'pakke(r)', 3, 59.00, 177.00),
    (3, 5, '38x73 mm Lægte ubeh.', 'stk', 1, 29.00, 29.00),
    (3, 28, 'Stalddørsgreb 50x75 mm', 'sæt', 1, 89.00, 89.00),
    (3, 29, 'T-hængsel 390 mm', 'stk', 2, 35.00, 70.00),
    (3, 24, 'Beslagskruer 4.0x50 mm', 'pakke(r)', 1, 39.00, 39.00),

    -- ORDER 4
    (4, 12, '97x97 mm trykimpr. Stolpe', 'stk', 10, 129.00, 1290.00),
    (4, 8, '45x195 mm Spærtræ ubh.', 'stk', 2, 115.00, 230.00),
    (4, 22, 'Bræddebolt 10x120 mm', 'stk', 20, 4.50, 90.00),
    (4, 23, 'Firkantskiver 40x40x11 mm', 'stk', 20, 1.50, 30.00),
    (4, 11, '45x195 mm Spærtræ ubh.', 'stk', 12, 96.00, 1152.00),
    (4, 20, 'Universal beslag højre 190 mm', 'stk', 12, 12.00, 144.00),
    (4, 21, 'Universal beslag venstre 190 mm', 'stk', 12, 12.00, 144.00),
    (4, 19, 'Hulbånd 1x20 mm', 'rulle(r)', 2, 49.00, 98.00),
    (4, 1, '25x200 mm trykimpr. Brædt', 'stk', 4, 79.00, 316.00),
    (4, 3, '25x125 mm trykimpr. Brædt', 'stk', 4, 48.00, 192.00),
    (4, 15, '19x100 mm trykimpr. Brædt', 'stk', 4, 36.00, 144.00),
    (4, 2, '25x200 mm trykimpr. Brædt', 'stk', 2, 118.00, 236.00),
    (4, 4, '25x125 mm trykimpr. Brædt', 'stk', 1, 72.00, 72.00),
    (4, 14, '19x100 mm trykimpr. Brædt', 'stk', 1, 54.00, 54.00),
    (4, 1, '25x200 mm trykimpr. Brædt', 'stk', 2, 79.00, 158.00),
    (4, 3, '25x125 mm trykimpr. Brædt', 'stk', 1, 48.00, 48.00),
    (4, 15, '19x100 mm trykimpr. Brædt', 'stk', 1, 36.00, 36.00),
    (4, 18, 'Plastmo bundskruer', 'pakke(r)', 3, 129.00, 387.00),
    (4, 7, '45x95 mm Reglar ubh.', 'stk', 12, 28.00, 336.00),
    (4, 30, 'Vinkelbeslag 3 mm', 'stk', 24, 5.00, 120.00),
    (4, 13, '19x100 mm trykimpr. Brædt', 'stk', 146, 18.00, 2628.00),
    (4, 25, 'Skruer 4.5x50 mm', 'pakke(r)', 2, 49.00, 98.00),
    (4, 27, 'Skruer 4.5x70 mm', 'pakke(r)', 3, 59.00, 177.00),
    (4, 5, '38x73 mm Lægte ubeh.', 'stk', 1, 29.00, 29.00),
    (4, 28, 'Stalddørsgreb 50x75 mm', 'sæt', 1, 89.00, 89.00),
    (4, 29, 'T-hængsel 390 mm', 'stk', 2, 35.00, 70.00),
    (4, 24, 'Beslagskruer 4.0x50 mm', 'pakke(r)', 1, 39.00, 39.00),

    -- ORDER 5
    (5, 12, '97x97 mm trykimpr. Stolpe', 'stk', 12, 129.00, 1548.00),
    (5, 8, '45x195 mm Spærtræ ubh.', 'stk', 1, 115.00, 115.00),
    (5, 9, '45x195 mm Spærtræ ubh.', 'stk', 3, 96.00, 288.00),
    (5, 22, 'Bræddebolt 10x120 mm', 'stk', 28, 4.50, 126.00),
    (5, 23, 'Firkantskiver 40x40x11 mm', 'stk', 28, 1.50, 42.00),
    (5, 11, '45x195 mm Spærtræ ubh.', 'stk', 15, 96.00, 1440.00),
    (5, 20, 'Universal beslag højre 190 mm', 'stk', 15, 12.00, 180.00),
    (5, 21, 'Universal beslag venstre 190 mm', 'stk', 15, 12.00, 180.00),
    (5, 19, 'Hulbånd 1x20 mm', 'rulle(r)', 2, 49.00, 98.00),
    (5, 2, '25x200 mm trykimpr. Brædt', 'stk', 2, 118.00, 236.00),
    (5, 4, '25x125 mm trykimpr. Brædt', 'stk', 2, 72.00, 144.00),
    (5, 14, '19x100 mm trykimpr. Brædt', 'stk', 2, 54.00, 108.00),
    (5, 1, '25x200 mm trykimpr. Brædt', 'stk', 2, 79.00, 158.00),
    (5, 3, '25x125 mm trykimpr. Brædt', 'stk', 2, 48.00, 96.00),
    (5, 15, '19x100 mm trykimpr. Brædt', 'stk', 2, 36.00, 72.00),
    (5, 1, '25x200 mm trykimpr. Brædt', 'stk', 4, 79.00, 316.00),
    (5, 3, '25x125 mm trykimpr. Brædt', 'stk', 2, 48.00, 96.00),
    (5, 15, '19x100 mm trykimpr. Brædt', 'stk', 2, 36.00, 72.00),
    (5, 16, 'Plastmo Ecolite blåtonet', 'stk', 6, 159.00, 954.00),
    (5, 17, 'Plastmo Ecolite blåtonet', 'stk', 6, 109.00, 654.00),
    (5, 18, 'Plastmo bundskruer', 'pakke(r)', 3, 129.00, 387.00),
    (5, 6, '45x95 mm Reglar ubh.', 'stk', 12, 32.00, 384.00),
    (5, 7, '45x95 mm Reglar ubh.', 'stk', 4, 28.00, 112.00),
    (5, 30, 'Vinkelbeslag 3 mm', 'stk', 32, 5.00, 160.00),
    (5, 13, '19x100 mm trykimpr. Brædt', 'stk', 203, 18.00, 3654.00),
    (5, 25, 'Skruer 4.5x50 mm', 'pakke(r)', 3, 49.00, 147.00),
    (5, 27, 'Skruer 4.5x70 mm', 'pakke(r)', 4, 59.00, 236.00),
    (5, 5, '38x73 mm Lægte ubeh.', 'stk', 1, 29.00, 29.00),
    (5, 28, 'Stalddørsgreb 50x75 mm', 'sæt', 1, 89.00, 89.00),
    (5, 29, 'T-hængsel 390 mm', 'stk', 2, 35.00, 70.00),
    (5, 24, 'Beslagskruer 4.0x50 mm', 'pakke(r)', 2, 39.00, 78.00);


