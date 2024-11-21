package tweb.titancommerce.models;

import jakarta.persistence.criteria.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Orders {
    private int id;
    private int user_id;
    private BigDecimal total;
    private String createdAt; // Cambiato da LocalDateTime a String
    private String status;
    private List<OrderItems> items;

    // Costruttore vuoto e costruttore completo
    public Orders() {}

    public Orders(int id, int user_id, BigDecimal total, String createdAt, String status) {
        this.id = id;
        this.user_id = user_id;
        this.total = total;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getter e Setter
    public int getId() { return id; }
    public int getUser_id() { return user_id; }
    public BigDecimal getTotal() { return total; }
    public String getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public List<OrderItems> getItems() {
        return items;
    }

    public void setUser_id(int user_id) { this.user_id = user_id; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setItems(List<OrderItems> items) {
        this.items = items;
    }

    // Metodo per salvare un nuovo ordine
    public int saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO orders (user_id, total, status, created_at) VALUES (?, ?, ?, ?) RETURNING id";
        System.out.println("Attempting to insert new order with user_id: " + user_id + ", total: " + total + ", status: " + status);

        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, user_id);
            st.setBigDecimal(2, total);
            st.setString(3, status);

            // Converti la stringa createdAt in Timestamp
            Timestamp timestamp;
            if (createdAt == null || createdAt.isEmpty()) {
                timestamp = new Timestamp(System.currentTimeMillis()); // Timestamp corrente
            } else {
                timestamp = Timestamp.valueOf(createdAt);
            }
            st.setTimestamp(4, timestamp);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                System.out.println("Order created with ID: " + this.id);
                return this.id;
            } else {
                System.out.println("Failed to insert order: no ID returned.");
            }
        } catch (SQLException e) {
            System.err.println("SQL error in saveAsNew: " + e.getMessage());
            throw e;
        }
        return -1;
    }

    // Metodo per aggiornare un ordine
    public boolean saveUpdate(Connection conn) throws SQLException {
        String query = "UPDATE orders SET user_id = ?, total = ?, status = ?, created_at = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, user_id);
            st.setBigDecimal(2, total);
            st.setString(3, status);

            // Converti la stringa createdAt in Timestamp
            Timestamp timestamp = Timestamp.valueOf(createdAt);
            st.setTimestamp(4, timestamp);

            st.setInt(5, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per caricare ordini in base allo stato e utente
    public static List<Orders> loadByStatus(int userId, String status, Connection conn) throws SQLException {
        List<Orders> orderList = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ? AND status = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            st.setString(2, status);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                // Converti il Timestamp in String per createdAt
                String createdAt = rs.getTimestamp("created_at").toString();

                Orders order = new Orders(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getBigDecimal("total"),
                        createdAt,
                        rs.getString("status")
                );
                orderList.add(order);
            }
        }
        return orderList;
    }

    // Metodo per caricare un ordine in base all'ID, includendo i prodotti
    public static Orders loadById(int orderId, Connection conn) throws SQLException {
        String query = "SELECT * FROM orders WHERE id = ?";
        Orders order = null;
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                // Converti il Timestamp in String per createdAt
                String createdAt = rs.getTimestamp("created_at").toString();

                order = new Orders(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getBigDecimal("total"),
                        createdAt,
                        rs.getString("status")
                );

                // Carica gli articoli dell'ordine
                List<OrderItems> items = OrderItems.loadByOrderId(orderId, conn);
                order.setItems(items);
            }
        }
        return order;
    }

    // Metodo per caricare ordini per utente, includendo i prodotti
    public static List<Orders> loadByUserId(int userId, Connection conn) throws SQLException {
        List<Orders> orderList = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                // Converti il Timestamp in String per createdAt
                String createdAt = rs.getTimestamp("created_at").toString();

                Orders order = new Orders(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getBigDecimal("total"),
                        createdAt,
                        rs.getString("status")
                );

                // Carica gli articoli dell'ordine
                List<OrderItems> items = OrderItems.loadByOrderId(order.getId(), conn);
                order.setItems(items);

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
                // Converti il Timestamp in String per createdAt
                String createdAt = rs.getTimestamp("created_at").toString();

                Orders order = new Orders(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getBigDecimal("total"),
                        createdAt,
                        rs.getString("status")
                );
                orderList.add(order);
            }
        }
        return orderList;
    }
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, this.id);
            return st.executeUpdate() > 0;
        }
    }
}