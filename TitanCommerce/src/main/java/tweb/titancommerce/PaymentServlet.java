package tweb.titancommerce;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Borsello;
import tweb.titancommerce.models.Users;
import tweb.titancommerce.login.LoginService;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"})
public class PaymentServlet extends HttpServlet {

    private Gson gson;

    public void init() {
        gson = new Gson();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");

        // Recupera lo username dalla sessione
        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            // Ottieni user_id dal database usando username
            int userId = Users.getUserIdByUsernameConn(username, conn);

            List<Borsello> paymentMethods = Borsello.getPaymentMethodsByUserId(userId, conn);
            String json = gson.toJson(paymentMethods);
            response.getWriter().write(json);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            throw new ServletException("Errore nel recupero dei metodi di pagamento", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");

        // Recupera lo username dalla sessione
        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        BufferedReader in = request.getReader();
        Borsello newBorsello = gson.fromJson(in, Borsello.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            // Ottieni user_id dal database usando username
            int userId = Users.getUserIdByUsernameConn(username, conn);
            newBorsello.setUser_id(userId);

            boolean success = newBorsello.savePayment(conn);
            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().println(gson.toJson(newBorsello));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payment method creation failed");
            }
        } catch (SQLException e) {
            throw new ServletException("Error creating payment method", e);
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");

        // Recupera lo username dalla sessione
        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        BufferedReader in = request.getReader();
        Borsello updatedBorsello = gson.fromJson(in, Borsello.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            // Ottieni user_id dal database usando username
            int userId = Users.getUserIdByUsernameConn(username, conn);
            updatedBorsello.setUser_id(userId);

            boolean success = updatedBorsello.updateImporto(conn);
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(gson.toJson(updatedBorsello));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payment update failed");
            }
        } catch (SQLException e) {
            throw new ServletException("Error updating payment method", e);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
