package tweb.titancommerce;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Users;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    private Gson gson;

    public void init() {
        gson = new Gson();
    }

    // GET: Recupera i dettagli del profilo dell'utente loggato
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Imposta gli header CORS
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true"); // Se usi le credenziali

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Verifica se l'utente è loggato
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("id") == null) {
            System.out.println("Session not found or user not logged in");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        } else {
            System.out.println("User ID: " + session.getAttribute("id"));
        }

        // Assicuriamoci che l'ID utente sia valido
        Object userIdObj = session.getAttribute("id");
        if (userIdObj == null || !(userIdObj instanceof Integer)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User ID not found.");
            return;
        }
        int userId = (int) userIdObj;

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            // Recupera i dettagli dell'utente dal database
            Users user = Users.loadById(userId, conn);
            if (user != null) {
                // Crea un oggetto JSON con i dettagli necessari
                JsonObject userDetails = new JsonObject();
                userDetails.addProperty("username", user.getUsername());
                userDetails.addProperty("email", user.getEmail());
                userDetails.addProperty("firstName", user.getFirstName());
                userDetails.addProperty("lastName", user.getLastName());
                userDetails.addProperty("creationDate", user.getCreatedAt().toString()); // Assicurati che sia formattata correttamente
                userDetails.addProperty("birthDate", user.getBirthDate().toString());    // Assicurati che sia formattata correttamente
                userDetails.addProperty("role", user.getRole());
                userDetails.addProperty("sesso", user.getSesso());

                out.println(gson.toJson(userDetails));
                response.setStatus(HttpServletResponse.SC_OK); // Imposta lo stato a 200 OK
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving user profile.");
        } finally {
            out.close();
        }
    }

    // Gestione delle richieste OPTIONS per CORS preflight
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Imposta gli header CORS
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void destroy() {
    }
}
