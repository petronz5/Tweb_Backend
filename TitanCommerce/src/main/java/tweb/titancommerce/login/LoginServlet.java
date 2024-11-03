package tweb.titancommerce.login;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.json.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "Login-Servlet", urlPatterns = {LoginService.LOGIN_PATH, LoginService.LOGOUT_PATH})
public class LoginServlet extends HttpServlet {

    public void init() {}

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String username = LoginService.getCurrentLogin(request.getSession());

        if (username.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject result = new JsonObject();
            result.addProperty("success", false);
            result.addProperty("error", true);
            result.addProperty("errorMessage", "User not logged in");
            out.println(result);
            return;
        }

        if (request.getServletPath().equals(LoginService.LOGIN_PATH)) {
            JsonObject result = new JsonObject();
            result.addProperty("operation", "status");
            result.addProperty("username", username);
            result.addProperty("success", true);
            result.addProperty("error", false);
            result.addProperty("errorMessage", "");
            out.println(result);
        } else if (request.getServletPath().equals(LoginService.LOGOUT_PATH)) {
            JsonObject result = new JsonObject();
            if (!username.isEmpty()) {
                LoginService.doLogOut(request.getSession(), username);
                result.addProperty("operation", "logout");
                result.addProperty("username", "");
                result.addProperty("success", true);
                result.addProperty("error", false);
                result.addProperty("errorMessage", "");
            } else {
                result.addProperty("operation", "logout");
                result.addProperty("username", "");
                result.addProperty("success", false);
                result.addProperty("error", true);
                result.addProperty("errorMessage", "No logged user");
            }
            out.println(result);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (request.getServletPath().equals(LoginService.LOGIN_PATH)) {
            response.setContentType("application/json");
            BufferedReader in = request.getReader();
            JsonObject loginObject = JsonParser.parseReader(in).getAsJsonObject();
            String username = loginObject.get("username").getAsString();
            String previous = LoginService.getCurrentLogin(request.getSession());
            JsonObject result = new JsonObject();

            if (!previous.isEmpty() && !previous.equals(username)) {
                result.addProperty("operation", "login");
                result.addProperty("username", previous);
                result.addProperty("success", false);
                result.addProperty("error", true);
                result.addProperty("errorMessage", "Already logged in as a different user.");
            } else {
                String password = loginObject.get("password").getAsString();
                boolean valid = Users.validateCredentials(username, password);

                if (valid) {
                    if (previous.isEmpty()) {
                        try {
                            LoginService.doLogIn(request.getSession(), username);

                            // Ottieni l'ID dell'utente dal database
                            int userId = Users.getUserIdByUsername(username); // Assicurati che questa funzione esista in Users

                            // Imposta l'ID dell'utente nella sessione
                            HttpSession session = request.getSession();
                            session.setAttribute("id", userId);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    result.addProperty("operation", "login");
                    result.addProperty("username", username);
                    result.addProperty("success", true);
                    result.addProperty("error", false);
                    result.addProperty("errorMessage", "");
                } else {
                    result.addProperty("operation", "login");
                    result.addProperty("username", username);
                    result.addProperty("success", false);
                    result.addProperty("error", true);
                    result.addProperty("errorMessage", "Invalid credentials");
                }
            }
            PrintWriter out = response.getWriter();
            out.println(result);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public void destroy() {}
}
