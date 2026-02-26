package net.gedeon.Collab.service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.auth.AuthResponse;
import net.gedeon.Collab.dto.auth.LoginRequest;
import net.gedeon.Collab.dto.auth.RegisterRequest;
import net.gedeon.Collab.entitie.user.RefreshToken;
import net.gedeon.Collab.entitie.user.User;
import net.gedeon.Collab.repository.RefreshTokenRepository;
import net.gedeon.Collab.repository.UserRepository;

/**
 * Service de gestion de l'authentification
 *
 * Gère l'inscription, la connexion, le rafraîchissement des tokens et la
 * déconnexion
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final String[] AVATAR_COLORS = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
            "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B739", "#52B788"
    };

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email ou le username existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .avatarColor(getRandomAvatarColor())
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // Générer les tokens
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Connexion d'un utilisateur
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Trouver l'utilisateur par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Générer les tokens
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Rafraîchit les tokens avec un refresh token
     */
    @Transactional
    public AuthResponse refreshTokens(String refreshTokenValue) {
        // Valider le refresh token JWT
        if (!jwtService.validateToken(refreshTokenValue)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Extraire l'ID utilisateur
        UUID userId = jwtService.extractUserId(refreshTokenValue);

        // Vérifier que le refresh token existe et est valide dans la BDD
        String tokenHash = hashToken(refreshTokenValue);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        // Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Générer de nouveaux tokens
        String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String newRefreshToken = createRefreshToken(user);

        // Révoquer l'ancien refresh token
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Déconnexion - révoque tous les refresh tokens d'un utilisateur
     */
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * Crée et stocke un refresh token pour un utilisateur
     */
    private String createRefreshToken(User user) {
        String tokenValue = jwtService.generateRefreshToken(user.getId());
        String tokenHash = hashToken(tokenValue);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    /**
     * Hash un token pour le stocker en base de données
     */
    private String hashToken(String token) {
        // Utilise le passwordEncoder pour hasher le token
        return passwordEncoder.encode(token);
    }

    /**
     * Génère une couleur aléatoire pour l'avatar
     */
    private String getRandomAvatarColor() {
        Random random = new Random();
        return AVATAR_COLORS[random.nextInt(AVATAR_COLORS.length)];
    }

    /**
     * Nettoie les refresh tokens expirés (à appeler périodiquement)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
}
