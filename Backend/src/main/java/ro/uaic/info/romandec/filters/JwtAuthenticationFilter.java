package ro.uaic.info.romandec.filters;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    //TODO: Hide this somewhere else. For test purposes only
    private final static String SECRET_KEY = "nLGz0KP+2dEbp2wxuT3oTsdfjlkhsfjksdhfulhsdfksdhfqR72ApYhMNq";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if(authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            var jwt = authHeader.substring(TOKEN_PREFIX.length());
        }

    }
}

