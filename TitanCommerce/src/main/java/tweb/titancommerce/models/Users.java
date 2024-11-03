package tweb.titancommerce.models;

import tweb.titancommerce.db.PoolingPersistenceManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Users {
    private int id;
    private String username;
    private String password;
    private String email;
    private String role; // "admin", "customer"
    private String firstName; // nome
    private String lastName;  // cognome
    private Date birthDate;   // data_di_nascita
    private Timestamp createdAt; // created_at
    private String codiceFiscale;
    private String sesso;

    // Costruttori
    public Users() {}

    public Users(int id, String username, String password, String email, String role,
                 String firstName, String lastName, Date birthDate, Timestamp createdAt,
                 String codiceFiscale, String sesso) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.codiceFiscale = codiceFiscale;
        this.sesso = sesso;
    }

    // Getter e Setter
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    // Nota: per sicurezza, evita di avere un getter per la password
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
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
        String query = "INSERT INTO users (username, password, email, role, nome, cognome, data_di_nascita, created_at, codice_fiscale, sesso) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, email);
            st.setString(4, role);
            st.setString(5, firstName);
            st.setString(6, lastName);
            st.setDate(7, birthDate);
            st.setTimestamp(8, createdAt);
            st.setString(9, codiceFiscale);
            st.setString(10, sesso);
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
        String query = "UPDATE users SET username = ?, password = ?, email = ?, role = ?, nome = ?, cognome = ?, data_di_nascita = ?, codice_fiscale = ?, sesso = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, email);
            st.setString(4, role);
            st.setString(5, firstName);
            st.setString(6, lastName);
            st.setDate(7, birthDate);
            st.setString(8, codiceFiscale);
            st.setString(9, sesso);
            st.setInt(10, id);
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

    public static int getUserIdByUsername(String username) throws SQLException {
        Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("User ID not found for username: " + username);
            }
        } finally {
            conn.close();
        }
    }

    public static int getUserIdByUsernameConn(String username, Connection conn) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("User not found");
            }
        }
    }



    // Metodo per caricare un utente per ID
    public static Users loadById(int id, Connection conn) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Users user = new Users();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setFirstName(rs.getString("nome"));
                user.setLastName(rs.getString("cognome"));
                user.setBirthDate(rs.getDate("data_di_nascita"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setCodiceFiscale(rs.getString("codice_fiscale"));
                user.setSesso(rs.getString("sesso"));
                // Imposta altri campi se necessario
                return user;
            }
        }
        return null;
    }

    // Metodo per caricare un utente per username
    public static Users loadByUsername(String username, Connection conn) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Users user = new Users();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setFirstName(rs.getString("nome"));
                user.setLastName(rs.getString("cognome"));
                user.setBirthDate(rs.getDate("data_di_nascita"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setCodiceFiscale(rs.getString("codice_fiscale"));
                user.setSesso(rs.getString("sesso"));
                // Imposta altri campi se necessario
                return user;
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
                Users user = new Users();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setFirstName(rs.getString("nome"));
                user.setLastName(rs.getString("cognome"));
                user.setBirthDate(rs.getDate("data_di_nascita"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setCodiceFiscale(rs.getString("codice_fiscale"));
                user.setSesso(rs.getString("sesso"));
                usersList.add(user);
            }
        }
        return usersList;
    }

    // Setter per l'ID (potrebbe essere privato se non vuoi che sia modificato dall'esterno)
    public void setId(int id) {
        this.id = id;
    }
}
