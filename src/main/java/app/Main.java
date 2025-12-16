package app;

import app.config.ThymeleafConfig;

import app.controllers.*;
import app.persistence.*;
import app.services.*;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.logging.Logger;

public class Main
{

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=public";
    private static final String DB = "fogcarport";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args)
    {
        // Initializing Javalin and Jetty webserver

        Javalin app = Javalin.create(config ->
        {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates/");
        }).start(7070);


        CarportMapper carportMapper = new CarportMapper(connectionPool);
        OrderMapper orderMapper = new OrderMapper(connectionPool);
        DrawingMapper drawingMapper = new DrawingMapper(connectionPool);
        CustomerMapper customerMapper = new CustomerMapper(connectionPool);
        EmployeeMapper employeeMapper = new EmployeeMapper(connectionPool);
        MaterialMapper materialMapper = new MaterialMapper(connectionPool);
        MaterialsLinesMapper materialsLinesMapper = new MaterialsLinesMapper(connectionPool);

        CalculatorService calculatorService = new CalculatorServiceImpl();
        CarportService carportService = new CarportServiceImpl(carportMapper);
        DrawingService drawingService = new DrawingServiceImpl(drawingMapper);
        CustomerService customerService = new CustomerServiceImpl(customerMapper);
        EmployeeService employeeService = new EmployeeServiceImpl(employeeMapper);
        OrderDetailsService orderDetailsService = new OrderDetailsServiceImpl(calculatorService, materialsLinesMapper, materialMapper);
        OrderService orderService = new OrderServiceImpl(orderMapper, customerMapper, employeeMapper);
        EmailService emailService = new EmailServiceImpl();

        HomeController homeController = new HomeController();
        CarportController carportController = new CarportController(carportService);
        DrawingController drawingController = new DrawingController(drawingService, calculatorService, orderService);
        ContactController contactController = new ContactController(customerService, orderService, drawingService, carportService);
        OrderController orderController = new OrderController(orderService, orderDetailsService, emailService, employeeService, carportService);

        // Routing
        homeController.addRoutes(app);
        carportController.addRoutes(app);
        drawingController.addRoutes(app);
        orderController.addRoutes(app);
        contactController.addRoutes(app);
    }
}