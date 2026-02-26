# 🎯 API Controllers - Documentation

Cette documentation détaille tous les controllers REST implémentés pour l'application **Collab** avec leur matrice de permissions.

## 📊 Matrice des Permissions

| Action                       | OWNER | EDITOR | VIEWER | Guest |
| ---------------------------- | ----- | ------ | ------ | ----- |  
| **Workspace**                |
| Créer un workspace           | ✅    | ✅     | ✅     | ❌    |
| Inviter des membres          | ✅    | ❌     | ❌     | ❌    |
| Supprimer le workspace       | ✅    | ❌     | ❌     | ❌    |
| **Document**                 |
| Créer un document            | ✅    | ✅     | ❌     | ❌    |
| Éditer un document           | ✅    | ✅     | ❌     | ❌    |
| Lire un document             | ✅    | ✅     | ✅     | ✅    |
| Supprimer un document        | ✅    | ❌     | ❌     | ❌    |
| **Collaboration**            |
| Voir les curseurs temps réel | ✅    | ✅     | ✅     | ✅    |
| **Versions**                 |
| Historique des versions      | ✅    | ✅     | ✅     | ❌    |
| Restaurer une version        | ✅    | ✅     | ❌     | ❌    |
| **Export**                   |
| Exporter un document         | ✅    | ✅     | ✅     | ✅    |

---

## 🔐 AuthController

**Base URL**: `/api/auth`

### Endpoints

#### POST `/register`

Inscription d'un nouvel utilisateur.

**Request Body**:

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response**: `AuthResponse` avec tokens JWT

---

#### POST `/login`

Connexion d'un utilisateur existant.

**Request Body**:

