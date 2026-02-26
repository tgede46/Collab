package net.gedeon.Collab.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.auth.AuthResponse;
import net.gedeon.Collab.dto.auth.LoginRequest;
import net.gedeon.Collab.dto.auth.RegisterRequest;
import net.gedeon.Collab.entitie.user.User;
import net.gedeon.Collab.service.AuthService;
import net.gedeon.Collab.service.JwtService;
import net.gedeon.Collab.service.UserService;

/**
 * AuthController - Gestion de l'authentification
 *
 * Endpoints publics pour l'inscription, la connexion et le rafraîchissement des
 * tokens
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints d'authentification")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Rafraîchir le token d'accès
     */
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        AuthResponse response = authService.refreshTokens(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Déconnexion (révocation du refresh token)
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion de l'utilisateur")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UUID userId = jwtService.extractUserId(token);
        authService.logout(userId);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Récupérer les informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    @Operation(summary = "Obtenir les informations de l'utilisateur connecté")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UUID userId = jwtService.extractUserId(token);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}
