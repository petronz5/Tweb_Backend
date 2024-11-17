package tweb.titancommerce.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {"/orders", "/payment", "/profile", "/usercart"}) // Intercetta tutte le richieste
public class AuthFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        // Imposta gli header CORS per ogni richiesta
        res.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Permetti sempre le richieste OPTIONS
        if (req.getMethod().equalsIgnoreCase("OPTIONS")) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Permetti l'accesso alle rotte di login e logout senza autenticazione
        if (req.getServletPath().equals(LoginService.LOGIN_PATH) ||
                req.getServletPath().equals(LoginService.LOGOUT_PATH)) {
            chain.doFilter(req, res);
            return;
        }

        // Verifica se l'utente è autenticato
        String currentUser = LoginService.getCurrentLogin(req.getSession());
        if (currentUser != null && !currentUser.isEmpty()) {
            chain.doFilter(req, res);
            return;
        }

        // Se l'utente non è autenticato, invia un errore 401 Unauthorized.
        // Assicurati che gli header CORS siano impostati prima di inviare l'errore
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
    }
}
