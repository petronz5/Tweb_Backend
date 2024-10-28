package tweb.titancommerce.models;

import tweb.titancommerce.db.PoolingPersistenceManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Products {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private int categoryId;

    // Costruttori, Getter e Setter

    public Products() {}

    public Products(int id, String name, String description, BigDecimal price, int stock, int categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getStock() {
        return stock;
    }

    public String getDescription() {
        return description;
    }

    // Metodo per salvare un nuovo prodotto
    public int saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO products (name, description, price, stock, category_id) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, name);
            st.setString(2, description);
            st.setBigDecimal(3, price);
            st.setInt(4, stock);
            st.setInt(5, categoryId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                return this.id;
            }
        }
        return -1;
    }

    // Metodo per aggiornare un prodotto esistente
    public boolean saveUpdate(Connection conn) throws SQLException {
        String query = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, category_id = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, name);
            st.setString(2, description);
            st.setBigDecimal(3, price);
            st.setInt(4, stock);
            st.setInt(5, categoryId);
            st.setInt(6, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per eliminare un prodotto
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per caricare un prodotto per ID
    public static Products loadById(int id, Connection conn) throws SQLException {
        String query = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new Products(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getBigDecimal("price"),
                        rs.getInt("stock"), rs.getInt("category_id"));
            }
        }
        return null;
    }

    // Metodo per caricare tutti i prodotti
    public static List<Products> loadAll(Connection conn) throws SQLException {
        List<Products> productsList = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (PreparedStatement st = conn.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Products product = new Products(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getBigDecimal("price"),
                        rs.getInt("stock"), rs.getInt("category_id"));
                productsList.add(product);
            }
        }
        return productsList;
    }

    public static List<Products> searchByName(String searchQuery, Connection conn) throws SQLException {
        List<Products> productsList = new ArrayList<>();
        String query = "SELECT * FROM products WHERE name ILIKE ?";  // Usa ILIKE per case-insensitive search
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, "%" + searchQuery + "%");  // Cerca prodotti che contengono il nome nella query
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Products product = new Products(rs.getInt("id"), rs.getString("name"),
                            rs.getString("description"), rs.getBigDecimal("price"),
                            rs.getInt("stock"), rs.getInt("category_id"));
                    productsList.add(product);
                }
            }
        }
        return productsList;
    }
    public static List<Products> filterByPriceAndCategory(BigDecimal minPrice, BigDecimal maxPrice, String[] categories, Connection conn) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM products WHERE 1=1");

        // Filtra per prezzo minimo
        if (minPrice != null) {
            query.append(" AND price >= ?");
        }
        // Filtra per prezzo massimo
        if (maxPrice != null) {
            query.append(" AND price <= ?");
        }
        // Filtra per categorie
        if (categories != null && categories.length > 0) {
            query.append(" AND category_id IN (");
            for (int i = 0; i < categories.length; i++) {
                query.append("?");
                if (i < categories.length - 1) {
                    query.append(", ");
                }
            }
            query.append(")");
        }

        try (PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;

            if (minPrice != null) {
                stmt.setBigDecimal(paramIndex++, minPrice);
            }
            if (maxPrice != null) {
                stmt.setBigDecimal(paramIndex++, maxPrice);
            }
            if (categories != null && categories.length > 0) {
                for (String category : categories) {
                    stmt.setInt(paramIndex++, Integer.parseInt(category));
                }
            }

            ResultSet rs = stmt.executeQuery();
            List<Products> productList = new ArrayList<>();

            while (rs.next()) {
                Products product = new Products(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getBigDecimal("price"),
                        rs.getInt("stock"), rs.getInt("category_id"));
                productList.add(product);
            }

            return productList;
        }
    }


}
