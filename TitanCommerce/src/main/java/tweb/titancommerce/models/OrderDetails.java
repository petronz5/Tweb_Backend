package tweb.titancommerce.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDetails {
    private int orderId;
    private int productId;
    private int quantity;
    private double price; // Aggiungiamo il campo price

    // Costruttori, getter e setter
    public OrderDetails() {}

    public OrderDetails(int orderId, int productId, int quantity, double price) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    // Getter e Setter per price
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Salva i dettagli di un nuovo ordine
    public int saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO order_details (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?) RETURNING order_id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            st.setInt(2, productId);
            st.setInt(3, quantity);
            st.setDouble(4, price); // Impostiamo il prezzo
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("order_id");
            }
        }
        return -1;
    }

    // Carica i dettagli di un ordine per ID ordine
    public static List<OrderDetails> loadByOrderId(int orderId, Connection conn) throws SQLException {
        List<OrderDetails> detailsList = new ArrayList<>();
        String query = "SELECT * FROM order_details WHERE order_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                detailsList.add(new OrderDetails(rs.getInt("order_id"),
                        rs.getInt("product_id"), rs.getInt("quantity"), rs.getDouble("price")));
            }
        }
        return detailsList;
    }

    // Carica tutti i dettagli degli ordini
    public static List<OrderDetails> loadAll(Connection conn) throws SQLException {
        List<OrderDetails> detailsList = new ArrayList<>();
        String query = "SELECT * FROM order_details";
        try (PreparedStatement st = conn.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                detailsList.add(new OrderDetails(rs.getInt("order_id"),
                        rs.getInt("product_id"), rs.getInt("quantity"), rs.getDouble("price")));
            }
        }
        return detailsList;
    }
}
