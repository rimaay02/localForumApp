package util;

public class CsrfTokenUtil {
    private static String CSRF_TOKEN_SESSION = "csrfToken";

    public static void setCsrfTokenInSession(String token) {
        CSRF_TOKEN_SESSION = token;
    }
    public static String getCsrfTokenFromSession() {
        return CSRF_TOKEN_SESSION;
    }


}
