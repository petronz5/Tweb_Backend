package tweb.titancommerce.models;

import tweb.titancommerce.db.PoolingPersistenceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Users {
    private int id;
    private String username;
    private String password;
    private String email;
    private String role; // "admin", "customer"

    // Costruttori, Getter e Setter
    public Users() {}

    public Users(int id, String username, String password, String email, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    // Metodo per validare le credenziali
    public static boolean validateCredentials(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setString(1, username);
            ResultSet rs = st.executeQuery();

            // Controlliamo se l'utente è stato trovato e verifichiamo la password
            if (rs.next()) {
                String storedPassword = rs.getString("password");

                // Se la password nel DB è memorizzata in chiaro, usa un semplice confronto
                // Se usi hashing (ad es. bcrypt), sostituisci questa parte con l'algoritmo di hashing
                return storedPassword.equals(password); // Comparazione della password
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore
            return false;
        }

        return false; // Ritorna false se l'utente non è stato trovato o c'è stato un errore
    }

    public static String getRoleByUsername(String username) throws SQLException {
        String role = "";
        String query = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
            }
        }

        return role;
    }


    // Metodo per salvare un nuovo utente
    public int saveAsNew(Connection conn) throws SQLException {
        String query = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, email);
            st.setString(4, role);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                return this.id;
            }
        }
        return -1;
    }

    // Metodo per aggiornare un utente esistente
    public boolean saveUpdate(Connection conn) throws SQLException {
        String query = "UPDATE users SET username = ?, password = ?, email = ?, role = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, email);
            st.setString(4, role);
            st.setInt(5, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per eliminare un utente
    public boolean delete(Connection conn) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        }
    }

    // Metodo per caricare un utente per ID
    public static Users loadById(int id, Connection conn) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new Users(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("email"), rs.getString("role"));
            }
        }
        return null;
    }

    public static Users loadByUsername(String username, Connection conn) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new Users(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("email"), rs.getString("role"));
            }
        }
        return null;
    }

    // Metodo per caricare tutti gli utenti
    public static List<Users> loadAll(Connection conn) throws SQLException {
        List<Users> usersList = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (PreparedStatement st = conn.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Users user = new Users(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("email"), rs.getString("role"));
                usersList.add(user);
            }
        }
        return usersList;
    }
}
