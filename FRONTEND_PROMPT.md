# 🎨 Guide de Développement Frontend - Collab API

## 📖 Contexte du Projet

**Collab** est une **API REST backend déployée** pour la collaboration documentaire en temps réel, similaire à Google Docs. L'API permet à plusieurs utilisateurs d'éditer des documents simultanément avec synchronisation en temps réel.

### 🚀 API Backend Déployée

- **Stack**: Spring Boot 4.0.3 + Java 21 + PostgreSQL + Kafka
- **Architecture**: API REST avec authentification JWT
- **Statut**: ✅ Déployée et opérationnelle
- **API Base URL Développement**: `http://localhost:8080/api`

### 📱 Plateformes Cibles

L'application cliente doit être développée pour :

- **🌐 Web**: Application web responsive (Desktop + Mobile web)
- **📱 Mobile**: Application native ou hybride (iOS + Android)
- **💻 Desktop**: Application desktop (Electron, Tauri ou PWA)

---

## 🎯 Mission Frontend

Créer des applications clientes multi-plateformes modernes, réactives et intuitives permettant:

1. **Authentification** (inscription, connexion, gestion de profil)
2. **Gestion des Workspaces** (création, invitation de membres, permissions)
3. **Édition collaborative de documents** en temps réel
4. **Visualisation des curseurs** des autres utilisateurs
5. **Gestion des versions** (historique, restauration)
6. **Partage de documents** via liens
7. **Export de documents** (txt, md, pdf)

---

## 🛠️ Stack Technologique

### 🌐 Web Application - Astro

**Stack Choisi: Astro + React/Vue Islands**

```
✅ Astro 4+ avec TypeScript
✅ Islands Architecture (composants interactifs isolés)
✅ React/Vue/Svelte pour les îles interactives
   - Éditeur collaboratif (React island)
   - Dashboard interactif (React/Vue island)
   - Composants temps réel (React island)
✅ TanStack Query (React Query) pour API calls
✅ Nanostores pour state management léger
✅ Socket.io-client pour WebSocket temps réel
✅ TailwindCSS pour styling
✅ Shadcn/ui ou DaisyUI pour composants
✅ Monaco Editor pour l'éditeur de code
```

**Architecture Astro**

```typescript
// Pages statiques Astro
src / pages / index.astro; // Landing page (statique)
login.astro; // Page login (minimal JS)
dashboard.astro; // Dashboard (avec islands React)

// Composants interactifs (Islands)
src /
  components /
  react / // Islands React pour interactivité
  DocumentEditor.tsx; // Éditeur collaboratif
WorkspaceList.tsx; // Liste interactive
islands / CollaborationWidget.tsx;

// Layouts
src / layouts / MainLayout.astro;
AuthLayout.astro;
```

**Avantages Astro:**

- ⚡ Performance exceptionnelle (0KB JS par défaut)
- 🎯 Hydratation partielle (islands)
- 📦 Build optimisé automatique
- 🔄 Support multi-framework
- 🎨 Excellent pour SEO

### 📱 Mobile Application (À Définir)

**Option 1: React Native (Recommandé)**

```
- React Native + TypeScript
- Expo (développement rapide)
- React Navigation
- TanStack Query
- Zustand ou Nanostores
- NativeWind (TailwindCSS pour mobile)
- React Native Paper ou Tamagui
- ✅ Partage de code avec Web (composants React)
```

**Option 2: Flutter**

```
- Flutter + Dart
- Provider / Riverpod pour state
- Dio pour HTTP
- Material Design 3
- ⚠️ Pas de partage de code avec Astro/Tauri
```

**Option 3: Capacitor (PWA Native)**

```
- Capacitor + Astro
- Réutilise l'application web Astro
- APIs natives pour iOS/Android
- ✅ Maximum de code partagé
- ⚠️ Performance légèrement inférieure à React Native
```

**Recommandation**: React Native pour performance native + partage de composants React avec Astro islands, OU Capacitor pour maximum de réutilisation du code Astro.

### 💻 Desktop Application - Tauri

**Stack Choisi: Tauri + Astro**

```
✅ Tauri 2.0 (Rust backend)
✅ Application web Astro (frontend)
✅ Partage du code avec l'app web
✅ APIs natives Rust pour:
   - Système de fichiers
   - Keychain (authentification)
   - Notifications système
   - Raccourcis clavier globaux
   - Auto-update
✅ Bundle ultra-léger (~3-5MB vs 100MB+ Electron)
✅ Performance native
✅ Sécurité renforcée
```

**Structure Tauri**

```bash
collab-desktop/
├── src-tauri/          # Backend Rust
│   ├── src/
│   │   ├── main.rs
│   │   ├── commands.rs  # Commands Tauri
│   │   └── menu.rs      # Menu natif
│   ├── Cargo.toml
│   └── tauri.conf.json
├── src/                # Frontend Astro (partagé avec web)
│   ├── pages/
│   ├── components/
│   └── layouts/
└── package.json
```

**Commandes Tauri (Rust)**

