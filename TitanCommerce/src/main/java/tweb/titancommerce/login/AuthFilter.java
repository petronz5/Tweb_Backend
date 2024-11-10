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

        // Se l'utente non è autenticato, invia un errore 401 Unauthorized
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
    }
}
