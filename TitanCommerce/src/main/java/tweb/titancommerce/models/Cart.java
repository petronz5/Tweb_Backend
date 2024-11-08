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
    private String productName;
    private double productPrice;

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    // Metodo statico per caricare il carrello dal database
    public static List<Cart> loadByUserId(int user_id, Connection conn) throws SQLException {
        List<Cart> cartList = new ArrayList<>();
        String query = "SELECT c.id, c.user_id, c.product_id, c.quantity, p.name AS product_name, p.price AS product_price " +
                "FROM cart c " +
                "JOIN products p ON c.product_id = p.id " +
                "WHERE c.user_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, user_id);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Cart cartItem = new Cart(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity")
                );
                cartItem.setProductName(rs.getString("product_name"));
                cartItem.setProductPrice(rs.getDouble("product_price"));
                cartList.add(cartItem);
            }
        }
        return cartList;
    }

    // Metodo per aggiungere un prodotto al carrello o aggiornare la quantità
    public boolean addOrUpdateCart(Connection conn) throws SQLException {
        String selectQuery = "SELECT id FROM cart WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement selectSt = conn.prepareStatement(selectQuery)) {
            selectSt.setInt(1, user_id);
            selectSt.setInt(2, product_id);
            ResultSet rs = selectSt.executeQuery();

            if (rs.next()) {
                // Se l'elemento esiste, aggiorna la quantità chiamando updateQuantity
                return updateQuantity(conn); // chiamata diretta al metodo di aggiornamento
            } else {
                // Se non esiste nel carrello, aggiungi il nuovo elemento
                String insertQuery = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement insertSt = conn.prepareStatement(insertQuery)) {
                    insertSt.setInt(1, user_id);
                    insertSt.setInt(2, product_id);
                    insertSt.setInt(3, quantity);
                    return insertSt.executeUpdate() > 0;
                }
            }
        }
    }

    // Metodo per aggiungere o aggiornare un prodotto nel carrello
    public boolean addToCart(Connection conn) throws SQLException {
        String selectQuery = "SELECT id FROM cart WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement selectSt = conn.prepareStatement(selectQuery)) {
            selectSt.setInt(1, user_id);
            selectSt.setInt(2, product_id);
            ResultSet rs = selectSt.executeQuery();

            if (rs.next()) {
                int existingId = rs.getInt("id");
                // Usa direttamente this.quantity invece di sommare
                String updateQuery = "UPDATE cart SET quantity = ? WHERE id = ?";
                try (PreparedStatement updateSt = conn.prepareStatement(updateQuery)) {
                    updateSt.setInt(1, this.quantity);  // Sostituzione diretta della quantità
                    updateSt.setInt(2, existingId);
                    return updateSt.executeUpdate() > 0;
                }
            } else {
                // Se non esiste nel carrello, aggiungi il nuovo elemento
                String insertQuery = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement insertSt = conn.prepareStatement(insertQuery)) {
                    insertSt.setInt(1, user_id);
                    insertSt.setInt(2, product_id);
                    insertSt.setInt(3, quantity);
                    return insertSt.executeUpdate() > 0;
                }
            }
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

    // Metodo per rimuovere un prodotto dal carrello
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, user_id);
            st.setInt(2, product_id);
            return st.executeUpdate() > 0;
        }
    }

    public static boolean clearCartByUserId(int userId, Connection conn) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Ritorna true se almeno una riga è stata cancellata
        }
    }

}
