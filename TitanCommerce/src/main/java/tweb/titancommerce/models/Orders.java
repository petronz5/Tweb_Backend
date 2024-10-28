package tweb.titancommerce.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Orders {
    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private String status; // ad esempio "pending", "shipped", "delivered"

    // Costruttori, getter e setter
    public Orders() {}

    public Orders(int id, int userId, LocalDateTime orderDate, String status) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Metodo per salvare un nuovo ordine
    public int saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO orders (user_id, status) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            st.setObject(2, orderDate);
            st.setString(3, status);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                return this.id;
            }
        }
        return -1;
    }

    // Metodo per aggiornare un ordine
    public boolean saveUpdate(Connection conn) throws SQLException {
        String query = "UPDATE orders SET user_id = ?, status = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            st.setObject(2, orderDate);
            st.setString(3, status);
            st.setInt(4, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per eliminare un ordine
    public static boolean deleteById(int orderId,  Connection conn) throws SQLException {
        String query = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per caricare ordini in base allo stato e utente (nuovo)
    public static List<Orders> loadByStatus(int userId, String status, Connection conn) throws SQLException {
        List<Orders> orderList = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ? AND status = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            st.setString(2, status);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Orders order = new Orders(rs.getInt("id"), rs.getInt("user_id"),
                        rs.getObject("order_date", LocalDateTime.class), rs.getString("status"));
                orderList.add(order);
            }
        }
        return orderList;
    }

    // Metodo per caricare un ordine in base all'ID
    public static Orders loadById(int orderId, Connection conn) throws SQLException {
        String query = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new Orders(rs.getInt("id"), rs.getInt("user_id"),
                        rs.getObject("order_date", LocalDateTime.class), rs.getString("status"));
            }
        }
        return null;
    }


    // Metodo per caricare ordini per utente (nuovo)
    public static List<Orders> loadByUserId(int userId, Connection conn) throws SQLException {
        List<Orders> orderList = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Orders order = new Orders(rs.getInt("id"), rs.getInt("user_id"),
                        rs.getObject("order_date", LocalDateTime.class), rs.getString("status"));
                orderList.add(order);
            }
        }
        return orderList;
    }

    // Metodo per caricare tutti gli ordini
    public static List<Orders> loadAll(Connection conn) throws SQLException {
        List<Orders> orderList = new ArrayList<>();
        String query = "SELECT * FROM orders";
        try (PreparedStatement st = conn.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Orders order = new Orders(rs.getInt("id"), rs.getInt("user_id"),
                        rs.getObject("order_date", LocalDateTime.class), rs.getString("status"));
                orderList.add(order);
            }
        }
        return orderList;
    }
}