```rust
// Backend Rust pour features natives
#[tauri::command]
async fn save_document_locally(content: String) -> Result<(), String> {
    // Sauvegarde locale sécurisée
}

#[tauri::command]
fn get_system_keychain_token() -> Result<String, String> {
    // Récupérer token depuis keychain système
}
```

**Frontend Astro (appel des commands)**

```typescript
import { invoke } from "@tauri-apps/api/tauri";

await invoke("save_document_locally", { content: "Hello" });
const token = await invoke("get_system_keychain_token");
```

---

## 📋 Architecture API Backend

### 🔐 AuthController (`/api/auth`)

#### Endpoints disponibles

**POST `/api/auth/register`**

```json
Request:
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePass123"
}

Response: AuthResponse
{
  "userId": "uuid",
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "tokenType": "Bearer"
}
```

**POST `/api/auth/login`**

```json
Request:
{
  "email": "john@example.com",
  "password": "securePass123"
}

Response: AuthResponse (même structure)
```

**POST `/api/auth/refresh?refreshToken={token}`**

- Rafraîchir le access token

**POST `/api/auth/logout`**

- Déconnecter l'utilisateur

**GET `/api/auth/me`**

- Récupérer les infos de l'utilisateur connecté
- Headers: `Authorization: Bearer {accessToken}`

---

### 🏢 WorkspaceController (`/api/workspaces`)

**POST `/api/workspaces`** - Créer un workspace

```json
Request:
{
  "name": "Mon Projet",
  "description": "Description du projet"
}

Response: WorkspaceResponse
{
  "id": "uuid",
  "name": "Mon Projet",
  "description": "Description",
  "userRole": "OWNER",
  "memberCount": 1,
  "createdAt": "2026-02-28T10:00:00"
}
```

**GET `/api/workspaces`** - Liste des workspaces de l'utilisateur

**GET `/api/workspaces/{workspaceId}`** - Détails d'un workspace

**POST `/api/workspaces/{workspaceId}/members`** - Inviter un membre (OWNER uniquement)

```json
Request:
{
  "email": "user@example.com",
  "role": "EDITOR" // ou "VIEWER"
}
```

**DELETE `/api/workspaces/{workspaceId}/members/{memberId}`** - Retirer un membre (OWNER uniquement)

**DELETE `/api/workspaces/{workspaceId}`** - Supprimer workspace (OWNER uniquement)

---

### 📄 DocumentController (`/api/workspaces/{workspaceId}/documents`)

**POST `/api/workspaces/{workspaceId}/documents`** - Créer un document (OWNER, EDITOR)

```json
Request:
{
  "title": "Mon Document",
  "content": "Contenu initial..."
}

Response: DocumentResponse
{
  "id": "uuid",
  "title": "Mon Document",
  "content": "Contenu initial...",
  "version": 1,
  "userPermission": "EDITOR",
  "activeCollaborators": 1,
  "createdAt": "2026-02-28T10:00:00",
  "updatedAt": "2026-02-28T10:00:00"
}
```

**GET `/api/workspaces/{workspaceId}/documents`** - Liste des documents

**GET `/api/workspaces/{workspaceId}/documents/{documentId}`** - Détails d'un document

**PUT `/api/workspaces/{workspaceId}/documents/{documentId}`** - Mettre à jour (OWNER, EDITOR)

```json
Request:
{
  "title": "Nouveau titre",
  "content": "Nouveau contenu",
  "clientVersion": 42 // Pour gestion des conflits
}
```

**DELETE `/api/workspaces/{workspaceId}/documents/{documentId}`** - Supprimer (OWNER uniquement)

**GET `/api/workspaces/{workspaceId}/documents/{documentId}/export?format=pdf`** - Exporter

- Formats: `txt`, `md`, `pdf` (tous rôles)

---

### 🤝 CollaborationController (`/api/documents/{documentId}/collaboration`)

**POST `/api/documents/{documentId}/collaboration/sessions`** - Démarrer session d'édition

```json
Response: SessionResponse
{
  "sessionId": "uuid",
  "userId": "uuid",
  "username": "john_doe",
  "cursorPosition": 0,
  "color": "#FF5733", // Couleur du curseur
  "lastSeen": "2026-02-28T10:00:00"
}
```

**GET `/api/documents/{documentId}/collaboration/sessions`** - Sessions actives (tous les curseurs)

**PUT `/api/documents/{documentId}/collaboration/sessions/{sessionId}/cursor?position=42`**

- Mettre à jour position du curseur

**DELETE `/api/documents/{documentId}/collaboration/sessions/{sessionId}`**

- Terminer session

**POST `/api/documents/{documentId}/collaboration/operations`** - Appliquer opération (OWNER, EDITOR)

```json
Request: OperationRequest
{
  "type": "INSERT", // ou "DELETE"
  "position": 42,
  "content": "Hello", // pour INSERT
  "length": 5, // pour DELETE
  "clientVersion": 10
}

Response: Operation (avec serverVersion)
```

**GET `/api/documents/{documentId}/collaboration/operations?sinceVersion=10`**

- Récupérer historique des opérations

---

### 📚 VersionController (`/api/documents/{documentId}/versions`)

