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

    // Costruttori, getter e setter
    public OrderDetails() {}

    public OrderDetails(int orderId, int productId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Metodo per salvare i dettagli di un nuovo ordine
    public int saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO order_details (order_id, product_id, quantity) VALUES (?, ?, ?) RETURNING order_id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            st.setInt(2, productId);
            st.setInt(3, quantity);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("order_id");
            }
        }
        return -1;
    }

    // Metodo per aggiornare i dettagli di un ordine
    public boolean saveUpdate(Connection conn) throws SQLException {
        String query = "UPDATE order_details SET product_id = ?, quantity = ? WHERE order_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, productId);
            st.setInt(2, quantity);
            st.setInt(3, orderId);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per eliminare i dettagli di un ordine
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM order_details WHERE order_id = ? AND product_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            st.setInt(2, productId);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per caricare i dettagli di un ordine per ID ordine
    public static List<OrderDetails> loadByOrderId(int orderId, Connection conn) throws SQLException {
        List<OrderDetails> detailsList = new ArrayList<>();
        String query = "SELECT * FROM order_details WHERE order_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                detailsList.add(new OrderDetails(rs.getInt("order_id"),
                        rs.getInt("product_id"), rs.getInt("quantity")));
            }
        }
        return detailsList;
    }

    // Metodo per caricare tutti i dettagli degli ordini
    public static List<OrderDetails> loadAll(Connection conn) throws SQLException {
        List<OrderDetails> detailsList = new ArrayList<>();
        String query = "SELECT * FROM order_details";
        try (PreparedStatement st = conn.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                detailsList.add(new OrderDetails(rs.getInt("order_id"),
                        rs.getInt("product_id"), rs.getInt("quantity")));
            }
        }
        return detailsList;
    }
}
