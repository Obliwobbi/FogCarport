package app.controllers;

import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

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
        ctx.render("orders.html");
    }
}

