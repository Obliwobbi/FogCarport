package app.controllers;

import app.dto.OrderWithDetailsDTO;
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
            List<OrderWithDetailsDTO> newOrders = orderService.getOrdersByStatusDTO("NY ORDRE");
            List<OrderWithDetailsDTO> pendingOrders = orderService.getOrdersByStatusDTO("AFVENTER ACCEPT");
            List<OrderWithDetailsDTO> paidOrders = orderService.getOrdersByStatusDTO("BETALT");
            List<OrderWithDetailsDTO> inTransitOrders = orderService.getOrdersByStatusDTO("AFSENDT");
            List<OrderWithDetailsDTO> doneOrders = orderService.getOrdersByStatusDTO("AFSLUTTET");

            ctx.attribute("newOrders", newOrders);
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
            ctx.attribute("newOrders", new ArrayList<>());
            ctx.attribute("pendingOrders", new ArrayList<>());

            ctx.redirect("/orders");
        }

        ctx.render("orders.html");
    }
}

