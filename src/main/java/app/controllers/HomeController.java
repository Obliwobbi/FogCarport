package app.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class HomeController
{
    public void addRoutes(Javalin app)
    {
        app.get("/", this::showIndex);
        app.post("/success/return-home", this::returnToHome);
    }

    private void showIndex(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.render("index.html");
    }

    private void returnToHome(Context ctx)
    {
        clearSuccessSession(ctx);
        ctx.redirect("/");
    }

    private void clearSuccessSession(Context ctx)
    {
        ctx.sessionAttribute("successOrderId", null);
        ctx.sessionAttribute("successCustomer", null);
        ctx.sessionAttribute("successCarport", null);
        ctx.sessionAttribute("currentEmployee",null);
    }
}