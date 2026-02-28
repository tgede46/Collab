package net.gedeon.Collab.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.entitie.user.User;
import net.gedeon.Collab.repository.UserRepository;

/**
 * Service personnalisé pour charger les détails d'un utilisateur
 *
 * Implémentation de UserDetailsService pour Spring Security
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Charge un utilisateur par son ID (utilisé par JWT)
     *
     * Note: Le username est en fait l'ID utilisateur (UUID)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UUID userId = UUID.fromString(username);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

            // Retourner un UserDetails Spring Security
            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getId().toString())
                    .password(user.getPassword()) // Le mot de passe hashé
                    .authorities(new ArrayList<>()) // Pas de rôles spécifiques pour le moment
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid user ID format: " + username);
        }
    }

    /**
     * Charge un utilisateur par son email (utilisé pour la connexion)
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .authorities(new ArrayList<>())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
