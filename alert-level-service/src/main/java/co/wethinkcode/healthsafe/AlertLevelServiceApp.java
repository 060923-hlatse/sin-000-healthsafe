package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

public class AlertLevelServiceApp {

    private static int alertLevel = 0;

    public static void main(String[] args) {

        Javalin app = Javalin.create().start(7032);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/alert-level", ctx -> {
            ctx.json(Map.of("level", alertLevel));
        });

        app.post("/alert-level", ctx -> {

            Map<String, Integer> request =
                    ctx.bodyAsClass(Map.class);

            Integer level = request.get("level");

            if (level == null || level < 0 || level > 8) {
                ctx.status(400);
                ctx.result("Alert level must be between 0 and 8");
                return;
            }

            alertLevel = level;

            ctx.json(Map.of(
                    "message", "Alert level updated",
                    "level", alertLevel
            ));
        });
    }
}