**POST `/api/documents/{documentId}/versions/snapshots`** - Créer snapshot manuel (OWNER, EDITOR)

**GET `/api/documents/{documentId}/versions/snapshots?limit=20`** - Historique des versions

```json
Response: SnapshotResponse[]
{
  "id": "uuid",
  "documentId": "uuid",
  "content": "Contenu du snapshot",
  "versionAt": 42,
  "createdAt": "2026-02-28T10:00:00"
}
```

**GET `/api/documents/{documentId}/versions/snapshots/{snapshotId}`** - Détails d'un snapshot

**POST `/api/documents/{documentId}/versions/snapshots/{snapshotId}/restore`** - Restaurer version (OWNER, EDITOR)

**GET `/api/documents/{documentId}/versions/compare?snapshot1Id=xxx&snapshot2Id=yyy`**

- Comparer deux versions (TODO backend)

**DELETE `/api/documents/{documentId}/versions/snapshots/{snapshotId}`** - Supprimer snapshot (OWNER uniquement)

---

## 🎨 Structure de l'Application Frontend

### Pages principales

```
/                           → Landing page (public)
/login                      → Page de connexion
/register                   → Page d'inscription
/dashboard                  → Dashboard avec liste des workspaces
/workspace/:id              → Vue d'un workspace avec ses documents
/document/:id/edit          → Éditeur de document collaboratif
/document/:id/versions      → Historique des versions
/profile                    → Profil utilisateur
/workspace/:id/settings     → Paramètres du workspace (membres, permissions)
```

---

## 🧩 Composants à Développer

### 1. **Authentification**

- [ ] `LoginForm` - Formulaire de connexion
- [ ] `RegisterForm` - Formulaire d'inscription
- [ ] `AuthGuard` - Protection des routes
- [ ] `TokenRefreshHandler` - Gestion auto du refresh token

### 2. **Workspaces**

- [ ] `WorkspaceList` - Liste des workspaces
- [ ] `WorkspaceCard` - Carte d'un workspace
- [ ] `CreateWorkspaceModal` - Modal création workspace
- [ ] `WorkspaceMembersList` - Liste des membres
- [ ] `InviteMemberModal` - Modal invitation membre
- [ ] `WorkspaceSettings` - Paramètres du workspace

### 3. **Documents**

- [ ] `DocumentList` - Liste des documents d'un workspace
- [ ] `DocumentCard` - Carte d'un document
- [ ] `CreateDocumentModal` - Modal création document
- [ ] `DocumentEditor` - Éditeur collaboratif temps réel
- [ ] `CollaboratorCursor` - Curseur d'un autre utilisateur
- [ ] `ActiveCollaboratorsList` - Liste des collaborateurs actifs
- [ ] `DocumentToolbar` - Barre d'outils (export, versions, share)

### 4. **Collaboration Temps Réel**

- [ ] `CursorManager` - Gestion des curseurs
- [ ] `OperationHandler` - Gestion des opérations INSERT/DELETE
- [ ] `ConflictResolver` - Résolution des conflits avec OT
- [ ] `SyncIndicator` - Indicateur de synchronisation
- [ ] `PresenceIndicator` - Indicateur de présence des utilisateurs

### 5. **Versions**

- [ ] `VersionHistory` - Historique des versions
- [ ] `VersionCard` - Carte d'une version
- [ ] `VersionComparator` - Comparaison de versions (diff)
- [ ] `RestoreVersionModal` - Modal restauration

### 6. **Partage (Future Feature)**

- [ ] `ShareModal` - Modal de partage avec lien
- [ ] `ShareLinkGenerator` - Générateur de liens
- [ ] `ShareLinkSettings` - Paramètres du lien (expiration, permissions)

### 7. **Export**

- [ ] `ExportModal` - Modal d'export
- [ ] `ExportFormatSelector` - Sélecteur de format

### 8. **UI Communs**

- [ ] `Navbar` - Barre de navigation
- [ ] `Sidebar` - Barre latérale
- [ ] `LoadingSpinner` - Spinner de chargement
- [ ] `ErrorBoundary` - Gestion des erreurs
- [ ] `Toast/Notification` - Notifications
- [ ] `ConfirmDialog` - Dialog de confirmation

---

## 🔄 Synchronisation Temps Réel

### Approche actuelle (Polling)

```typescript
// Récupérer les opérations toutes les 500ms
setInterval(() => {
  fetchOperations(lastVersion).then((ops) => applyOperations(ops));
}, 500);
```

### Future Feature: WebSocket

```typescript
// Se connecter au WebSocket
const socket = io("http://localhost:8080");

socket.on("operation", (op) => {
  applyOperation(op);
});

socket.on("cursor-update", (cursor) => {
  updateCollaboratorCursor(cursor);
});

// Émettre opération
socket.emit("operation", operationData);
```

---

## 📊 Système de Permissions

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

**Implémenter dans le frontend**: Afficher/Masquer les actions selon le rôle (`userPermission` ou `userRole`)

---

## 🚀 Fonctionnalités Futures à Intégrer

### 1. **WebSocket/Socket.io** (Haute Priorité)

