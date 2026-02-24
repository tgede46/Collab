package net.gedeon.Collab.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.auth.AuthResponse;
import net.gedeon.Collab.dto.auth.LoginRequest;
import net.gedeon.Collab.dto.auth.RegisterRequest;
import net.gedeon.Collab.entitie.user.RefreshToken;
import net.gedeon.Collab.entitie.user.User;
import net.gedeon.Collab.repository.UserRepository;

/**
 * AuthController - Gestion de l'authentification
 *
 * Endpoints publics pour l'inscription, la connexion et le rafraîchissement des
 * tokens
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // TODO: Injecter un JwtService pour générer les tokens

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        // Vérifier si le username existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Générer les tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // Trouver l'utilisateur par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Vérifier si le compte est actif
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        // Générer les tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Rafraîchir le token d'accès
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {

        // TODO: Implémenter la logique de validation et rafraîchissement du token
        // 1. Valider le refresh token
        // 2. Récupérer l'utilisateur
        // 3. Générer un nouveau access token
        // 4. Retourner la réponse

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Refresh token not implemented yet");
    }

    /**
     * Déconnexion (révocation du refresh token)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String refreshToken) {

        // TODO: Implémenter la logique de révocation du refresh token
        // 1. Trouver le refresh token dans la base
        // 2. Le révoquer

        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Récupérer les informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {

        // TODO: Extraire l'utilisateur du JWT dans l'Authorization header
        // Pour l'instant, on retourne une erreur

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Get current user not implemented yet");
    }

    // ============== HELPER METHODS ==============

    private String generateAccessToken(User user) {
        // TODO: Implémenter la génération de JWT avec une durée de vie courte (15-30
        // min)
        // Pour l'instant, on retourne un UUID comme placeholder
        return "access_" + UUID.randomUUID().toString();
    }

    private String generateRefreshToken(User user) {
        // TODO: Implémenter la génération de JWT refresh token avec une durée de vie
        // longue (7-30 jours)
        // Sauvegarder le token dans la base de données

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash("refresh_" + UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();

        // TODO: Sauvegarder dans RefreshTokenRepository

        return refreshToken.getTokenHash();
    }
}
