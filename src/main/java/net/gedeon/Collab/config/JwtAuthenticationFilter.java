package net.gedeon.Collab.config;

import java.io.IOException;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.service.JwtService;

/**
 * Filtre d'authentification JWT
 *
 * Ce filtre intercepte toutes les requêtes HTTP pour extraire et valider
 * le token JWT présent dans l'en-tête Authorization.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Extraire le token JWT de l'en-tête Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si pas d'en-tête ou format incorrect, passer au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extraire le token (après "Bearer ")
            final String jwt = authHeader.substring(7);

            // 3. Extraire l'ID utilisateur du token
            final UUID userId = jwtService.extractUserId(jwt);

            // 4. Si l'utilisateur n'est pas déjà authentifié
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Charger les détails de l'utilisateur
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

                // 6. Valider le token
                if (jwtService.validateToken(jwt, userId)) {
                    // 7. Créer l'objet Authentication
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // 8. Ajouter les détails de la requête
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. Mettre l'authentification dans le SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log l'erreur et continuer (l'utilisateur ne sera pas authentifié)
            logger.error("Cannot set user authentication: {}", e);
        }

        // 10. Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