- Remplacer le polling par WebSocket pour temps réel
- Événements: `operation`, `cursor-update`, `user-join`, `user-leave`
- Backend: À implémenter (actuellement prévu)

### 2. **ShareLink - Partage de Documents** (Haute Priorité)

```typescript
// Générer un lien de partage
POST /api/documents/{id}/share
{
  "expiresIn": 3600, // secondes
  "permission": "VIEWER" // ou "EDITOR"
}

Response:
{
  "shareLink": "https://collab.app/s/abc123xyz",
  "token": "abc123xyz",
  "expiresAt": "2026-03-01T10:00:00"
}

// Accéder via lien
GET /s/{token} → Redirection vers document avec accès Guest
```

Interface à créer:

- Modal de partage avec options
- Gestion de l'expiration
- Liste des liens actifs
- Révocation de liens

### 3. **Rich Text Editor** (Moyenne Priorité)

- Actuellement: texte brut
- Future: Formatage (gras, italique, listes, liens, images)
- Options recommandées:
  - **Slate.js**: Flexible, React-friendly
  - **Tiptap**: Vue.js mais compatible React
  - **Quill**: Simple mais moins customizable

### 4. **Notifications** (Moyenne Priorité)

- Notifications push pour:
  - Invitation à un workspace
  - Modification importante d'un document
  - Commentaires (future feature)
- Backend: À implémenter avec Firebase Cloud Messaging ou Socket.io

### 5. **Commentaires et Annotations** (Basse Priorité)

- Permettre d'ajouter des commentaires sur le document
- Thread de discussions
- Résolution de commentaires

### 6. **Mode Hors-Ligne** (Basse Priorité)

- Édition locale avec synchronisation ultérieure
- Service Worker + IndexedDB

### 7. **Recherche Globale** (Moyenne Priorité)

- Rechercher dans tous les documents
- Filtres avancés
- Backend: À implémenter avec Elasticsearch

### 8. **Analytics Dashboard** (Basse Priorité)

- Statistiques d'utilisation
- Activité des membres
- Temps d'édition

---

## 🎯 Roadmap Recommandée

### Phase 1: MVP (2-3 semaines)

1. ✅ Setup du projet frontend
2. ✅ Authentification (login, register, JWT)
3. ✅ Dashboard avec liste des workspaces
4. ✅ Création/Suppression de workspace
5. ✅ Liste des documents
6. ✅ Création/Suppression de document
7. ✅ Éditeur de base (sans temps réel)

### Phase 2: Collaboration (2-3 semaines)

1. ✅ Éditeur collaboratif avec polling
2. ✅ Affichage des curseurs
3. ✅ Gestion des opérations INSERT/DELETE
4. ✅ Indicateur de présence
5. ✅ Gestion des conflits (OT côté client)

### Phase 3: Permissions & Membres (1-2 semaines)

1. ✅ Invitation de membres
2. ✅ Gestion des rôles (OWNER, EDITOR, VIEWER)
3. ✅ Affichage conditionnel selon permissions
4. ✅ Settings du workspace

### Phase 4: Versions (1 semaine)

1. ✅ Historique des versions
2. ✅ Restauration de version
3. ✅ Comparaison de versions (diff)
4. ✅ Snapshots manuels

### Phase 5: Export & Partage (1-2 semaines)

1. ✅ Export (txt, md, pdf)
2. ✅ Génération de ShareLink
3. ✅ Accès Guest via lien
4. ✅ Gestion des expirations

### Phase 6: Améliorations (2-3 semaines)

1. ✅ Remplacer polling par WebSocket
2. ✅ Rich text editor
3. ✅ Notifications temps réel
4. ✅ Optimisations performances

### Phase 7: Features Avancées (optionnel)

1. Commentaires et annotations
2. Recherche globale
3. Mode hors-ligne
4. Analytics

---

## 💡 Considérations Techniques

### Gestion de l'État

```typescript
// Store global (Zustand exemple)
interface AppState {
  user: User | null;
  workspaces: Workspace[];
  currentDocument: Document | null;
  activeCollaborators: Collaborator[];
  operations: Operation[];

  // Actions
  login: (credentials) => Promise<void>;
  logout: () => void;
  fetchWorkspaces: () => Promise<void>;
  createDocument: (data) => Promise<Document>;
  applyOperation: (op: Operation) => void;
}
```

### Gestion des Opérations (OT)

```typescript
interface Operation {
  type: "INSERT" | "DELETE";
  position: number;
  content?: string;
  length?: number;
  clientVersion: number;
  serverVersion?: number;
}

// Transformer une opération locale contre une opération serveur
function transform(localOp: Operation, serverOp: Operation): Operation {
  if (serverOp.type === "INSERT") {
    if (serverOp.position <= localOp.position) {
      return {
        ...localOp,
        position: localOp.position + serverOp.content.length,
      };
    }
  } else if (serverOp.type === "DELETE") {
    if (serverOp.position < localOp.position) {
      return { ...localOp, position: localOp.position - serverOp.length };
    }
  }
  return localOp;
}
```

### Gestion des Curseurs

