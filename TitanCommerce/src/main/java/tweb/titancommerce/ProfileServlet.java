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

    // GET: Retrieve logged-in user's profile details
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set CORS headers
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            // Retrieve user details from the database
            Users user = Users.loadById(userId, conn);
            if (user != null) {
                // Create a JSON object with the required details
                JsonObject userDetails = new JsonObject();
                userDetails.addProperty("username", user.getUsername());
                userDetails.addProperty("email", user.getEmail());
                userDetails.addProperty("firstName", user.getFirstName());
                userDetails.addProperty("lastName", user.getLastName());
                userDetails.addProperty("creationDate", user.getCreatedAt().toString());
                userDetails.addProperty("birthDate", user.getBirthDate().toString());
                userDetails.addProperty("role", user.getRole());
                userDetails.addProperty("gender", user.getSesso());

                out.println(gson.toJson(userDetails));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error retrieving user profile", e);
        }
    }

    // Handle OPTIONS requests for CORS preflight
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set CORS headers
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void destroy() {
    }
}
