package app.controllers;

import app.entities.Order;
import app.exceptions.DatabaseException;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class OrderController
{
    private final OrderService orderService;

    public OrderController(OrderService orderService)
    {
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/orders", this::showOrders);
    }

    private void showOrders(Context ctx)
    {
        try
        {
            List<Order> pendingOrders = orderService.getOrdersByStatus("AFVENTER ACCEPT");
            List<Order> paidOrders = orderService.getOrdersByStatus("BETALT");
            List<Order> inTransitOrders = orderService.getOrdersByStatus("AFSENDT");
            List<Order> doneOrders = orderService.getOrdersByStatus("AFSLUTTET");

            ctx.attribute("pendingOrders", pendingOrders);
            ctx.attribute("paidOrders", paidOrders);
            ctx.attribute("inTransitOrders", inTransitOrders);
            ctx.attribute("doneOrders", doneOrders);

            ctx.render("orders.html");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("orderErrorMessage", e.getMessage());
            ctx.attribute("paidOrders", new ArrayList<>());
            ctx.attribute("inTransitOrders", new ArrayList<>());
            ctx.attribute("doneOrders", new ArrayList<>());
            ctx.redirect("/orders");
        }
    }
}

