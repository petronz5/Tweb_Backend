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

            List<Cart> cartItems = Cart.loadByUserId(userId, conn);
            String cartJson = gson.toJson(cartItems);
            response.getWriter().write(cartJson);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving cart items");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        String username = LoginService.getCurrentLogin(request.getSession());
        System.out.println("DEBUG: Username ottenuto dalla sessione: " + username);

        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        try (BufferedReader reader = request.getReader();
             Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {

            int userId = Users.getUserIdByUsernameConn(username, conn);
            System.out.println("DEBUG: userId recuperato dal database: " + userId);

            Cart cartItem;
            try {
                cartItem = gson.fromJson(reader, Cart.class);
                System.out.println("DEBUG: Oggetto Cart deserializzato: " + cartItem);
            } catch (JsonSyntaxException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
                return;
            }

            if (cartItem == null || cartItem.getProduct_id() == 0) {
                System.out.println("DEBUG: cartItem non valido o product_id mancante");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing product_id");
                return;
            }

            cartItem.setUser_id(userId);
            System.out.println("DEBUG: Cart dopo aver settato user_id: " + cartItem);

            boolean updated = cartItem.addOrUpdateCart(conn); // Usa addOrUpdateCart invece di addToCart
            System.out.println("DEBUG: Risultato di addOrUpdateCart: " + updated);

            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Cart updated successfully");
                System.out.println("DEBUG: Cart aggiornato con successo");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update cart");
                System.out.println("DEBUG: Fallimento nell'aggiornamento del cart");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }


    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        String username = LoginService.getCurrentLogin(request.getSession());
        if (username == null || username.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        String productIdParam = request.getParameter("product_id");

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int userId = Users.getUserIdByUsernameConn(username, conn);

            if (productIdParam == null) {
                // Cancella tutti gli articoli del carrello per l'utente loggato
                boolean cleared = Cart.clearCartByUserId(userId, conn);
                if (cleared) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Cart cleared successfully");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error clearing cart");
                }
            } else {
                // Cancella un singolo articolo del carrello in base al product_id
                int productId;
                try {
                    productId = Integer.parseInt(productIdParam);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid productId");
                    return;
                }

                Cart cartItem = new Cart(userId, productId, 0);
                boolean deleted = cartItem.delete(conn);

                if (deleted) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Item removed from cart");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found in cart");
                }
            }
        } catch (SQLException e) {
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
