package panther_stock_management.backend.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-API-Key";

    private final String apiKey;

    public ApiKeyAuthFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER_NAME);

        if (apiKey != null && apiKey.equals(provided)) {
            Authentication authentication = new UsernamePasswordAuthenticationToken("api-client", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API key inválida ou ausente");
    }
}