```typescript
interface Cursor {
  userId: string;
  username: string;
  position: number;
  color: string;
}

// Afficher curseur dans l'éditeur
function renderCursor(cursor: Cursor, editorElement: HTMLElement) {
  const cursorEl = document.createElement("div");
  cursorEl.style.position = "absolute";
  cursorEl.style.left = `${calculatePosition(cursor.position)}px`;
  cursorEl.style.borderLeft = `2px solid ${cursor.color}`;
  cursorEl.title = cursor.username;
  editorElement.appendChild(cursorEl);
}
```

### Polling optimisé

```typescript
// Polling intelligent avec backoff
let pollInterval = 500; // ms
const maxPollInterval = 5000;

function startPolling() {
  const poll = async () => {
    const ops = await fetchOperations(lastVersion);

    if (ops.length > 0) {
      applyOperations(ops);
      pollInterval = 500; // Réduire intervalle si activité
    } else {
      pollInterval = Math.min(pollInterval * 1.5, maxPollInterval);
    }

    setTimeout(poll, pollInterval);
  };

  poll();
}
```

### 📱 Considérations Multi-Plateformes

#### Partage de Code

**Stratégie recommandée : Monorepo avec Astro + Tauri + React Native**

```bash
collab-app/
├── packages/
│   ├── shared/          # Code partagé (types, utils, API client)
│   │   ├── src/
│   │   │   ├── api/     # CollabAPIClient
│   │   │   ├── types/   # TypeScript interfaces
│   │   │   ├── utils/   # Fonctions utilitaires
│   │   │   └── stores/  # Nanostores (partagé)
│   │   └── package.json
│   ├── ui/              # Composants UI partagés (React)
│   │   ├── src/
│   │   │   ├── DocumentEditor.tsx
│   │   │   ├── WorkspaceCard.tsx
│   │   │   └── theme/   # Design system
│   │   └── package.json
├── apps/
│   ├── web/             # Application Astro
│   │   ├── src/
│   │   │   ├── pages/
│   │   │   ├── components/
│   │   │   │   └── react/  # Islands React utilisant @collab/ui
│   │   │   └── layouts/
│   │   ├── astro.config.mjs
│   │   └── package.json
│   ├── desktop/         # Application Tauri
│   │   ├── src-tauri/   # Backend Rust
│   │   ├── src/         # Frontend (réutilise code web)
│   │   └── package.json
│   └── mobile/          # Application React Native
│       ├── src/
│       │   └── components/  # Réutilise @collab/ui
│       ├── ios/
│       ├── android/
│       └── package.json
├── pnpm-workspace.yaml  # ou yarn workspaces
└── package.json
```

**Code partagé entre plateformes :**

```typescript
// packages/shared/src/api/client.ts
export class CollabAPIClient {
  constructor(
    private baseURL: string,
    private token?: string,
  ) {}

  async login(email: string, password: string) {
    const response = await fetch(`${this.baseURL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    return response.json();
  }

  async getWorkspaces() {
    /* ... */
  }
  async createDocument(workspaceId: string, data: any) {
    /* ... */
  }
  // ... tous les endpoints
}

// packages/shared/src/stores/auth.ts (Nanostores)
import { atom, computed } from "nanostores";

export const $user = atom<User | null>(null);
export const $token = atom<string | null>(null);
export const $isAuthenticated = computed($user, (user) => !!user);

// Utilisable dans Astro, React, Vue, Svelte

// packages/ui/src/hooks/useAuth.ts (React)
import { useStore } from "@nanostores/react";
import { $user, $token } from "@collab/shared/stores";

export const useAuth = () => {
  const user = useStore($user);
  const token = useStore($token);

  return { user, token };
};

// Dans Astro (pas de hooks)
// src/pages/dashboard.astro
import { $user } from "@collab/shared/stores";
const user = $user.get();
```

#### Différences par Plateforme

**🌐 Web (Astro)**

- Navigation : Astro pages (file-based routing)
- Routage côté client : View Transitions API ou React Router (dans islands)
- Stockage : LocalStorage, IndexedDB
- Authentification : JWT dans LocalStorage + HttpOnly cookies (API routes Astro)
- Offline : Service Worker + Cache API (via Vite PWA)
- Hydratation : Partielle (islands)
- API Routes : `src/pages/api/` (endpoints Astro)

**📱 Mobile**

- Navigation : React Navigation (Stack, Tab, Drawer)
- Stockage : AsyncStorage, MMKV (performant)
- Authentification : Secure Store API
- Offline : NetInfo + local SQLite
- Features natives : Camera, Biométrie, Notifications Push

**💻 Desktop (Tauri)**

- Navigation : Astro pages (réutilise app web)
- Stockage : Tauri Store API, système de fichiers
- Authentification : Keychain système (macOS Keychain, Windows Credential Manager, Linux Secret Service)
- Offline : Fichiers locaux + SQLite
- Features natives :
  - Menus natifs (Rust)
  - Raccourcis clavier globaux (globalShortcut)
  - Notifications système (Tauri Notification)
  - Auto-update (Tauri Updater)
  - Accès fichiers sécurisé (File System API)
  - Fenêtres multiples
  - Tray icon

#### Gestion de l'Authentification Multi-Plateforme

```typescript
// packages/shared/src/auth/storage.ts
export interface AuthStorage {
  saveToken(token: string): Promise<void>;
  getToken(): Promise<string | null>;
  removeToken(): Promise<void>;
}

