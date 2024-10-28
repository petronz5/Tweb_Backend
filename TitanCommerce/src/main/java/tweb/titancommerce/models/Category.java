package tweb.titancommerce.models;

import tweb.titancommerce.db.PoolingPersistenceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Category {
    private int id;
    private String name;
    private String image_url;

    // Costruttori, Getter e Setter

    public Category() {}

    public Category(int id, String name, String image_url) {
        this.id = id;
        this.name = name;
        this.image_url = image_url;
    }

    // Metodo per salvare una nuova categoria
    public int saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO categories (name) VALUES (?) RETURNING id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                return this.id;
            }
        }
        return -1;
    }

    // Metodo per aggiornare una categoria esistente
    public boolean saveUpdate(Connection conn) throws SQLException {
        String query = "UPDATE categories SET name = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, name);
            st.setInt(2, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per eliminare una categoria
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per caricare una categoria per ID
    public static Category loadById(int id, Connection conn) throws SQLException {
        String query = "SELECT * FROM categories WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new Category(rs.getInt("id"), rs.getString("name"), rs.getString("image_url"));
            }
        }
        return null;
    }

    // Metodo per caricare tutte le categorie
    // Supponiamo che Category abbia almeno un costruttore con id e name
    public static List<Category> loadAllCategories(Connection conn) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, image_url FROM categories";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String image_url = rs.getString("image_url");
                categories.add(new Category(id, name, image_url));
            }
        }
        return categories;
    }

}