```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response**: `AuthResponse` avec tokens JWT

---

#### POST `/refresh`

Rafraîchir le token d'accès.

**Query Param**: `refreshToken`

---

#### POST `/logout`

Déconnexion (révocation du refresh token).

---

#### GET `/me`

Récupérer les informations de l'utilisateur connecté.

**Headers**: `Authorization: Bearer {token}`

---

## 🏢 WorkspaceController

**Base URL**: `/api/workspaces`

### Permissions

- **OWNER**: Toutes actions (créer, inviter, supprimer)
- **EDITOR/VIEWER**: Lecture uniquement

### Endpoints

#### POST `/`

Créer un nouveau workspace. ✅ **OWNER** (tout utilisateur devient owner de son workspace)

**Request Body**:

```json
{
  "name": "Mon Projet",
  "description": "Description du projet"
}
```

---

#### GET `/`

Récupérer tous les workspaces de l'utilisateur connecté.

**Response**: Liste de `WorkspaceResponse`

---

#### GET `/{workspaceId}`

Récupérer un workspace spécifique.

**Response**: `WorkspaceResponse` avec détails complets

---

#### POST `/{workspaceId}/members`

Inviter un membre au workspace. ✅ **OWNER uniquement**

**Request Body**:

```json
{
  "email": "user@example.com",
  "role": "EDITOR"
}
```

---

#### DELETE `/{workspaceId}/members/{memberId}`

Retirer un membre du workspace. ✅ **OWNER uniquement**

---

#### DELETE `/{workspaceId}`

Supprimer un workspace. ✅ **OWNER uniquement**

---

## 📄 DocumentController

**Base URL**: `/api/workspaces/{workspaceId}/documents`

### Permissions

- **OWNER**: Toutes actions
- **EDITOR**: Créer, Éditer, Lire
- **VIEWER**: Lire uniquement
- **Guest**: Lire uniquement (via ShareLink)

### Endpoints

#### POST `/`

Créer un nouveau document. ✅ **OWNER, EDITOR**

**Request Body**:

```json
{
  "title": "Mon Document",
  "content": "Contenu initial..."
}
```

---

#### GET `/`

Récupérer tous les documents du workspace.

**Response**: Liste de `DocumentResponse`

---

#### GET `/{documentId}`

Récupérer un document spécifique. ✅ **OWNER, EDITOR, VIEWER, Guest**

**Response**: `DocumentResponse` avec contenu complet

---

#### PUT `/{documentId}`

Mettre à jour un document. ✅ **OWNER, EDITOR**

**Request Body**:

```json
{
  "title": "Nouveau titre",
  "content": "Nouveau contenu...",
  "clientVersion": 42
}
```

**Gestion des conflits**: Le `clientVersion` permet de détecter les conflits d'édition.

---

#### DELETE `/{documentId}`

Supprimer un document. ✅ **OWNER uniquement**

---

#### GET `/{documentId}/export`

Exporter un document. ✅ **OWNER, EDITOR, VIEWER, Guest**

**Query Param**: `format` (txt, md, pdf...)

---

## 🤝 CollaborationController

**Base URL**: `/api/documents/{documentId}/collaboration`

### Permissions

- **Voir curseurs**: Tous (OWNER, EDITOR, VIEWER, Guest)
- **Appliquer opérations**: OWNER, EDITOR uniquement

### Endpoints

#### POST `/sessions`

Démarrer une session d'édition. ✅ **Tous**

**Response**: `SessionResponse` avec sessionId et couleur de curseur

---

#### GET `/sessions`

Récupérer toutes les sessions actives. ✅ **Tous**

**Response**: Liste de `SessionResponse` avec positions des curseurs

---

#### PUT `/sessions/{sessionId}/cursor`

Mettre à jour la position du curseur. ✅ **Tous**

**Query Param**: `position` (index dans le texte)

---

#### DELETE `/sessions/{sessionId}`

Terminer une session d'édition.

---

#### POST `/operations`

Appliquer une opération (INSERT ou DELETE) avec OT. ✅ **OWNER, EDITOR**

**Request Body**:

```json
{
  "type": "INSERT",
  "position": 42,
  "content": "Hello",
  "clientVersion": 10
}
```

**Algorithme OT**: L'opération est automatiquement transformée contre les opérations concurrentes.

---

#### GET `/operations`

Récupérer l'historique des opérations.

**Query Param**: `sinceVersion` (optionnel)

---

## 📚 VersionController

**Base URL**: `/api/documents/{documentId}/versions`

### Permissions

- **Voir historique**: OWNER, EDITOR, VIEWER
- **Restaurer**: OWNER, EDITOR
- **Supprimer snapshot**: OWNER uniquement

### Endpoints

#### POST `/snapshots`

Créer un snapshot manuel du document. ✅ **OWNER, EDITOR**

**Response**: `SnapshotResponse`

---

#### GET `/snapshots`

Récupérer l'historique des versions. ✅ **OWNER, EDITOR, VIEWER**

**Query Param**: `limit` (défaut: 20)

**Response**: Liste de `SnapshotResponse` triée par date décroissante

---

#### GET `/snapshots/{snapshotId}`

Récupérer un snapshot spécifique. ✅ **OWNER, EDITOR, VIEWER**

---

#### POST `/snapshots/{snapshotId}/restore`

Restaurer une version précédente. ✅ **OWNER, EDITOR**

**Effet**: Le document est restauré au contenu du snapshot, un nouveau snapshot est créé automatiquement.

---

#### GET `/compare`

Comparer deux versions.

**Query Params**: `snapshot1Id`, `snapshot2Id`

**Response**: Différences entre les deux versions (TODO: implémenter algorithme diff)

---

#### DELETE `/snapshots/{snapshotId}`

Supprimer un snapshot. ✅ **OWNER uniquement**

---

## 🛠️ Service: PermissionService

Service centralisé pour la gestion des permissions. Utilisé par tous les controllers pour vérifier les droits d'accès.

### Méthodes principales

```java
// Workspace
boolean canCreateWorkspace(UUID userId)
boolean canInviteToWorkspace(UUID workspaceId, UUID userId)
boolean canDeleteWorkspace(UUID workspaceId, UUID userId)
boolean canAccessWorkspace(UUID workspaceId, UUID userId)

