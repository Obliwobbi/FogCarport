package app.services;

import app.entities.Carport;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.MaterialMapper;
import app.persistence.MaterialsLinesMapper;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDetailsServiceImplTest
{
    private final static String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static OrderDetailsServiceImpl orderDetailsService;

    @BeforeAll
    static void setupClass()
    {
        connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
        MaterialsLinesMapper materialsLinesMapper = new MaterialsLinesMapper(connectionPool);
        MaterialMapper materialMapper = new MaterialMapper(connectionPool);
        CalculatorService calculatorService = new CalculatorServiceImpl();
        orderDetailsService = new OrderDetailsServiceImpl(calculatorService, materialsLinesMapper, materialMapper);

        // Initialize test database with materials
        try (Connection conn = connectionPool.getConnection())
        {
            setupTestDatabase(conn);
        }
        catch (SQLException e)
        {
            fail("Failed to setup test database: " + e.getMessage());
        }
    }

    private static void setupTestDatabase(Connection conn) throws SQLException
    {
        try (Statement stmt = conn.createStatement())
        {
            // Drop existing tables in correct order
            stmt.execute("DROP TABLE IF EXISTS test.materials_lines CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.drawings CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.carports CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.materials CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.customers CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.employees CASCADE");

            // Create tables
            stmt.execute("""
                    CREATE TABLE test.employees
                    (
                        employee_id SERIAL PRIMARY KEY,
                        name        VARCHAR(100)        NOT NULL,
                        email       VARCHAR(100) UNIQUE NOT NULL,
                        phone       VARCHAR(20)
                    );""");

            stmt.execute("""
                    CREATE TABLE test.customers
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
                    );""");

            stmt.execute("""
                    CREATE TABLE test.carports
                    (
                        carport_id      SERIAL PRIMARY KEY,
                        width           DECIMAL(10, 2) NOT NULL,
                        length          DECIMAL(10, 2) NOT NULL,
                        height          DECIMAL(10, 2) NOT NULL,
                        with_shed       BOOLEAN DEFAULT FALSE,
                        shed_width      DECIMAL(10, 2),
                        shed_length     DECIMAL(10, 2),
                        customer_wishes VARCHAR(250)
                    );""");

            stmt.execute("""
                    CREATE TABLE test.drawings
                    (
                        drawing_id   SERIAL PRIMARY KEY,
                        drawing_data TEXT NOT NULL
                    );""");

            stmt.execute("""
                    CREATE TABLE test.orders
                    (
                        order_id      SERIAL PRIMARY KEY,
                        order_date    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                        status        VARCHAR(50)              NOT NULL DEFAULT 'NY ORDRE',
                        delivery_date TIMESTAMP WITH TIME ZONE DEFAULT (NOW() + INTERVAL '1 year'),
                        drawing_id    INT,
                        carport_id    INT                      NOT NULL,
                        customer_id   INT                      NOT NULL,
                        total_price   DECIMAL(12, 2)           NOT NULL DEFAULT 0.00,
                        CONSTRAINT fk_drawing FOREIGN KEY (drawing_id) REFERENCES test.drawings (drawing_id) ON DELETE SET NULL,
                        CONSTRAINT fk_carport FOREIGN KEY (carport_id) REFERENCES test.carports (carport_id) ON DELETE CASCADE,
                        CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES test.customers (customer_id) ON DELETE CASCADE
                    );""");

            stmt.execute("""
                    CREATE TABLE test.materials
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
                    );""");

            stmt.execute("""
                    CREATE TABLE test.materials_lines
                    (
                        line_id       SERIAL PRIMARY KEY,
                        order_id      INT            NOT NULL,
                        material_id   INT NULL,
                        material_name VARCHAR(100)   NOT NULL,
                        unit_type     VARCHAR(50)    NOT NULL,
                        quantity      INT            NOT NULL,
                        unit_price    DECIMAL(10, 2) NOT NULL,
                        line_price    DECIMAL(10, 2) NOT NULL,
                        CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES test.orders(order_id) ON DELETE CASCADE,
                        CONSTRAINT fk_material FOREIGN KEY (material_id) REFERENCES test.materials (id) ON DELETE SET NULL
                    );""");

            // Insert test data
            stmt.execute("""
                    INSERT INTO test.customers (customer_id, firstname, lastname, email, phone, street, house_number, zipcode, city)
                    VALUES (1, 'Anders', 'Andersen', 'anders@example.com', '+45 12345678', 'Hovedgaden', '10', 2000, 'Frederiksberg');
                    """);

            stmt.execute("""
                    INSERT INTO test.employees (name, email, phone)
                    VALUES ('Jesper Person', 'jp@fogcarport.dk', '+45 23456789');
                    """);

            stmt.execute("""
                    INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                    VALUES (1, 600, 780, 210, FALSE, NULL, NULL, 'Standard carport uden skur');
                    """);

            stmt.execute("""
                    INSERT INTO test.drawings (drawing_id, drawing_data)
                    VALUES (1, 'SVG drawing data for standard carport 600x780...');
                    """);

            // Insert materials
            stmt.execute("INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price) VALUES " +
                    "(1, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 360, 20, 2.5, 79.00)," +
                    "(2, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 540, 20, 2.5, 118.00)," +
                    "(3, '25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 360, 12.5, 2.5, 48.00)," +
                    "(4, '25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 540, 12.5, 2.5, 72.00)," +
                    "(5, '38x73 mm Lægte ubeh.', 'Z til bagside af dør', 1, 'stk', 420, 7.3, 3.8, 29.00)," +
                    "(6, '45x95 mm Reglar ubh.', 'Løsholter til skur gavle', 1, 'stk', 270, 9.5, 4.5, 32.00)," +
                    "(7, '45x95 mm Reglar ubh.', 'Løsholter til skur sider', 1, 'stk', 240, 9.5, 4.5, 28.00)," +
                    "(8, '45x195 mm Spærtræ ubh.', 'Remme i sider – sadles ned i stolper', 1, 'stk', 600, 19.5, 4.5, 115.00)," +
                    "(9, '45x195 mm Spærtræ ubh.', 'Remme i sider – skur del, deles', 1, 'stk', 480, 19.5, 4.5, 96.00)," +
                    "(10, '45x195 mm Spærtræ ubh.', 'Spær, monteres på rem', 1, 'stk', 600, 19.5, 4.5, 115.00)," +
                    "(11, '45x195 mm Spærtræ ubh.', 'Spær, monteres på rem', 1, 'stk', 480, 19.5, 4.5, 96.00)," +
                    "(12, '97x97 mm trykimpr. Stolpe', 'Stolper nedgraves 90 cm', 1, 'stk', 300, 9.7, 9.7, 129.00)," +
                    "(13, '19x100 mm trykimpr. Brædt', 'Beklædning af skur 1 på 2', 1, 'stk', 210, 10, 1.9, 18.00)," +
                    "(14, '19x100 mm trykimpr. Brædt', 'Vandbrædt på stern', 1, 'stk', 540, 10, 1.9, 54.00)," +
                    "(15, '19x100 mm trykimpr. Brædt', 'Vandbrædt på stern', 1, 'stk', 360, 10, 1.9, 36.00)," +
                    "(16, 'Plastmo Ecolite blåtonet', 'Tagplader monteres på spær', 1, 'stk', 600, NULL, NULL, 159.00)," +
                    "(17, 'Plastmo Ecolite blåtonet', 'Tagplader monteres på spær', 1, 'stk', 360, NULL, NULL, 109.00)," +
                    "(18, 'Plastmo bundskruer', 'Skruer til tagplader', 200, 'pakke(r)', NULL, NULL, NULL, 129.00)," +
                    "(19, 'Hulbånd 1x20 mm', 'Til vindkryds på spær', 1, 'rulle(r)', NULL, NULL, NULL, 49.00)," +
                    "(20, 'Universal beslag højre 190 mm', 'Til montering af spær på rem', 1, 'stk', NULL, NULL, NULL, 12.00)," +
                    "(21, 'Universal beslag venstre 190 mm', 'Til montering af spær på rem', 1, 'stk', NULL, NULL, NULL, 12.00)," +
                    "(22, 'Bræddebolt 10x120 mm', 'Til montering af rem på stolper', 1, 'stk', NULL, NULL, NULL, 4.50)," +
                    "(23, 'Firkantskiver 40x40x11 mm', 'Til montering af rem på stolper', 1, 'stk', NULL, NULL, NULL, 1.50)," +
                    "(24, 'Beslagskruer 4.0x50 mm', 'Til montering af universalbeslag + hulbånd', 250, 'pakke(r)', NULL, NULL, NULL, 39.00)," +
                    "(25, 'Skruer 4.5x50 mm', 'Til montering af inderste beklædning', 300, 'pakke(r)', NULL, NULL, NULL, 49.00)," +
                    "(26, 'Skruer 4.5x60 mm', 'Til montering af stern & vandbrædt', 200, 'pakke(r)', NULL, NULL, NULL, 45.00)," +
                    "(27, 'Skruer 4.5x70 mm', 'Til montering af yderste beklædning', 400, 'pakke(r)', NULL, NULL, NULL, 59.00)," +
                    "(28, 'Stalddørsgreb 50x75 mm', 'Lås til skurdør', 1, 'sæt', NULL, NULL, NULL, 89.00)," +
                    "(29, 'T-hængsel 390 mm', 'Til skurdør', 1, 'stk', NULL, NULL, NULL, 35.00)," +
                    "(30, 'Vinkelbeslag 3 mm', 'Til montering af løsholter i skur', 1, 'stk', NULL, NULL, NULL, 5.00)");

            stmt.execute("""
                    INSERT INTO test.orders (order_id, order_date, status, delivery_date, drawing_id, carport_id, customer_id, total_price)
                    VALUES (1, '2024-01-15 10:30:00', 'AFVENTER ACCEPT', '2024-02-15 10:00:00', 1, 1, 1, 5000.00);
                    """);
        }
    }


    @BeforeEach
    void setUp() throws SQLException
    {
        try (Connection conn = connectionPool.getConnection();
             Statement stmt = conn.createStatement())
        {
            stmt.execute("DELETE FROM test.materials_lines");
            // Don't delete orders - keep the test order from setupTestDatabase
        }
    }


    @DisplayName("Create material list: Delivered Material: Carport size (6m x 7.8m) + shed (5.3m x 2.1m)")
    @Test
    void createMaterialListDeliveredMaterial() throws DatabaseException
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530, 210, "");

        List<MaterialsLine> materialList = orderDetailsService.createMaterialList(carport);

//        for(MaterialsLine material : materialList)
//        {
//            System.out.println(material.getMaterial().getName() + " | " + material.getQuantity() + " " + material.getMaterial().getUnitType() + " | " + material.getLinePrice());
//        }
        assertNotNull(materialList);
        assertFalse(materialList.isEmpty());
    }

    @DisplayName("Create material list: Carport without shed")
    @Test
    void createMaterialListNoShed() throws DatabaseException
    {
        Carport carport = new Carport(1, 600, 780, 225, false, "");

        List<MaterialsLine> materialList = orderDetailsService.createMaterialList(carport);

//        for(MaterialsLine material : materialList)
//        {
//            System.out.println(material.getMaterial().getName() + " | " + material.getQuantity() + " " + material.getMaterial().getUnitType() + " | " + material.getLinePrice());
//        }
        assertNotNull(materialList);
        assertFalse(materialList.isEmpty());
    }

    @DisplayName("Create order and add MaterialsLine to DB")
    @Test
    void createOrderAndAddMaterialsLine() throws DatabaseException
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530, 210, "");
        boolean actual = orderDetailsService.addMaterialListToOrder(1, carport);

        assertTrue(actual);

    }
}
