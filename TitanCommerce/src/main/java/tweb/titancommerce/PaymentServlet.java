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

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);
            if (userId == -1) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            List<Borsello> paymentMethods = Borsello.getPaymentMethodsByUserId(userId, conn);
            if (paymentMethods.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No payment methods found");
                return;
            }

            String json = gson.toJson(paymentMethods);
            response.getWriter().write(json);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            e.printStackTrace(); // Debugging output
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving payment methods");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        BufferedReader in = request.getReader();
        Borsello newBorsello = gson.fromJson(in, Borsello.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);
            if (userId == -1) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            newBorsello.setUser_id(userId);

            if (newBorsello.savePayment(conn)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().println(gson.toJson(newBorsello));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create payment method");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Debugging output
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error during payment creation");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        BufferedReader in = request.getReader();
        Borsello updatedBorsello = gson.fromJson(in, Borsello.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);
            if (userId == -1) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            updatedBorsello.setUser_id(userId);

            if (updatedBorsello.updateImporto(conn)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(gson.toJson(updatedBorsello));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update payment method");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Debugging output
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error during payment update");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
