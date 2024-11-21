package tweb.titancommerce.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItems {
    private int id;
    private int order_id;
    private int product_id;
    private int quantity;
    private String productName;
    private double productPrice;
    private String description;
    private String url_products;

    // Costruttori
    public OrderItems() {}

    public OrderItems(int id, int order_id, int product_id, int quantity) {
        this.id = id;
        this.order_id = order_id;
        this.product_id = product_id;
        this.quantity = quantity;
    }

    // Getter e Setter
    public int getId() { return id; }
    public int getOrder_id() { return order_id; }
    public int getProduct_id() { return product_id; }
    public int getQuantity() { return quantity; }
    public String getProductName() { return productName; }
    public double getProductPrice() { return productPrice; }
    public String getDescription() { return description; }
    public String getUrl_products() { return url_products; }

    public void setId(int id) { this.id = id; }
    public void setOrder_id(int order_id) { this.order_id = order_id; }
    public void setProduct_id(int product_id) { this.product_id = product_id; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public void setDescription(String description) { this.description = description; }
    public void setUrl_products(String url_products) { this.url_products = url_products; }


    // Metodo per salvare un nuovo articolo dell'ordine
    public boolean saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, order_id);
            st.setInt(2, product_id);
            st.setInt(3, quantity);
            return st.executeUpdate() > 0;
        }
    }

    public static List<OrderItems> loadByOrderId(int orderId, Connection conn) throws SQLException {
        List<OrderItems> items = new ArrayList<>();
        String query = "SELECT oi.*, p.name AS product_name, p.price AS product_price, p.description, p.url_products " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, orderId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                OrderItems item = new OrderItems(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity")
                );
                // Aggiungi i dettagli del prodotto
                item.setProductName(rs.getString("product_name"));
                item.setProductPrice(rs.getDouble("product_price"));
                item.setDescription(rs.getString("description"));
                item.setUrl_products(rs.getString("url_products"));
                items.add(item);
            }
        }
        return items;
    }


}
