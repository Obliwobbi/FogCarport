package app.controllers;

import app.dto.OrderWithDetailsDTO;
import app.entities.Carport;
import app.entities.MaterialsLine;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.services.OrderDetailsService;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrderController
{
    private final OrderService orderService;
    private final OrderDetailsService orderDetailsService;

    public OrderController(OrderService orderService, OrderDetailsService orderDetailsService)
    {
        this.orderService = orderService;
        this.orderDetailsService = orderDetailsService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/orders", this::showOrders);
        app.get("/orders/details/{id}", this::showOrderDetails);

        app.post("/orders/delete/{id}", this::deleteOrder);

        app.post("/orders/details/{id}/update-order", this::updateOrderInfo);
        app.post("/orders/details/{id}/update-customer", this::updateCustomerInfo);
        app.post("/orders/details/{id}/update-carport", this::updateCarportInfo);
        app.post("/orders/details/{id}/update-prices", this::updateMaterialPrices);

        app.post("/orders/details/{id}/stykliste", this::generateMaterialList);
        app.post("/orders/details/{id}/regenerate-stykliste", this::regenerateMaterialList);

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

    private void showOrderDetails(Context ctx)
    {
        String orderIdString = ctx.pathParam("id");
        String editSection = ctx.queryParam("edit");

        try
        {
            int orderId = Integer.parseInt(orderIdString);
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            if (order == null)
            {
                ctx.attribute("errorMessage", "Ordre ikke fundet");
                ctx.redirect("/orders");
                return;
            }
            List<MaterialsLine> materialsLines = order.getMaterialsLines();

            ctx.attribute("order", order);
            ctx.attribute("materialsLines", materialsLines);
            ctx.attribute("hasMaterialsList", materialsLines != null && !materialsLines.isEmpty());
            ctx.attribute("editsection", editSection);

            ctx.render("order-details.html");
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Ugyldigt ordre ID");
            ctx.redirect("/orders");
        }
        catch (NullPointerException e)
        {
            ctx.attribute("errorMessage", "Ordre har ikke materiale liste");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders");
        }
    }

    private void updateOrderInfo(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        try
        {
            String status = ctx.formParam("status");
            String deliveryDateString = ctx.formParam("deliveryDate");
            LocalDateTime deliveryDate = (deliveryDateString != null && !deliveryDateString.isEmpty()) ? LocalDateTime.parse(deliveryDateString) : null;

            orderService.updateOrderStatus(orderId,status);
            orderService.updateOrderDeliveryDate(orderId,deliveryDate);
            ctx.redirect("/orders/details/"+orderId+"?success=order");
        }
        catch (DatabaseException e)
        {
            ctx.redirect("/orders/details/"+orderId + "?error="+e.getMessage());
        }

    }

    private void updateCustomerInfo(Context ctx)
    {
    }

    private void updateCarportInfo(Context ctx)
    {
    }

    private void updateMaterialPrices(Context ctx)
    {
    }


    private void generateMaterialList(Context ctx)
    {
        String orderIdStr = ctx.pathParam("id");

        try
        {
            OrderWithDetailsDTO order = new OrderWithDetailsDTO();
            int orderId = Integer.parseInt(orderIdStr);
            try
            {
                order = orderService.getOrderwithDetails(orderId);
            }
            catch (DatabaseException e)
            {
                ctx.attribute("errorMessage", "Ugyldigt ordre ID: " + orderId);
            }
            List<MaterialsLine> existingMaterialsLines = order.getMaterialsLines();
            if (existingMaterialsLines != null && !existingMaterialsLines.isEmpty())
            {
                ctx.attribute("errorMessage", "Materialer er allerede generet for denne ordre");
                ctx.redirect("/orders/details/"+orderId);
                return;
            }
            Carport carport = order.getCarport();
            orderDetailsService.addMaterialListToOrder(orderId,carport);

            ctx.attribute("successMessage", "Materiale liste blev genereret");
            ctx.redirect("/orders/details/"+ orderId);
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke oprette materiale liste: " + e.getMessage());
        }

    }

    private void regenerateMaterialList(Context ctx)
    {
    }

    private void deleteOrder(Context ctx) throws DatabaseException
    {
        String orderIdStr = ctx.pathParam("id");

        try
        {
            int orderId = Integer.parseInt(orderIdStr);
            if (orderService.deleteOrder(orderId))
            {
                ctx.redirect("/orders");
                ctx.attribute("successMessage", "Du har slette ordren med id " + orderId);
            }
            else
            {
                ctx.attribute("errorMessage", "Kunne ikke slette ordren");
                ctx.redirect("/orders");
            }

        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke parse tallet");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders");
        }
    }
}