// Web implementation
class WebAuthStorage implements AuthStorage {
  async saveToken(token: string) {
    localStorage.setItem("auth_token", token);
  }
  async getToken() {
    return localStorage.getItem("auth_token");
  }
  async removeToken() {
    localStorage.removeItem("auth_token");
  }
}

// Mobile implementation (React Native)
class MobileAuthStorage implements AuthStorage {
  async saveToken(token: string) {
    await SecureStore.setItemAsync("auth_token", token);
  }
  async getToken() {
    return await SecureStore.getItemAsync("auth_token");
  }
  async removeToken() {
    await SecureStore.deleteItemAsync("auth_token");
  }
}

// Desktop implementation (Electron)
class DesktopAuthStorage implements AuthStorage {
  async saveToken(token: string) {
    await keytar.setPassword("collab", "auth_token", token);
  }
  async getToken() {
    return await keytar.getPassword("collab", "auth_token");
  }
  async removeToken() {
    await keytar.deletePassword("collab", "auth_token");
  }
}
```

#### Responsive Design (Web & Mobile Web)

```typescript
// Breakpoints
const breakpoints = {
  mobile: '(max-width: 640px)',
  tablet: '(min-width: 641px) and (max-width: 1024px)',
  desktop: '(min-width: 1025px)',
}

// Adaptive Layout
<div className="
  // Mobile
  flex flex-col p-4

  // Tablet
  md:flex-row md:p-6

  // Desktop
  lg:grid lg:grid-cols-12 lg:gap-8 lg:p-8
">
  <Sidebar className="lg:col-span-3" />
  <MainContent className="lg:col-span-9" />
</div>
```

---

## 🎨 Design System & Guidelines

### 🎨 Système de Couleurs

#### Palette Principale : Noir & Blanc + Accent

**Mode Clair (Light Theme)**

```css
/* Backgrounds */
--bg-primary: #ffffff /* Fond principal */ --bg-secondary: #f8f9fa
  /* Fond secondaire */ --bg-tertiary: #f1f3f5 /* Cards, panels */ /* Text */
  --text-primary: #000000 /* Texte principal */ --text-secondary: #6c757d
  /* Texte secondaire */ --text-tertiary: #adb5bd /* Texte désactivé */
  /* Borders */ --border-light: #e9ecef --border-medium: #dee2e6
  --border-dark: #ced4da /* Accent Color (À choisir) */ --accent: #6366f1
  /* Indigo - Moderne */ --accent-hover: #4f46e5 --accent-light: #e0e7ff;
```

**Mode Sombre (Dark Theme)**

```css
/* Backgrounds */
--bg-primary: #0a0a0a /* Fond principal */ --bg-secondary: #1a1a1a
  /* Fond secondaire */ --bg-tertiary: #2a2a2a /* Cards, panels */ /* Text */
  --text-primary: #ffffff /* Texte principal */ --text-secondary: #a0a0a0
  /* Texte secondaire */ --text-tertiary: #6c6c6c /* Texte désactivé */
  /* Borders */ --border-light: #2a2a2a --border-medium: #3a3a3a
  --border-dark: #4a4a4a /* Accent Color (même que light) */ --accent: #6366f1
  --accent-hover: #7c3aed --accent-light: #312e81;
```

#### Couleurs Sémantiques

```css
/* Success */
--success: #10b981 --success-light: #d1fae5 --success-dark: #065f46
  /* Warning */ --warning: #f59e0b --warning-light: #fef3c7
  --warning-dark: #92400e /* Error/Danger */ --error: #ef4444
  --error-light: #fee2e2 --error-dark: #991b1b /* Info */ --info: #3b82f6
  --info-light: #dbeafe --info-dark: #1e40af;
```

### 🎭 Alternatives de Couleur d'Accent

Choisissez une couleur d'accent selon la personnalité de votre marque :

1. **Indigo** `#6366F1` - Moderne, professionnel, tech
2. **Violet** `#8B5CF6` - Créatif, innovant
3. **Bleu** `#3B82F6` - Classique, fiable
4. **Teal** `#14B8A6` - Frais, collaboratif
5. **Rose** `#EC4899` - Énergique, créatif
6. **Orange** `#F97316` - Dynamique, chaleureux

### 🌓 Toggle Dark/Light Mode

**Implémentation recommandée :**

```typescript
// React/Next.js avec next-themes
import { ThemeProvider } from "next-themes";

// Stocker préférence utilisateur
const [theme, setTheme] = useState<"light" | "dark" | "system">("system");

// Auto-détection système
const systemTheme = window.matchMedia("(prefers-color-scheme: dark)").matches;

// Toggle manuel
const toggleTheme = () => {
  setTheme(theme === "light" ? "dark" : "light");
};
```

### Inspirations Design

- **Linear**: Design minimaliste noir/blanc avec accent
- **Notion**: Interface épurée, sidebar élégante
- **GitHub Dark**: Excellent dark mode
- **Raycast**: UI moderne, réactive, shortcuts
- **Vercel**: Design system noir/blanc sophistiqué

