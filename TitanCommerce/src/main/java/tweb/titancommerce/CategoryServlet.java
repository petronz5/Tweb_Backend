package tweb.titancommerce;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Category;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "CategoryServlet", urlPatterns = {"/categories"})
public class CategoryServlet extends HttpServlet {

    private Gson gson;

    public void init() {
        gson = new Gson();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            List<Category> categories = Category.loadAllCategories(conn);
            System.out.println("Categorie caricate: " + gson.toJson(categories));
            response.getWriter().println(gson.toJson(categories));
        } catch (SQLException e) {
            throw new ServletException("Error retrieving categories", e);
        }
    }
}
