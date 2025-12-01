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
    private static final String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
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
            // Insert test materials matching SQL script
            stmt.execute("INSERT INTO materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price) VALUES " +
                    "(1, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 360, 200, 25, 79.00)," +
                    "(2, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 540, 200, 25, 118.00)," +
                    "(3, '25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 360, 125, 25, 48.00)," +
                    "(4, '25x125 mm trykimpr. Brædt', 'Oversternbrædder', 1, 'stk', 540, 125, 25, 72.00)," +
                    "(5, '38x73 mm Lægte ubeh.', 'Z til bagside af dør', 1, 'stk', 420, 73, 38, 29.00)," +
                    "(6, '45x95 mm Reglar ubh.', 'Løsholter til skur gavle', 1, 'stk', 270, 95, 45, 32.00)," +
                    "(7, '45x95 mm Reglar ubh.', 'Løsholter til skur sider', 1, 'stk', 240, 95, 45, 28.00)," +
                    "(8, '45x195 mm Spærtræ ubh.', 'Remme i sider', 1, 'stk', 600, 195, 45, 115.00)," +
                    "(9, '45x195 mm Spærtræ ubh.', 'Remme i sider – skur del', 1, 'stk', 480, 195, 45, 96.00)," +
                    "(10, '45x195 mm Spærtræ ubh.', 'Spær', 1, 'stk', 600, 195, 45, 115.00)," +
                    "(11, '97x97 mm trykimpr. Stolpe', 'Stolper', 1, 'stk', 300, 97, 97, 129.00)," +
                    "(12, '19x100 mm trykimpr. Brædt', 'Beklædning', 1, 'stk', 210, 100, 19, 18.00)," +
                    "(13, '19x100 mm trykimpr. Brædt', 'Vandbrædt', 1, 'stk', 540, 100, 19, 54.00)," +
                    "(14, '19x100 mm trykimpr. Brædt', 'Vandbrædt', 1, 'stk', 360, 100, 19, 36.00)," +
                    "(15, 'Plastmo Ecolite blåtonet', 'Tagplader', 1, 'stk', 600, NULL, NULL, 159.00)," +
                    "(16, 'Plastmo Ecolite blåtonet', 'Tagplader', 1, 'stk', 360, NULL, NULL, 109.00)," +
                    "(17, 'Plastmo bundskruer', 'Skruer til tagplader', 200, 'pakke', NULL, NULL, NULL, 129.00)," +
                    "(18, 'Hulbånd 1x20 mm', 'Vindkryds', 1, 'rulle', NULL, NULL, NULL, 49.00)," +
                    "(19, 'Universal beslag højre 190 mm', 'Beslag', 1, 'stk', NULL, NULL, NULL, 12.00)," +
                    "(20, 'Universal beslag venstre 190 mm', 'Beslag', 1, 'stk', NULL, NULL, NULL, 12.00)," +
                    "(21, 'Bræddebolt 10x120 mm', 'Bolte', 1, 'stk', NULL, NULL, NULL, 4.50)," +
                    "(22, 'Firkantskiver 40x40x11 mm', 'Skiver', 1, 'stk', NULL, NULL, NULL, 1.50)," +
                    "(23, 'Beslagskruer 4.0x50 mm', 'Skruer', 250, 'pakke', NULL, NULL, NULL, 39.00)," +
                    "(24, 'Skruer 4.5x50 mm', 'Skruer', 300, 'pakke', NULL, NULL, NULL, 49.00)," +
                    "(25, 'Skruer 4.5x60 mm', 'Skruer', 200, 'pakke', NULL, NULL, NULL, 45.00)," +
                    "(26, 'Skruer 4.5x70 mm', 'Skruer', 400, 'pakke', NULL, NULL, NULL, 59.00)," +
                    "(27, 'Stalddørsgreb 50x75 mm', 'Lås', 1, 'sæt', NULL, NULL, NULL, 89.00)," +
                    "(28, 'T-hængsel 390 mm', 'Hængsel', 1, 'stk', NULL, NULL, NULL, 35.00)," +
                    "(29, 'Vinkelbeslag 3 mm', 'Beslag', 1, 'stk', NULL, NULL, NULL, 5.00) " +
                    "ON CONFLICT (id) DO NOTHING");
        }
    }

    @BeforeEach
    void setUp()
    {
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
}
