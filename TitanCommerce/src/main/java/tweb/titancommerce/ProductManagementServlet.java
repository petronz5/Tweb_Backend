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

@WebServlet(name = "ProductManagementServlet", urlPatterns = {"/products"})
public class ProductManagementServlet extends HttpServlet {

    private Gson gson;

    public void init() {
        gson = new Gson();
    }

    // GET: Recupera uno o più prodotti
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String searchQuery = request.getParameter("search");
        String minPriceParam = request.getParameter("minPrice");
        String maxPriceParam = request.getParameter("maxPrice");
        String[] categoriesParam = request.getParameterValues("categories");

        BigDecimal minPrice = minPriceParam != null ? new BigDecimal(minPriceParam) : null;
        BigDecimal maxPrice = maxPriceParam != null ? new BigDecimal(maxPriceParam) : null;

        int productId = request.getParameter("id") != null ? Integer.parseInt(request.getParameter("id")) : -1;

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {

            // Filtra per ID
            if (productId > 0) {
                Products product = Products.loadById(productId, conn);
                if (product != null) {
                    out.write(gson.toJson(product));
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
                }
            }
            // Ricerca per nome
            else if (searchQuery != null) {
                List<Products> productList = Products.searchByName(searchQuery, conn);
                if (!productList.isEmpty()) {
                    out.write(gson.toJson(productList));
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No products found matching the search criteria");
                }
            }
            // Filtra per prezzo e categorie
            else if (minPrice != null || maxPrice != null || categoriesParam != null) {
                List<Products> productList = Products.filterByPriceAndCategory(minPrice, maxPrice, categoriesParam, conn);
                if (!productList.isEmpty()) {
                    out.write(gson.toJson(productList));
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No products found matching the filter criteria");
                }
            }
            // Carica tutti i prodotti
            else {
                List<Products> productList = Products.loadAll(conn);
                if (!productList.isEmpty()) {
                    out.write(gson.toJson(productList));
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No products available");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving products");
        } finally {
            out.close();
        }
    }

    // POST: Crea un nuovo prodotto
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

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
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating product");
        }
    }

    // PUT: Aggiorna un prodotto esistente
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        BufferedReader in = request.getReader();
        Products product = gson.fromJson(in, Products.class);

        try (Connection conn = PoolingPersistenceManager.getPersistenceManager().getConnection()) {
            // Verifica se il prodotto esiste
            Products existingProduct = Products.loadById(product.getId(), conn);
            if (existingProduct == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
                return;
            }

            boolean updated = product.saveUpdate(conn);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(gson.toJson(product));
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Product update failed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating product");
        }
    }

    // DELETE: Cancella un prodotto
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        String productIdParam = request.getParameter("id");
        if (productIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product ID is required");
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Product ID");
            return;
        }

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
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting product");
        }
    }

    // Gestione delle richieste OPTIONS per CORS preflight
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Metodo per impostare gli header CORS
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    public void destroy() {
    }
}
