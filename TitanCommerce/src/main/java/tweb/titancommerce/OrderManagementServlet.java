package tweb.titancommerce;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.*;
import tweb.titancommerce.login.LoginService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "OrderManagementServlet", urlPatterns = {"/orders"})
public class OrderManagementServlet extends HttpServlet {

    private Gson gson;

    public void init() {
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Verifica autenticazione utente
        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        int orderId = request.getParameter("id") != null ? Integer.parseInt(request.getParameter("id")) : -1;
        String status = request.getParameter("status");

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);

            if (orderId > 0) {
                Orders order = Orders.loadById(orderId, conn);
                if (order != null && order.getUser_id() == userId) {
                    out.println(gson.toJson(order));
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Order not found or unauthorized");
                }
            } else if (status != null) {
                List<Orders> orders = Orders.loadByStatus(userId, status, conn);
                out.println(gson.toJson(orders));
            } else {
                List<Orders> orders = Orders.loadByUserId(userId, conn);
                out.println(gson.toJson(orders));
            }
        } catch (SQLException e) {
            throw new ServletException("Error retrieving orders", e);
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

        try (BufferedReader reader = request.getReader();
             Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {

            int userId = Users.getUserIdByUsernameConn(username, conn);

            Orders order = gson.fromJson(reader, Orders.class);
            order.setUser_id(userId);

            int orderId = order.saveAsNew(conn);
            if (orderId > 0) {
                response.getWriter().println(gson.toJson(orderId));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (SQLException e) {
            throw new ServletException("Error creating order", e);
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

        try (BufferedReader reader = request.getReader();
             Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {

            int userId = Users.getUserIdByUsernameConn(username, conn);
            Orders order = gson.fromJson(reader, Orders.class);

            if (order.getUser_id() != userId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized update attempt");
                return;
            }

            boolean updated = order.saveUpdate(conn);
            response.getWriter().println(gson.toJson(updated));
        } catch (SQLException e) {
            throw new ServletException("Error updating order", e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
            return;
        }

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);
            Orders order = Orders.loadById(orderId, conn);

            if (order == null || order.getUser_id() != userId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized or order not found");
                return;
            }

            boolean deleted = order.delete(conn);
            response.getWriter().println(gson.toJson(deleted));
        } catch (SQLException e) {
            throw new ServletException("Error deleting order", e);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}