package tweb.titancommerce.login;

import jakarta.servlet.http.HttpSession;
import tweb.titancommerce.models.Users;
import java.sql.SQLException;

public class LoginService {
    private final static String SESSION_USER_KEY = "user";  // Chiave per l'username nella sessione
    public static final String LOGIN_PATH = "/login";
    public final static String LOGOUT_PATH = "/logout";

    /*
     * Restituisce l'username dell'utente attualmente loggato
     */
    public static String getCurrentLogin(HttpSession session) {
        if (session.getAttribute(SESSION_USER_KEY) == null) return "";
        return (String) session.getAttribute(SESSION_USER_KEY);
    }

    /*
     * Effettua il login per un utente dato l'username.
     * Ritorna true se il login ha successo, false altrimenti.
     */
    public static boolean doLogIn(HttpSession session, String username) throws SQLException {
        if (session.getAttribute(SESSION_USER_KEY) == null) {
            session.setAttribute(SESSION_USER_KEY, username);

            // Recupera il ruolo dell'utente dal database
            String role = Users.getRoleByUsername(username);
            session.setAttribute("role", role);  // Salva il ruolo nella sessione

            // Imposta la durata della sessione (5 minuti)
            session.setMaxInactiveInterval(5 * 60);
            return true;
        }
        // Controlla se l'utente attualmente loggato è lo stesso
        String loggedUser = (String) session.getAttribute(SESSION_USER_KEY);
        if (loggedUser.equals(username)) return true;
        return false;
    }

    /*
     * Effettua il logout dell'utente dalla sessione.
     */
    public static boolean doLogOut(HttpSession session, String username) {
        if (session.getAttribute(SESSION_USER_KEY) == null)
            return true;
        if (((String) session.getAttribute(SESSION_USER_KEY)).equals(username)) {
            session.invalidate(); // Invalida la sessione
            return true;
        }
        return false;
    }
}
