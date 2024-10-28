package tweb.titancommerce;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tweb.titancommerce.db.PoolingPersistenceManager;
import tweb.titancommerce.models.Products;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import static java.lang.System.out;

@WebServlet(name = "ProductManagementServlet", urlPatterns = {"/products"})
public class ProductManagementServlet extends HttpServlet {

    private Gson gson;

    public void init() {
        gson = new Gson();
    }

    // GET: Recupera uno o più prodotti
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("application/json");

        String searchQuery = request.getParameter("search");
        String minPriceParam = request.getParameter("minPrice");
        String maxPriceParam = request.getParameter("maxPrice");
        String[] categoriesParam = request.getParameterValues("categories");

        BigDecimal minPrice = minPriceParam != null ? new BigDecimal(minPriceParam) : null;
        BigDecimal maxPrice = maxPriceParam != null ? new BigDecimal(maxPriceParam) : null;

        int productId = request.getParameter("id") != null ? Integer.parseInt(request.getParameter("id")) : -1;

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            PrintWriter out = response.getWriter();

            // Filtra per ID
            if (productId > 0) {
                Products product = Products.loadById(productId, conn);
                if (product != null) {
                    out.write(gson.toJson(product));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
                }
            }
            // Ricerca per nome
            else if (searchQuery != null) {
                List<Products> productList = Products.searchByName(searchQuery, conn);
                out.write(gson.toJson(productList));
            }
            // Filtra per prezzo e categorie
            else if (minPrice != null || maxPrice != null || categoriesParam != null) {
                List<Products> productList = Products.filterByPriceAndCategory(minPrice, maxPrice, categoriesParam, conn);
                out.write(gson.toJson(productList));
            }
            // Carica tutti i prodotti
            else {
                List<Products> productList = Products.loadAll(conn);
                out.write(gson.toJson(productList));
            }
        } catch (SQLException e) {
            throw new ServletException("Error retrieving products", e);
        }
    }


    // POST: Crea un nuovo prodotto
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        BufferedReader in = request.getReader();
        Products newProduct = gson.fromJson(in, Products.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            int productId = newProduct.saveAsNew(conn);
            if (productId > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().println(gson.toJson(newProduct));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Product creation failed");
            }
        } catch (SQLException e) {
            throw new ServletException("Error creating product", e);
        }
    }

    // PUT: Aggiorna un prodotto esistente
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // il tuo frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        BufferedReader in = request.getReader();
        Products product = gson.fromJson(in, Products.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            boolean updated = product.saveUpdate(conn);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(gson.toJson(product));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Product update failed");
            }
        } catch (SQLException e) {
            throw new ServletException("Error updating product", e);
        }
    }

    // DELETE: Cancella un prodotto
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");  // il tuo frontend gira su localhost:3000
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        int productId = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            Products product = Products.loadById(productId, conn);
            if (product != null) {
                boolean deleted = product.delete(conn);
                if (deleted) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Product deletion failed");
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            }
        } catch (SQLException e) {
            throw new ServletException("Error deleting product", e);
        }
    }

    public void destroy() {
    }
}
