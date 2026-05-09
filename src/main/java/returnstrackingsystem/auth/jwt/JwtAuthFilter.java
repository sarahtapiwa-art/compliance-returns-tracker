package returnstrackingsystem.auth.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import returnstrackingsystem.auth.CustomUserDetailsService;

import java.io.*;
import java.util.stream.Collectors;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 09:06
 * projectName compliance-returns-tracker
 **/

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String requestPath = request.getServletPath();

        boolean isOutlookEndpoint = requestPath.matches("/api/v1/submissions/\\d+/send-outlook");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        if (isOutlookEndpoint) {
            handleMicrosoftGraphToken(request, token);
            filterChain.doFilter(request, response);
            return;
        }

        handleAppJwtToken(request, token);
        filterChain.doFilter(request, response);
    }

    private void handleAppJwtToken(HttpServletRequest request, String token) {
        try {
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authentication successful for user: {}", username);
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process JWT token: {}", e.getMessage());
        }
    }

    private void handleMicrosoftGraphToken(HttpServletRequest request, String token) {
        try {
            String userEmail = extractUserEmailFromRequest(request);

            if (userEmail != null) {
                UserDetails userDetails = userDetailsService.loadUserByEmail(userEmail);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Microsoft Graph token processed for user: {}", userEmail);
                } else {
                    log.warn("User not found for Microsoft token: {}", userEmail);
                }
            } else {
                log.warn("Could not extract user email from Microsoft token request");
            }
        } catch (Exception e) {
            log.error("Failed to process Microsoft Graph token: {}", e.getMessage(), e);
        }
    }

    private String extractUserEmailFromRequest(HttpServletRequest request) {
        try {
            if ("POST".equalsIgnoreCase(request.getMethod()) &&
                    request.getContentType() != null &&
                    request.getContentType().contains("application/json")) {

                CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

                String body = cachedRequest.getReader().lines().collect(Collectors.joining());

                if (!body.isEmpty()) {
                    JsonNode jsonNode = new ObjectMapper().readTree(body);
                    if (jsonNode.has("email")) {
                        return jsonNode.get("email").asText();
                    }
                }

                return null;
            }


            String emailHeader = request.getHeader("X-User-Email");
            if (emailHeader != null && !emailHeader.trim().isEmpty()) {
                return emailHeader.trim();
            }

            return null;
        } catch (Exception e) {
            log.warn("Failed to extract user email from request: {}", e.getMessage());
            return null;
        }
    }

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = requestInputStream.readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(this.cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream));
        }
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final InputStream cachedBodyInputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            try {
                return cachedBodyInputStream.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return cachedBodyInputStream.read();
        }
    }
}