package tweb.titancommerce.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private int userId;
    private int productId;
    private int quantity;

    // Costruttori, getter e setter
    public Cart() {}

    public Cart(int userId, int productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    // Metodo per aggiungere un prodotto al carrello
    // Metodo per aggiungere un nuovo elemento al carrello
    // Metodo per aggiungere un prodotto al carrello (nuovo)
    public boolean addToCart(Connection conn) throws SQLException {
        String query = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart.quantity + EXCLUDED.quantity";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            st.setInt(2, productId);
            st.setInt(3, quantity);
            return st.executeUpdate() > 0;
        }
    }



    // Metodo per aggiornare il carrello
    public boolean saveUpdate(Connection conn) throws SQLException {
        String query = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, quantity);
            st.setInt(2, userId);
            st.setInt(3, productId);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per rimuovere un prodotto dal carrello
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            st.setInt(2, productId);
            return st.executeUpdate() > 0;
        }
    }

    public boolean updateQuantity(Connection conn) throws SQLException {
        String query = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, this.quantity);
            st.setInt(2, this.userId);
            st.setInt(3, this.productId);
            return st.executeUpdate() > 0;
        }
    }


    // Metodo per caricare il carrello di un utente
    public static List<Cart> loadByUserId(int userId, Connection conn) throws SQLException {
        List<Cart> cartList = new ArrayList<>();
        String query = "SELECT * FROM cart WHERE user_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                cartList.add(new Cart(rs.getInt("user_id"),
                        rs.getInt("product_id"), rs.getInt("quantity")));
            }
        }
        return cartList;
    }

    public static int countByUserId(int userId, Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM Cart WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
