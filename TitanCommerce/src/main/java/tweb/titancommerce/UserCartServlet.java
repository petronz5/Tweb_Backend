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
import tweb.titancommerce.login.LoginService;
import tweb.titancommerce.models.Users;

import java.io.BufferedReader;
import java.io.IOException;
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

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);

            if (userId == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            List<Cart> cartItems = Cart.loadByUserId(userId, conn);
            String cartJson = gson.toJson(cartItems);

            if (cartItems.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("[]"); // Restituisce un array vuoto
                System.out.println("Carrello vuoto per user_id: " + userId);
                return;
            }

            response.getWriter().write(cartJson);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            e.printStackTrace();  // per debugging
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving cart items");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        try (BufferedReader reader = request.getReader();
             Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {

            int userId = Users.getUserIdByUsernameConn(username, conn);

            Cart cartItem;
            try {
                cartItem = gson.fromJson(reader, Cart.class);
            } catch (JsonSyntaxException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
                return;
            }

            if (cartItem == null || cartItem.getProduct_id() == 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing product_id");
                return;
            }

            cartItem.setUser_id(userId);

            boolean updated = cartItem.addOrUpdateCart(conn);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Cart updated successfully");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update cart");
            }
        } catch (SQLException e) {
            e.printStackTrace();  // per debugging
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            System.out.println("Tentativo di accesso non autorizzato durante la rimozione dal carrello.");
            return;
        }

        String productIdParam = request.getParameter("product_id");
        System.out.println("Ricevuta richiesta DELETE per product_id: " + productIdParam + " da utente: " + username);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);
            System.out.println("User ID recuperato: " + userId);

            if (productIdParam == null) {
                System.out.println("Product_id non fornito. Tentativo di svuotare il carrello.");
                boolean cleared = Cart.clearCartByUserId(userId, conn);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Cart cleared successfully");
                System.out.println("Carrello svuotato con successo per user_id: " + userId);
            } else {
                int productId;
                try {
                    productId = Integer.parseInt(productIdParam);
                    System.out.println("Product ID parsato: " + productId);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid productId");
                    System.out.println("Product_id invalido: " + productIdParam);
                    return;
                }

                Cart cartItem = new Cart(userId, productId, 0);
                System.out.println("Tentativo di rimuovere product_id: " + productId + " dal carrello dell'utente: " + userId);
                boolean deleted = cartItem.delete(conn);
                System.out.println("Rimozione product_id " + productId + " riuscita: " + deleted);

                if (deleted) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Item removed from cart");
                    System.out.println("Prodotto_id " + productId + " rimosso con successo dal carrello.");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found in cart");
                    System.out.println("Prodotto_id " + productId + " non trovato nel carrello.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella gestione del carrello: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }



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