### Principes UX

- **Feedback immédiat**: Loading states, optimistic updates, animations fluides
- **Visibilité**: Qui édite quoi, versions sauvegardées, statut de sync
- **Simplicité**: Pas plus de 2 clics pour actions courantes
- **Responsive**: Mobile-first, adaptive layout
- **Accessibilité**: Contraste WCAG AA minimum, focus visible, keyboard navigation
- **Performance**: < 2s chargement initial, 60fps animations

### 📐 Spacing & Typography

```css
/* Spacing Scale (Tailwind-like) */
--space-1:
  0.25rem /* 4px */ --space-2: 0.5rem /* 8px */ --space-3: 0.75rem /* 12px */
    --space-4: 1rem /* 16px */ --space-6: 1.5rem /* 24px */ --space-8: 2rem
    /* 32px */ --space-12: 3rem /* 48px */ /* Typography */ --font-sans: "Inter",
  system-ui, sans-serif --font-mono: "JetBrains Mono", "Fira Code",
  monospace /* Font Sizes */ --text-xs: 0.75rem /* 12px */ --text-sm: 0.875rem
    /* 14px */ --text-base: 1rem /* 16px */ --text-lg: 1.125rem /* 18px */
    --text-xl: 1.25rem /* 20px */ --text-2xl: 1.5rem /* 24px */
    --text-3xl: 1.875rem /* 30px */ --text-4xl: 2.25rem /* 36px */;
```

### 🎨 Curseurs Collaborateurs (Couleurs Vives)

Pour les curseurs des utilisateurs, utiliser des couleurs vives distinctes :

```css
--cursor-1: #ef4444 /* Rouge */ --cursor-2: #f59e0b /* Orange */
  --cursor-3: #10b981 /* Vert */ --cursor-4: #3b82f6 /* Bleu */
  --cursor-5: #8b5cf6 /* Violet */ --cursor-6: #ec4899 /* Rose */
  --cursor-7: #14b8a6 /* Teal */ --cursor-8: #f97316 /* Orange foncé */;
```

---

## 📦 Livrables Attendus

### Code - Commun

- [ ] Authentification JWT complète (login, register, refresh token)
- [ ] Gestion des erreurs et états de chargement
- [ ] Tests unitaires (minimum 70% couverture)
- [ ] Tests E2E (Playwright/Cypress) pour flows critiques
- [ ] Documentation technique (README, Architecture)
- [ ] Design system implémenté (noir/blanc + accent)
- [ ] Mode clair/sombre fonctionnel

### 🌐 Livrables Web

- [ ] Application web responsive (Desktop + Mobile web)
- [ ] Progressive Web App (PWA) avec offline support
- [ ] SEO optimisé (Next.js SSR)
- [ ] Performance Lighthouse > 90
- [ ] Build optimisé (code splitting, lazy loading)

### 📱 Livrables Mobile

- [ ] Application iOS (TestFlight ready)
- [ ] Application Android (APK/AAB)
- [ ] Navigation native fluide
- [ ] Notifications push configurées
- [ ] Deep linking pour partage de documents
- [ ] Biométrie (Touch ID / Face ID)

### 💻 Livrables Desktop

- [ ] Application Windows (.exe)
- [ ] Application macOS (.dmg)
- [ ] Application Linux (.AppImage ou .deb)
- [ ] Auto-update configuré
- [ ] Raccourcis clavier natifs

### Documentation

- [ ] Guide utilisateur par plateforme
- [ ] Guide développeur (setup, architecture)
- [ ] Documentation de l'API client (SDK)
- [ ] Changelog par version
- [ ] Guide de contribution

### 🚀 Déploiement

#### Web (Astro)

```bash
# Build
pnpm build  # Output: dist/

# Vercel (Recommandé pour Astro)
vercel deploy --prod

# Netlify
netlify deploy --prod --dir=dist

# Cloudflare Pages
wrangler pages deploy dist

# Docker
docker build -t collab-web .
docker run -p 4321:4321 collab-web

# Node.js adapter (si SSR)
npm run build && node dist/server/entry.mjs
```

#### Mobile

```bash
# React Native - iOS
cd ios && pod install
npx react-native run-ios --configuration Release

# React Native - Android
cd android && ./gradlew assembleRelease

# Expo
eas build --platform ios
eas build --platform android
eas submit
```

#### Desktop (Tauri)

```bash
# Development
pnpm tauri dev

# Build pour toutes les plateformes
pnpm tauri build

# Build spécifique
pnpm tauri build --target x86_64-pc-windows-msvc  # Windows
pnpm tauri build --target x86_64-apple-darwin     # macOS Intel
pnpm tauri build --target aarch64-apple-darwin    # macOS Apple Silicon
pnpm tauri build --target x86_64-unknown-linux-gnu # Linux

# Outputs:
# macOS: src-tauri/target/release/bundle/macos/Collab.app
# Windows: src-tauri/target/release/bundle/msi/Collab_1.0.0_x64.msi
# Linux: src-tauri/target/release/bundle/deb/collab_1.0.0_amd64.deb
```

