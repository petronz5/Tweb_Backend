package tweb.titancommerce.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private int id;
    private int user_id;
    private int product_id;
    private int quantity;

    // Costruttori, getter e setter
    public Cart() {}

    public Cart(int id, int user_id, int product_id, int quantity) {
        this.id = id;
        this.user_id = user_id;
        this.product_id = product_id;
        this.quantity = quantity;
    }

    public Cart(int user_id, int product_id, int quantity) {
        this.user_id = user_id;
        this.product_id = product_id;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Metodo per aggiungere o aggiornare un prodotto nel carrello
    public boolean addToCart(Connection conn) throws SQLException {
        String query = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                "ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart.quantity + EXCLUDED.quantity " +
                "RETURNING id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, user_id);
            st.setInt(2, product_id);
            st.setInt(3, quantity);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                return true;
            }
            return false;
        }
    }

    // Metodo per aggiornare la quantità di un prodotto nel carrello
    public boolean updateQuantity(Connection conn) throws SQLException {
        String query = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, this.quantity);
            st.setInt(2, this.user_id);
            st.setInt(3, this.product_id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per caricare il carrello di un utente
    public static List<Cart> loadByUserId(int user_id, Connection conn) throws SQLException {
        List<Cart> cartList = new ArrayList<>();
        String query = "SELECT * FROM cart WHERE user_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, user_id);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                cartList.add(new Cart(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity")
                ));
            }
        }
        return cartList;
    }

    // Metodo per rimuovere un prodotto dal carrello
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, user_id);
            st.setInt(2, product_id);
            return st.executeUpdate() > 0;
        }
    }
}
