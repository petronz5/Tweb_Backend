package tweb.titancommerce;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Cart;
import tweb.titancommerce.models.Users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "UserCartServlet", urlPatterns = {"/usercart"})
public class UserCartServlet extends HttpServlet {

    private Gson gson;

    public void init() {
        gson = new Gson();
    }

    // GET: Recupera il carrello dell'utente o i dettagli dell'utente
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // Frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        int userId = Integer.parseInt(request.getParameter("userId"));

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            // Recupera il carrello dell'utente
            List<Cart> cartItems = Cart.loadByUserId(userId, conn);
            out.println(gson.toJson(cartItems));
        } catch (SQLException e) {
            throw new ServletException("Error retrieving cart", e);
        }
    }

    // Aggiungere o modificare un prodotto nel carrello
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // Frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String role = (String) request.getSession().getAttribute("role");

        if (!"customer".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can modify the cart.");
            return;
        }

        BufferedReader in = request.getReader();
        Cart cartItem = gson.fromJson(in, Cart.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            boolean updated = cartItem.updateQuantity(conn);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating cart quantity");
            }
        } catch (SQLException e) {
            throw new ServletException("Error updating cart", e);
        }
    }

    // Rimuovere un prodotto dal carrello
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // Frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String role = (String) request.getSession().getAttribute("role");

        if (!"customer".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can remove products from the cart.");
            return;
        }

        int userId = Integer.parseInt(request.getParameter("userId"));
        int productId = Integer.parseInt(request.getParameter("productId"));

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            Cart cartItem = new Cart(userId, productId, 0);
            boolean deleted = cartItem.delete(conn);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found in cart");
            }
        } catch (SQLException e) {
            throw new ServletException("Error deleting from cart", e);
        }
    }




    public void destroy() {
    }
}
