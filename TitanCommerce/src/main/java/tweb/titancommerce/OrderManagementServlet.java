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
            System.out.println("Tentativo di accesso non autorizzato.");
            return;
        }

        String orderIdParam = request.getParameter("id");
        int orderId = -1;
        if (orderIdParam != null) {
            try {
                orderId = Integer.parseInt(orderIdParam);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
                System.out.println("Order ID invalido: " + orderIdParam);
                return;
            }
        }

        String status = request.getParameter("status");

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);
            System.out.println("User ID recuperato: " + userId);

            if (orderId > 0) {
                Orders order = Orders.loadById(orderId, conn);
                if (order != null && order.getUser_id() == userId) {
                    String orderJson = gson.toJson(order);
                    out.println(orderJson);
                    response.setStatus(HttpServletResponse.SC_OK);
                    System.out.println("Ordine con ID " + orderId + " recuperato con successo per user_id: " + userId);
                } else if (order == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                    System.out.println("Ordine non trovato con ID: " + orderId);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized access to the order");
                    System.out.println("Accesso non autorizzato all'ordine con ID: " + orderId);
                }
            } else if (status != null) {
                List<Orders> orders = Orders.loadByStatus(userId, status, conn);
                if (!orders.isEmpty()) {
                    String ordersJson = gson.toJson(orders);
                    out.println(ordersJson);
                    response.setStatus(HttpServletResponse.SC_OK);
                    System.out.println("Ordini con status '" + status + "' recuperati con successo per user_id: " + userId);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No orders found with the specified status");
                    System.out.println("Nessun ordine trovato con status: " + status + " per user_id: " + userId);
                }
            } else {
                List<Orders> orders = Orders.loadByUserId(userId, conn);
                if (!orders.isEmpty()) {
                    String ordersJson = gson.toJson(orders);
                    out.println(ordersJson);
                    response.setStatus(HttpServletResponse.SC_OK);
                    System.out.println("Ordini recuperati con successo per user_id: " + userId);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No orders found for the user");
                    System.out.println("Nessun ordine trovato per user_id: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero degli ordini: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving orders");
        } finally {
            out.close();
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

            if (order.getItems() == null || order.getItems().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No items in order.");
                return;
            }

            // Inizia una transazione
            conn.setAutoCommit(false);

            int orderId = order.saveAsNew(conn);
            if (orderId > 0) {
                boolean allItemsSaved = true;

                for (OrderItems item : order.getItems()) {
                    item.setOrder_id(orderId);

                    // Decrementa lo stock
                    Products product = Products.loadById(item.getProduct_id(), conn);
                    if (product != null) {
                        boolean stockUpdated = product.decrementStock(item.getQuantity(), conn);
                        if (!stockUpdated) {
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Insufficient stock for product ID: " + item.getProduct_id());
                            conn.rollback();
                            return;
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found: ID " + item.getProduct_id());
                        conn.rollback();
                        return;
                    }

                    // Salva l'articolo dell'ordine
                    boolean itemSaved = item.saveAsNew(conn);
                    if (!itemSaved) {
                        allItemsSaved = false;
                        break;
                    }
                }

                if (allItemsSaved) {
                    // Svuota il carrello dell'utente
                    boolean cartCleared = Cart.clearCartByUserId(userId, conn);
                    if (!cartCleared) {
                        conn.rollback();
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to clear cart after order creation");
                        return;
                    }

                    // Commit della transazione
                    conn.commit();
                    response.getWriter().println(gson.toJson(orderId));
                    response.setStatus(HttpServletResponse.SC_CREATED);
                } else {
                    conn.rollback();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save order items");
                }
            } else {
                conn.rollback();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Order creation failed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

            // Verifica se l'ordine esiste
            Orders existingOrder = Orders.loadById(order.getId(), conn);
            if (existingOrder == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }

            if (existingOrder.getUser_id() != userId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized update attempt");
                return;
            }

            boolean updated = order.saveUpdate(conn);
            if (updated) {
                response.getWriter().println(gson.toJson(updated));
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Order update failed");
            }
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

            if (order == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }

            if (order.getUser_id() != userId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized or order not found");
                return;
            }

            boolean deleted = order.delete(conn);
            if (deleted) {
                response.getWriter().println(gson.toJson(deleted));
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Order deletion failed");
            }
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
