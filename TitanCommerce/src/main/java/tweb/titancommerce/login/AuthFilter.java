package tweb.titancommerce.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebFilter(urlPatterns = "*")
public class AuthFilter extends HttpFilter {
    /*
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req.getServletPath().equals(LoginService.LOGIN_PATH) ||
                req.getServletPath().equals(LoginService.LOGOUT_PATH)) {
            chain.doFilter(req, res);
            return;
        }

        if (!LoginService.getCurrentLogin(req.getSession()).isEmpty()) {
            chain.doFilter(req, res);
            return;
        }

        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        chain.doFilter(req, res);
    }

     */
}