#### CI/CD

- [ ] GitHub Actions configuré
- [ ] Déploiement automatique sur commit (main/production)
- [ ] Tests automatiques sur PR
- [ ] Variables d'environnement sécurisées

---

## 🔗 Configuration & URLs de l'API

### Backend API Déployée

#### Production

- **Base URL**: `https://api.collab.app/api` (⚠️ À CONFIGURER)
- **WebSocket URL**: `wss://api.collab.app`
- **Swagger/OpenAPI**: `https://api.collab.app/swagger-ui.html`

#### Développement Local

- **Base URL**: `http://localhost:8080/api`
- **WebSocket URL**: `ws://localhost:8080`
- **Swagger/OpenAPI**: `http://localhost:8080/swagger-ui.html`

#### Variables d'Environnement

**🌐 Web (Astro)**

```bash
# .env.production
PUBLIC_API_URL=https://api.collab.app/api
PUBLIC_WS_URL=wss://api.collab.app
PUBLIC_APP_NAME=Collab
PUBLIC_APP_VERSION=1.0.0

# .env.development
PUBLIC_API_URL=http://localhost:8080/api
PUBLIC_WS_URL=ws://localhost:8080

# Utilisation dans Astro
# const API_URL = import.meta.env.PUBLIC_API_URL;
```

**📱 Mobile (React Native)**

```javascript
// app.config.js ou .env
export default {
  production: {
    API_URL: "https://api.collab.app/api",
    WS_URL: "wss://api.collab.app",
  },
  development: {
    API_URL: "http://localhost:8080/api",
    WS_URL: "ws://localhost:8080",
  },
  // Features natives
  ios: {
    bundleIdentifier: "app.collab.ios",
  },
  android: {
    package: "app.collab.android",
  },
};
```

**💻 Desktop (Tauri)**

```json
// src-tauri/tauri.conf.json
{
  "build": {
    "beforeDevCommand": "pnpm dev",
    "beforeBuildCommand": "pnpm build",
    "devPath": "http://localhost:4321",
    "distDir": "../dist"
  },
  "tauri": {
    "bundle": {
      "identifier": "app.collab.desktop",
      "icon": ["icons/icon.icns", "icons/icon.ico", "icons/icon.png"]
    },
    "updater": {
      "active": true,
      "endpoints": ["https://updates.collab.app/{{target}}/{{current_version}}"]
    }
  }
}
```

```typescript
// src/config/env.ts (Astro)
export const config = {
  API_URL: import.meta.env.PUBLIC_API_URL || "http://localhost:8080/api",
  WS_URL: import.meta.env.PUBLIC_WS_URL || "ws://localhost:8080",
  IS_TAURI: "__TAURI__" in window,
};
```

### Documentation API

- **Documentation complète**: Voir `API_CONTROLLERS.md`
- **Guide démarrage backend**: Voir `QUICKSTART.md`

### Exemples de requêtes

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Créer workspace
curl -X POST http://localhost:8080/api/workspaces \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Mon Workspace","description":"Description"}'

# Créer document
curl -X POST http://localhost:8080/api/workspaces/{workspaceId}/documents \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"title":"Doc 1","content":"Hello World"}'
```

### Bibliothèques recommandées

#### Core

- **Axios** ou **Fetch API** pour requêtes HTTP
- **Socket.io-client** pour WebSocket en temps réel
- **TanStack Query** pour cache et synchronisation API

#### UI & Styling

- **TailwindCSS** pour styling
- **Shadcn/ui** ou **Headless UI** pour composants
- **next-themes** pour gestion dark/light mode
- **Framer Motion** pour animations

#### Formulaires & Validation

- **react-hook-form** pour formulaires performants
- **zod** pour validation schema

#### Utilitaires

- **date-fns** ou **dayjs** pour dates
- **clsx** ou **classnames** pour classes CSS conditionnelles
- **nanoid** pour génération d'IDs

#### Éditeur

- **Monaco Editor** (VS Code editor)
- **Slate.js** (React) ou **Tiptap** (Vue)
- **Quill** (plus simple)

#### Export & Documents

- **jsPDF** pour génération PDF
- **html2canvas** pour screenshots
- **diff-match-patch** pour comparaison versions

#### Mobile (React Native)

- **React Native Paper** ou **Tamagui** pour UI
- **React Navigation** pour navigation
- **React Native MMKV** pour stockage
- **Reanimated** pour animations natives

---

## 🎉 Bon Développement

Si vous avez des questions ou besoin de clarifications sur l'API, consultez:

- `API_CONTROLLERS.md` - Documentation complète de l'API
- `QUICKSTART.md` - Guide de démarrage backend
- `README.md` - Vue d'ensemble du projet

**Contact Backend Team**: Pour toute question sur les endpoints ou comportements attendus.

---

**Version**: 2.0
**Date**: 28 février 2026
**Statut**: ✅ API Backend Déployée | 📱 Frontend Multi-plateforme À DÉVELOPPER
**Design**: Noir & Blanc + Couleur d'accent | Mode Clair/Sombre