// Document
boolean canCreateDocument(UUID workspaceId, UUID userId)
boolean canEditDocument(UUID documentId, UUID userId)
boolean canReadDocument(UUID documentId, UUID userId)
boolean canDeleteDocument(UUID documentId, UUID userId)

// Versions
boolean canViewVersionHistory(UUID documentId, UUID userId)
boolean canRestoreVersion(UUID documentId, UUID userId)

// Export
boolean canExportDocument(UUID documentId, UUID userId)

// Collaboration
boolean canViewCursors(UUID documentId, UUID userId)
```

---

## 🔄 Flux d'Édition Collaborative

### 1. Connexion au document

```
Client → POST /collaboration/sessions
Server → SessionResponse { sessionId, color, ... }
```

### 2. Édition temps réel

```
Client tape "H" à position 5
Client → POST /collaboration/operations {
  type: INSERT,
  position: 5,
  content: "H",
  clientVersion: 10
}

Server:
1. Récupère les opérations concurrentes (version > 10)
2. Transforme l'opération avec OT
3. Applique au document
4. Sauvegarde
5. Publie via Kafka (TODO)

Server → Operation { serverVersion: 11, ... }
```

### 3. Synchronisation des curseurs

```
Client bouge curseur → PUT /sessions/{id}/cursor?position=42
Server → CursorEvent publié via Kafka (TODO)
Autres clients reçoivent la position du curseur
```

---

## 📦 DTOs (Data Transfer Objects)

### Auth

- `LoginRequest`: email, password
- `RegisterRequest`: username, email, password
- `AuthResponse`: userId, tokens, tokenType

### Workspace

- `CreateWorkspaceRequest`: name, description
- `InviteMemberRequest`: email, role
- `WorkspaceResponse`: id, name, description, userRole, memberCount

### Document

- `CreateDocumentRequest`: title, content
- `UpdateDocumentRequest`: title, content, clientVersion
- `DocumentResponse`: id, title, content, version, userPermission, activeCollaborators

### Collaboration

- `OperationRequest`: type, position, content, length, clientVersion
- `SessionResponse`: sessionId, userId, username, cursorPosition, color

### Versions

- `SnapshotResponse`: id, documentId, content, versionAt, createdAt

---

## 🚀 Prochaines Étapes (TODO)

1. **JWT Authentication**
   - Implémenter JwtService pour génération/validation des tokens
   - Configurer Spring Security avec JWT
   - Filtres d'authentification

2. **Kafka Integration**
   - Producer pour CursorEvent et Operation
   - Consumer pour synchronisation temps réel
   - Topics: `cursor-updates`, `document-operations`

3. **ShareLink**
   - Implémenter génération de liens de partage
   - Validation et gestion des expirations
   - Accès Guest via token

4. **WebSocket**
   - Alternative/complément à Kafka pour temps réel
   - Notifications push aux clients connectés

5. **Rate Limiting**
   - Limiter les opérations par utilisateur
   - Protection contre le spam

6. **Caching**
   - Redis pour sessions actives
   - Cache des permissions fréquentes

---

## 📝 Notes d'Implémentation

### Gestion des Versions (Optimistic Locking)

Le champ `version` de `Document` utilise `@Version` de JPA pour le verrouillage optimiste. Cela évite les conflits d'édition simultanée au niveau de la base de données.

### Operational Transformation (OT)

L'algorithme OT dans `OTEngine` garantit:

- **Convergence**: Tous les clients finissent avec le même document
- **Préservation d'intention**: Chaque opération respecte l'intention de l'utilisateur
- **Commutativité**: L'ordre d'application des opérations ne change pas le résultat final

### Sessions d'Édition

Les sessions expirent après 5 minutes d'inactivité (`lastSeen` < now - 5min).
Un job de nettoyage périodique devrait être implémenté.

---

Compilé avec succès ✅
Spring Boot 4.0.3 | Java 21 | PostgreSQL | Kafka
