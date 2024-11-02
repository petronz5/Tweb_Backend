package tweb.titancommerce;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Cart;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));

            try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
                // Carica il carrello dal database
                List<Cart> cartItems = Cart.loadByUserId(userId, conn);
                String cartJson = gson.toJson(cartItems);
                response.getWriter().write(cartJson);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving cart items");
        }
    }


    // PUT: Aggiungere o modificare un prodotto nel carrello
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        try (BufferedReader reader = request.getReader();
             Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {

            // Parsing del JSON del corpo della richiesta
            Cart cartItem;
            try {
                cartItem = gson.fromJson(reader, Cart.class);
            } catch (JsonSyntaxException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
                return;
            }

            if (cartItem == null || cartItem.getUser_id() == 0 || cartItem.getProduct_id() == 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing user_id or product_id");
                return;
            }

            // Aggiunge o aggiorna il prodotto nel carrello
            boolean updated = cartItem.addToCart(conn);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Cart updated successfully");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update cart");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }

    // DELETE: Rimuovere un prodotto dal carrello
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        int userId;
        int productId;

        try {
            userId = Integer.parseInt(request.getParameter("user_id"));
            productId = Integer.parseInt(request.getParameter("product_id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid userId or productId");
            return;
        }

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            Cart cartItem = new Cart(userId , productId , 0);
            boolean deleted = cartItem.delete(conn);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Item removed from cart");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found in cart");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting item from cart");
        }
    }

    // Gestione delle opzioni pre-flight per il CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
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
