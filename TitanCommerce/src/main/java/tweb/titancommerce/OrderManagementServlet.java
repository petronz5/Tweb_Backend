package tweb.titancommerce;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.*;

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
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // Frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
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
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // Frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
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

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // Sostituisci con il tuo frontend
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Ottieni l'userId dai parametri della query string
        int userId = Integer.parseInt(request.getParameter("userId"));

        BufferedReader in = request.getReader();
        Cart cartItem = gson.fromJson(in, Cart.class);

        // Aggiorna il carrello utilizzando l'oggetto cartItem e l'userId
        //cartItem.setUserId(userId);  // Assicura che userId sia impostato correttamente

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            boolean updated = cartItem.updateQuantity(conn);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore aggiornamento quantità nel carrello");
            }
        } catch (SQLException e) {
            throw new ServletException("Errore aggiornamento carrello", e);
        }
    }


    // DELETE: Cancella un ordine
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // Frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
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
