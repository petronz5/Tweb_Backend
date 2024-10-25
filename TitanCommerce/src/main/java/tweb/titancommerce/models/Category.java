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

    // Costruttori, Getter e Setter

    public Category() {}

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
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
                return new Category(rs.getInt("id"), rs.getString("name"));
            }
        }
        return null;
    }

    // Metodo per caricare tutte le categorie
    public static List<String> loadAllCategories(Connection conn) throws SQLException {
        List<String> categories = new ArrayList<>();
        String query = "SELECT name FROM categories";  // Assumendo che la tabella sia chiamata 'categories' e che abbia una colonna 'name'
        try (PreparedStatement st = conn.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        }
        return categories;
    }
}
