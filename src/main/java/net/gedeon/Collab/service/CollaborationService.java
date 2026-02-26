package net.gedeon.Collab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.entitie.collaboration.EditingSession;
import net.gedeon.Collab.entitie.collaboration.Operation;
import net.gedeon.Collab.entitie.collaboration.OperationType;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.repository.DocumentRepository;
import net.gedeon.Collab.repository.EditingSessionRepository;
import net.gedeon.Collab.repository.OperationRepository;

/**
 * Service de gestion de la collaboration en temps réel
 */
@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final EditingSessionRepository sessionRepository;
    private final OperationRepository operationRepository;
    private final DocumentRepository documentRepository;
    private final PermissionService permissionService;
    private final OTEngine otEngine;

    /**
     * Démarre une session d'édition pour un utilisateur sur un document
     */
    @Transactional
    public EditingSession startSession(UUID documentId, UUID userId) {
        // Vérifier les permissions
        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to access this document");
        }

        // Vérifier si une session active existe déjà
        sessionRepository.findByDocumentIdAndUserId(documentId, userId)
                .ifPresent(session -> {
                    // Mettre à jour la dernière activité
                    session.setLastSeen(LocalDateTime.now());
                    sessionRepository.save(session);
                });

        // Créer une nouvelle session
        String sessionId = UUID.randomUUID().toString();
        EditingSession session = EditingSession.builder()
                .documentId(documentId)
                .userId(userId)
                .sessionId(sessionId)
                .connectedAt(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .cursorPosition(0)
                .build();

        return sessionRepository.save(session);
    }

    /**
     * Termine une session d'édition
     */
    @Transactional
    public void endSession(String sessionId) {
        EditingSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.disconnect();
        sessionRepository.delete(session);
    }

    /**
     * Met à jour la position du curseur d'un utilisateur
     */
    @Transactional
    public EditingSession updateCursor(String sessionId, int position) {
        EditingSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.updateCursor(position);
        return sessionRepository.save(session);
    }

    /**
     * Récupère toutes les sessions actives d'un document
     */
    public List<EditingSession> getActiveSessions(UUID documentId) {
        return sessionRepository.findByDocumentId(documentId);
    }

    /**
     * Applique une opération d'édition en utilisant l'algorithme OT
     */
    @Transactional
    public Operation applyOperation(UUID documentId, UUID userId, OperationType type,
            int position, String content, int length, long clientVersion) {
        // Vérifier les permissions
        if (!permissionService.canEditDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to edit this document");
        }

        // Récupérer le document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Créer l'opération
        Operation operation = Operation.builder()
                .document(document)
                .userId(userId)
                .type(type)
                .position(position)
                .content(content)
                .length(length)
                .clientVersion(clientVersion)
                .serverVersion(document.getVersion())
                .appliedAt(LocalDateTime.now())
                .build();

        // Appliquer la transformation opérationnelle
        String newContent = otEngine.apply(document, operation);

        // Mettre à jour le document
        document.setContent(newContent);
        document.setVersion(document.getVersion() + 1);
        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

        // Sauvegarder l'opération
        return operationRepository.save(operation);
    }

    /**
     * Récupère l'historique des opérations d'un document
     */
    public List<Operation> getOperationHistory(UUID documentId, UUID userId) {
        // Vérifier les permissions
        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to view document operations");
        }

        return operationRepository.findByDocumentIdOrderByAppliedAtDesc(documentId);
    }

    /**
     * Récupère les opérations depuis une version spécifique
     */
    public List<Operation> getOperationsSince(UUID documentId, long version, UUID userId) {
        // Vérifier les permissions
        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to view document operations");
        }

        return operationRepository.findByDocumentIdAndServerVersionGreaterThan(documentId, version);
    }

    /**
     * Transforme une opération client pour la rendre compatible avec l'état serveur
     */
    public Operation transformOperation(Operation clientOp, List<Operation> serverOps) {
        return otEngine.transform(clientOp, serverOps);
    }

    /**
     * Nettoie les sessions inactives (à appeler périodiquement)
     */
    @Transactional
    public void cleanupInactiveSessions() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        List<EditingSession> inactiveSessions = sessionRepository.findByLastSeenBefore(timeoutThreshold);

        for (EditingSession session : inactiveSessions) {
            session.disconnect();
            sessionRepository.delete(session);
        }
    }

    /**
     * Obtient le nombre d'utilisateurs actifs sur un document
     */
    public long getActiveUserCount(UUID documentId) {
        return sessionRepository.countByDocumentId(documentId);
    }

    /**
     * Vérifie si un utilisateur est actuellement en train d'éditer un document
     */
    public boolean isUserActive(UUID documentId, UUID userId) {
        return sessionRepository.findByDocumentIdAndUserId(documentId, userId).isPresent();
    }

    /**
     * Diffuse une opération à tous les utilisateurs actifs (à implémenter avec
     * WebSocket/Kafka)
     */
    public void broadcastOperation(UUID documentId, Operation operation) {
        // TODO: Implémenter la diffusion via WebSocket ou Kafka
        // Cette méthode sera utilisée pour notifier tous les clients connectés
        // de la nouvelle opération
    }

    /**
     * Synchronise un client avec l'état actuel du serveur
     */
    @Transactional
    public Document synchronizeClient(UUID documentId, UUID userId, long clientVersion) {
        // Vérifier les permissions
        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to access this document");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Si le client est à jour, retourner null
        if (clientVersion == document.getVersion()) {
            return null;
        }

        // Sinon, retourner le document complet
        return document;
    }
}
