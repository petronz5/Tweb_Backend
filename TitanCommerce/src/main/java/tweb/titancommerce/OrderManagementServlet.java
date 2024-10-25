package tweb.titancommerce;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Category;
import tweb.titancommerce.models.Orders;
import tweb.titancommerce.models.OrderDetails;
import tweb.titancommerce.models.Products;

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

    // GET: Recupera un ordine, lista di ordini, o ordini filtrati per utente o stato
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        int orderId = request.getParameter("id") != null ? Integer.parseInt(request.getParameter("id")) : -1;
        int userId = request.getParameter("userId") != null ? Integer.parseInt(request.getParameter("userId")) : -1;
        String status = request.getParameter("status");

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            if (orderId > 0) {
                // Recupera un ordine specifico
                Orders order = Orders.loadById(orderId, conn);
                if (order != null) {
                    out.println(gson.toJson(order));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                }
            } else if (userId > 0 && status != null) {
                // Recupera gli ordini per utente e stato
                List<Orders> orders = Orders.loadByStatus(userId, status, conn);
                out.println(gson.toJson(orders));
            } else if (userId > 0) {
                // Recupera tutti gli ordini per utente
                List<Orders> orders = Orders.loadByUserId(userId, conn);
                out.println(gson.toJson(orders));
            } else {
                // Recupera tutti gli ordini
                List<Orders> orders = Orders.loadAll(conn);
                out.println(gson.toJson(orders));
            }
        } catch (SQLException e) {
            throw new ServletException("Error retrieving orders", e);
        }
    }
    // POST: Crea un nuovo ordine
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BufferedReader in = request.getReader();
        Orders newOrder = gson.fromJson(in, Orders.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int orderId = newOrder.saveAsNew(conn);
            if (orderId > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().println(gson.toJson(newOrder));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Order creation failed");
            }
        } catch (SQLException e) {
            throw new ServletException("Error creating order", e);
        }
    }

    // PUT: Aggiorna lo stato di un ordine
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BufferedReader in = request.getReader();
        Orders order = gson.fromJson(in, Orders.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            boolean updated = order.saveUpdate(conn);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(gson.toJson(order));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating order");
            }
        } catch (SQLException e) {
            throw new ServletException("Error updating order", e);
        }
    }


    // DELETE: Cancella un ordine
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int orderId = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            boolean deleted = Orders.deleteById(orderId, conn); // Usa deleteById()
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            }
        } catch (SQLException e) {
            throw new ServletException("Error deleting order", e);
        }
    }

    public void destroy() {
    }
}
