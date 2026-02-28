# 🌐 Guide de Développement Web Frontend - Collab (Astro)

## 📖 Contexte du Projet

**Collab Web** est l'application web pour la collaboration documentaire en temps réel, similaire à Google Docs. Elle consomme l'**API REST backend déployée** et permet l'édition collaborative de documents avec visualisation des curseurs en temps réel.

### 🚀 API Backend Déployée

- **Stack Backend**: Spring Boot 4.0.3 + Java 21 + PostgreSQL + Kafka
- **Architecture**: API REST avec authentification JWT
- **Statut**: ✅ Déployée et opérationnelle
- **API Base URL Production**: `https://api.collab.app/api` (⚠️ À configurer)
- **API Base URL Développement**: `http://localhost:8080/api`
- **Documentation API**: Voir `API_CONTROLLERS.md`

---

## 🎯 Objectif

Créer une **application web moderne, performante et responsive** avec Astro permettant :

1. ✅ **Authentification** (inscription, connexion, JWT)
2. ✅ **Gestion des Workspaces** (création, membres, permissions)
3. ✅ **Édition collaborative de documents** en temps réel
4. ✅ **Visualisation des curseurs** des collaborateurs
5. ✅ **Gestion des versions** (historique, restauration)
6. ✅ **Partage de documents** via liens (feature future)
7. ✅ **Export de documents** (txt, md, pdf)
8. ✅ **Design noir & blanc** avec couleur d'accent
9. ✅ **Mode clair/sombre**

---

## 🛠️ Stack Technologique - Astro

### Architecture Choisie

```
✅ Astro 4+ avec TypeScript
✅ Islands Architecture (composants interactifs isolés)
✅ React pour les îles interactives
   - Éditeur collaboratif (React island)
   - Dashboard interactif (React island)
   - Composants temps réel (React island)
✅ TanStack Query (React Query) pour API calls
✅ Nanostores pour state management léger
✅ Socket.io-client pour WebSocket temps réel
✅ TailwindCSS pour styling
✅ Shadcn/ui ou DaisyUI pour composants
✅ Monaco Editor pour l'éditeur de code
```

### Pourquoi Astro ?

- ⚡ **Performance exceptionnelle** : 0KB JavaScript par défaut
- 🎯 **Hydratation partielle** : Islands architecture
- 📦 **Build optimisé** automatique
- 🔄 **Multi-framework** : React, Vue, Svelte dans la même app
- 🎨 **Excellent pour SEO** : SSG/SSR hybrid
- 🚀 **Developer Experience** : File-based routing, Vite-powered

---

## 📁 Structure du Projet

```bash
collab-web/
├── src/
│   ├── pages/                    # Pages Astro (routing automatique)
│   │   ├── index.astro          # Landing page (statique)
│   │   ├── login.astro          # Page de connexion
│   │   ├── register.astro       # Page d'inscription
│   │   ├── dashboard.astro      # Dashboard (avec islands)
│   │   ├── workspace/
│   │   │   └── [id].astro       # Détails workspace
│   │   ├── document/
│   │   │   ├── [id]/
│   │   │   │   ├── edit.astro   # Éditeur collaboratif
│   │   │   │   └── versions.astro # Historique
│   │   ├── profile.astro
│   │   └── api/                 # API Routes (optionnel)
│   │       └── auth/
│   │           └── refresh.ts   # Refresh token endpoint
│   │
│   ├── components/              # Composants Astro
│   │   ├── react/               # 🏝️ Islands React
│   │   │   ├── DocumentEditor.tsx       # Éditeur collaboratif
│   │   │   ├── WorkspaceList.tsx        # Liste workspaces
│   │   │   ├── DocumentList.tsx         # Liste documents
│   │   │   ├── CollaborationWidget.tsx  # Curseurs temps réel
│   │   │   ├── VersionHistory.tsx       # Historique versions
│   │   │   ├── MemberManager.tsx        # Gestion membres
│   │   │   └── ThemeToggle.tsx          # Toggle dark/light
│   │   │
│   │   ├── ui/                  # Composants UI de base
│   │   │   ├── Button.astro
│   │   │   ├── Card.astro
│   │   │   ├── Modal.astro
│   │   │   └── Toast.astro
│   │   │
│   │   └── layout/              # Composants layout
│   │       ├── Navbar.astro
│   │       ├── Sidebar.astro
│   │       └── Footer.astro
│   │
│   ├── layouts/                 # Layouts Astro
│   │   ├── BaseLayout.astro     # Layout de base
│   │   ├── AuthLayout.astro     # Layout pages auth
│   │   └── AppLayout.astro      # Layout dashboard/app
│   │
│   ├── lib/                     # Logique métier
│   │   ├── api/                 # Client API
│   │   │   ├── client.ts        # CollabAPIClient
│   │   │   ├── auth.ts          # Auth endpoints
│   │   │   ├── workspace.ts     # Workspace endpoints
│   │   │   ├── document.ts      # Document endpoints
│   │   │   ├── collaboration.ts # Collaboration endpoints
│   │   │   └── version.ts       # Version endpoints
│   │   │
│   │   ├── stores/              # Nanostores
│   │   │   ├── auth.ts          # $user, $token
│   │   │   ├── workspace.ts     # $workspaces, $currentWorkspace
│   │   │   ├── document.ts      # $documents, $currentDocument
│   │   │   ├── theme.ts         # $theme (light/dark)
│   │   │   └── collaboration.ts # $collaborators, $operations
│   │   │
│   │   ├── hooks/               # React hooks (pour islands)
│   │   │   ├── useAuth.ts
│   │   │   ├── useWorkspace.ts
│   │   │   ├── useDocument.ts
│   │   │   ├── useCollaboration.ts
│   │   │   └── useTheme.ts
│   │   │
│   │   ├── utils/               # Utilitaires
│   │   │   ├── ot.ts            # Operational Transformation
│   │   │   ├── permissions.ts   # Gestion permissions
│   │   │   ├── storage.ts       # LocalStorage wrapper
│   │   │   └── format.ts        # Formatage dates, etc.
│   │   │
│   │   └── types/               # TypeScript types
│   │       ├── api.ts           # Types API
│   │       ├── auth.ts
│   │       ├── workspace.ts
│   │       ├── document.ts
│   │       └── collaboration.ts
│   │
│   ├── styles/                  # Styles globaux
│   │   ├── global.css           # Styles globaux + TailwindCSS
│   │   ├── theme.css            # Variables CSS thème
│   │   └── editor.css           # Styles éditeur Monaco
│   │
│   └── env.d.ts                 # Types environnement
│
├── public/                      # Assets statiques
│   ├── favicon.ico
│   ├── logo.svg
│   └── fonts/
│
├── .env                         # Variables d'environnement
├── .env.example
├── astro.config.mjs             # Configuration Astro
├── tailwind.config.mjs          # Configuration TailwindCSS
├── tsconfig.json                # Configuration TypeScript
└── package.json
```

---

## 🎨 Design System - Noir & Blanc + Accent

### Palette de Couleurs

#### Mode Clair (Light Theme)

```css
/* src/styles/theme.css */
:root[data-theme="light"] {
  /* Backgrounds */
  --bg-primary: #ffffff;
  --bg-secondary: #f8f9fa;
  --bg-tertiary: #f1f3f5;
  --bg-hover: #e9ecef;

  /* Text */
  --text-primary: #000000;
  --text-secondary: #6c757d;
  --text-tertiary: #adb5bd;
  --text-disabled: #dee2e6;

  /* Borders */
  --border-light: #e9ecef;
  --border-medium: #dee2e6;
  --border-dark: #ced4da;

  /* Accent - Indigo (changeable) */
  --accent: #6366f1;
  --accent-hover: #4f46e5;
  --accent-light: #e0e7ff;
  --accent-dark: #4338ca;

  /* Semantic Colors */
  --success: #10b981;
  --warning: #f59e0b;
  --error: #ef4444;
  --info: #3b82f6;
}
```

#### Mode Sombre (Dark Theme)

```css
:root[data-theme="dark"] {
  /* Backgrounds */
  --bg-primary: #0a0a0a;
  --bg-secondary: #1a1a1a;
  --bg-tertiary: #2a2a2a;
  --bg-hover: #3a3a3a;

  /* Text */
  --text-primary: #ffffff;
  --text-secondary: #a0a0a0;
  --text-tertiary: #6c6c6c;
  --text-disabled: #4a4a4a;

  /* Borders */
  --border-light: #2a2a2a;
  --border-medium: #3a3a3a;
  --border-dark: #4a4a4a;

  /* Accent - Indigo (même couleur) */
  --accent: #6366f1;
  --accent-hover: #7c3aed;
  --accent-light: #312e81;
  --accent-dark: #5b21b6;

  /* Semantic Colors */
  --success: #10b981;
  --warning: #f59e0b;
  --error: #ef4444;
  --info: #3b82f6;
}
```

#### Couleurs des Curseurs Collaborateurs

```css
/* Couleurs vives pour les curseurs */
:root {
  --cursor-1: #ef4444; /* Rouge */
  --cursor-2: #f59e0b; /* Orange */
  --cursor-3: #10b981; /* Vert */
  --cursor-4: #3b82f6; /* Bleu */
  --cursor-5: #8b5cf6; /* Violet */
  --cursor-6: #ec4899; /* Rose */
  --cursor-7: #14b8a6; /* Teal */
  --cursor-8: #f97316; /* Orange foncé */
}
```

### Configuration TailwindCSS

```javascript
// tailwind.config.mjs
export default {
  content: ["./src/**/*.{astro,html,js,jsx,md,mdx,svelte,ts,tsx,vue}"],
  darkMode: ["class", '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        accent: {
          DEFAULT: "var(--accent)",
          hover: "var(--accent-hover)",
          light: "var(--accent-light)",
          dark: "var(--accent-dark)",
        },
        bg: {
          primary: "var(--bg-primary)",
          secondary: "var(--bg-secondary)",
          tertiary: "var(--bg-tertiary)",
          hover: "var(--bg-hover)",
        },
        text: {
          primary: "var(--text-primary)",
          secondary: "var(--text-secondary)",
          tertiary: "var(--text-tertiary)",
        },
        border: {
          light: "var(--border-light)",
          medium: "var(--border-medium)",
          dark: "var(--border-dark)",
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "Fira Code", "monospace"],
      },
    },
  },
  plugins: [],
};
```

---

## 🔧 Configuration Astro

### astro.config.mjs

```javascript
import { defineConfig } from "astro/config";
import react from "@astrojs/react";
import tailwind from "@astrojs/tailwind";

export default defineConfig({
  output: "static", // ou 'hybrid' pour SSR partiel
  integrations: [
    react(), // Pour les islands React
    tailwind({
      applyBaseStyles: false, // Gérer manuellement les styles de base
    }),
  ],
  vite: {
    optimizeDeps: {
      include: ["@tanstack/react-query", "socket.io-client"],
    },
  },
});
```

### Variables d'Environnement

```bash
# .env.development
PUBLIC_API_URL=http://localhost:8080/api
PUBLIC_WS_URL=ws://localhost:8080
PUBLIC_APP_NAME=Collab
PUBLIC_APP_VERSION=1.0.0
PUBLIC_ENVIRONMENT=development

# .env.production
PUBLIC_API_URL=https://api.collab.app/api
PUBLIC_WS_URL=wss://api.collab.app
PUBLIC_APP_NAME=Collab
PUBLIC_APP_VERSION=1.0.0
PUBLIC_ENVIRONMENT=production
```

**Utilisation dans le code:**

```typescript
// Accessible partout (Astro, React, etc.)
const API_URL = import.meta.env.PUBLIC_API_URL;
const WS_URL = import.meta.env.PUBLIC_WS_URL;
```

---

## 🔌 API Client

### Client API Principal

```typescript
// src/lib/api/client.ts
export class CollabAPIClient {
  private baseURL: string;
  private token: string | null = null;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  setToken(token: string) {
    this.token = token;
  }

  clearToken() {
    this.token = null;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {},
  ): Promise<T> {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      ...options.headers,
    };

    if (this.token) {
      headers["Authorization"] = `Bearer ${this.token}`;
    }

    const response = await fetch(`${this.baseURL}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `HTTP ${response.status}`);
    }

    return response.json();
  }

  // Auth
  async login(email: string, password: string) {
    return this.request<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  }

  async register(username: string, email: string, password: string) {
    return this.request<AuthResponse>("/auth/register", {
      method: "POST",
      body: JSON.stringify({ username, email, password }),
    });
  }

  async refreshToken(refreshToken: string) {
    return this.request<AuthResponse>(
      `/auth/refresh?refreshToken=${refreshToken}`,
      { method: "POST" },
    );
  }

  async logout() {
    return this.request("/auth/logout", { method: "POST" });
  }

  async getCurrentUser() {
    return this.request<User>("/auth/me");
  }

  // Workspaces
  async getWorkspaces() {
    return this.request<WorkspaceResponse[]>("/workspaces");
  }

  async createWorkspace(data: { name: string; description: string }) {
    return this.request<WorkspaceResponse>("/workspaces", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async getWorkspace(workspaceId: string) {
    return this.request<WorkspaceResponse>(`/workspaces/${workspaceId}`);
  }

  async deleteWorkspace(workspaceId: string) {
    return this.request(`/workspaces/${workspaceId}`, { method: "DELETE" });
  }

  async inviteMember(
    workspaceId: string,
    data: { email: string; role: string },
  ) {
    return this.request(`/workspaces/${workspaceId}/members`, {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  // Documents
  async getDocuments(workspaceId: string) {
    return this.request<DocumentResponse[]>(
      `/workspaces/${workspaceId}/documents`,
    );
  }

  async createDocument(
    workspaceId: string,
    data: { title: string; content: string },
  ) {
    return this.request<DocumentResponse>(
      `/workspaces/${workspaceId}/documents`,
      {
        method: "POST",
        body: JSON.stringify(data),
      },
    );
  }

  async getDocument(workspaceId: string, documentId: string) {
    return this.request<DocumentResponse>(
      `/workspaces/${workspaceId}/documents/${documentId}`,
    );
  }

  async updateDocument(
    workspaceId: string,
    documentId: string,
    data: { title?: string; content?: string; clientVersion: number },
  ) {
    return this.request<DocumentResponse>(
      `/workspaces/${workspaceId}/documents/${documentId}`,
      {
        method: "PUT",
        body: JSON.stringify(data),
      },
    );
  }

  async deleteDocument(workspaceId: string, documentId: string) {
    return this.request(`/workspaces/${workspaceId}/documents/${documentId}`, {
      method: "DELETE",
    });
  }

  async exportDocument(
    workspaceId: string,
    documentId: string,
    format: "txt" | "md" | "pdf",
  ) {
    const response = await fetch(
      `${this.baseURL}/workspaces/${workspaceId}/documents/${documentId}/export?format=${format}`,
      {
        headers: {
          Authorization: `Bearer ${this.token}`,
        },
      },
    );

    return response.blob();
  }

  // Collaboration
  async startSession(documentId: string) {
    return this.request<SessionResponse>(
      `/documents/${documentId}/collaboration/sessions`,
      { method: "POST" },
    );
  }

  async getSessions(documentId: string) {
    return this.request<SessionResponse[]>(
      `/documents/${documentId}/collaboration/sessions`,
    );
  }

  async updateCursor(documentId: string, sessionId: string, position: number) {
    return this.request(
      `/documents/${documentId}/collaboration/sessions/${sessionId}/cursor?position=${position}`,
      { method: "PUT" },
    );
  }

  async endSession(documentId: string, sessionId: string) {
    return this.request(
      `/documents/${documentId}/collaboration/sessions/${sessionId}`,
      { method: "DELETE" },
    );
  }

  async applyOperation(documentId: string, operation: OperationRequest) {
    return this.request<Operation>(
      `/documents/${documentId}/collaboration/operations`,
      {
        method: "POST",
        body: JSON.stringify(operation),
      },
    );
  }

  async getOperations(documentId: string, sinceVersion?: number) {
    const query = sinceVersion ? `?sinceVersion=${sinceVersion}` : "";
    return this.request<Operation[]>(
      `/documents/${documentId}/collaboration/operations${query}`,
    );
  }

  // Versions
  async getSnapshots(documentId: string, limit: number = 20) {
    return this.request<SnapshotResponse[]>(
      `/documents/${documentId}/versions/snapshots?limit=${limit}`,
    );
  }

  async createSnapshot(documentId: string) {
    return this.request<SnapshotResponse>(
      `/documents/${documentId}/versions/snapshots`,
      { method: "POST" },
    );
  }

  async restoreSnapshot(documentId: string, snapshotId: string) {
    return this.request(
      `/documents/${documentId}/versions/snapshots/${snapshotId}/restore`,
      { method: "POST" },
    );
  }
}

// Instance globale
export const apiClient = new CollabAPIClient(
  import.meta.env.PUBLIC_API_URL || "http://localhost:8080/api",
);
```

---

## 🗂️ State Management avec Nanostores

### Store Auth

```typescript
// src/lib/stores/auth.ts
import { atom, computed } from "nanostores";
import type { User } from "../types/auth";

export const $user = atom<User | null>(null);
export const $token = atom<string | null>(null);
export const $refreshToken = atom<string | null>(null);

export const $isAuthenticated = computed($user, (user) => !!user);

export const $userRole = computed(
  [$user, $currentWorkspace],
  (user, workspace) => {
    if (!user || !workspace) return null;
    // Logique pour obtenir le rôle dans le workspace
    return workspace.userRole;
  },
);

// Actions
export function setAuth(user: User, token: string, refreshToken: string) {
  $user.set(user);
  $token.set(token);
  $refreshToken.set(refreshToken);

  // Sauvegarder dans localStorage
  localStorage.setItem("auth_token", token);
  localStorage.setItem("refresh_token", refreshToken);
  localStorage.setItem("user", JSON.stringify(user));
}

export function clearAuth() {
  $user.set(null);
  $token.set(null);
  $refreshToken.set(null);

  localStorage.removeItem("auth_token");
  localStorage.removeItem("refresh_token");
  localStorage.removeItem("user");
}

export function loadAuthFromStorage() {
  const token = localStorage.getItem("auth_token");
  const refreshToken = localStorage.getItem("refresh_token");
  const userStr = localStorage.getItem("user");

  if (token && refreshToken && userStr) {
    try {
      const user = JSON.parse(userStr);
      $user.set(user);
      $token.set(token);
      $refreshToken.set(refreshToken);
    } catch (e) {
      clearAuth();
    }
  }
}
```

### Store Thème

```typescript
// src/lib/stores/theme.ts
import { atom } from "nanostores";

export type Theme = "light" | "dark" | "system";

export const $theme = atom<Theme>("system");

export function setTheme(theme: Theme) {
  $theme.set(theme);
  localStorage.setItem("theme", theme);
  applyTheme(theme);
}

export function loadThemeFromStorage() {
  const stored = localStorage.getItem("theme") as Theme;
  if (stored) {
    $theme.set(stored);
    applyTheme(stored);
  } else {
    applyTheme("system");
  }
}

function applyTheme(theme: Theme) {
  const root = document.documentElement;

  if (theme === "system") {
    const prefersDark = window.matchMedia(
      "(prefers-color-scheme: dark)",
    ).matches;
    root.setAttribute("data-theme", prefersDark ? "dark" : "light");
  } else {
    root.setAttribute("data-theme", theme);
  }
}

// Écouter les changements système
if (typeof window !== "undefined") {
  window
    .matchMedia("(prefers-color-scheme: dark)")
    .addEventListener("change", (e) => {
      if ($theme.get() === "system") {
        applyTheme("system");
      }
    });
}
```

---

## 🏝️ React Islands (Composants Interactifs)

### Éditeur Collaboratif

```tsx
// src/components/react/DocumentEditor.tsx
import { useEffect, useState } from "react";
import Editor from "@monaco-editor/react";
import { useStore } from "@nanostores/react";
import { $theme } from "../../lib/stores/theme";
import { apiClient } from "../../lib/api/client";

interface DocumentEditorProps {
  documentId: string;
  workspaceId: string;
  initialContent: string;
  initialVersion: number;
  userPermission: "OWNER" | "EDITOR" | "VIEWER";
}

export default function DocumentEditor({
  documentId,
  workspaceId,
  initialContent,
  initialVersion,
  userPermission,
}: DocumentEditorProps) {
  const [content, setContent] = useState(initialContent);
  const [version, setVersion] = useState(initialVersion);
  const [collaborators, setCollaborators] = useState<any[]>([]);
  const theme = useStore($theme);

  const isReadOnly = userPermission === "VIEWER";

  useEffect(() => {
    // Démarrer session de collaboration
    let sessionId: string;

    async function startCollaboration() {
      const session = await apiClient.startSession(documentId);
      sessionId = session.sessionId;

      // Polling pour les opérations
      const interval = setInterval(async () => {
        const ops = await apiClient.getOperations(documentId, version);
        if (ops.length > 0) {
          // Appliquer les opérations
          ops.forEach((op) => applyOperation(op));
        }
      }, 500);

      return () => {
        clearInterval(interval);
        if (sessionId) {
          apiClient.endSession(documentId, sessionId);
        }
      };
    }

    startCollaboration();
  }, [documentId]);

  function applyOperation(op: any) {
    // Logique OT ici
    setContent((prev) => {
      if (op.type === "INSERT") {
        return (
          prev.slice(0, op.position) + op.content + prev.slice(op.position)
        );
      } else if (op.type === "DELETE") {
        return prev.slice(0, op.position) + prev.slice(op.position + op.length);
      }
      return prev;
    });
    setVersion(op.serverVersion);
  }

  async function handleChange(value: string | undefined) {
    if (!value || isReadOnly) return;

    // Calculer la différence et envoyer l'opération
    // Logique OT simplifiée ici
    setContent(value);
  }

  return (
    <div className="h-full w-full">
      <Editor
        height="100%"
        defaultLanguage="markdown"
        value={content}
        onChange={handleChange}
        theme={theme === "dark" ? "vs-dark" : "vs-light"}
        options={{
          readOnly: isReadOnly,
          minimap: { enabled: false },
          fontSize: 14,
          lineNumbers: "on",
          wordWrap: "on",
        }}
      />

      {/* Affichage des collaborateurs */}
      <div className="absolute top-4 right-4 flex gap-2">
        {collaborators.map((collab) => (
          <div
            key={collab.userId}
            className="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-semibold"
            style={{ backgroundColor: collab.color }}
            title={collab.username}
          >
            {collab.username[0].toUpperCase()}
          </div>
        ))}
      </div>
    </div>
  );
}
```

### Toggle Thème

```tsx
// src/components/react/ThemeToggle.tsx
import { useStore } from "@nanostores/react";
import { $theme, setTheme, type Theme } from "../../lib/stores/theme";

export default function ThemeToggle() {
  const theme = useStore($theme);

  function handleToggle() {
    const newTheme: Theme = theme === "light" ? "dark" : "light";
    setTheme(newTheme);
  }

  return (
    <button
      onClick={handleToggle}
      className="p-2 rounded-lg hover:bg-bg-hover transition-colors"
      aria-label="Toggle theme"
    >
      {theme === "light" ? (
        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          {/* Icon Moon */}
          <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
        </svg>
      ) : (
        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          {/* Icon Sun */}
          <path
            fillRule="evenodd"
            d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z"
            clipRule="evenodd"
          />
        </svg>
      )}
    </button>
  );
}
```

---

## 📄 Exemples de Pages Astro

### Page de Connexion

```astro
---
// src/pages/login.astro
import BaseLayout from '../layouts/BaseLayout.astro';
import LoginForm from '../components/react/LoginForm';
---

<BaseLayout title="Connexion - Collab">
  <div class="min-h-screen flex items-center justify-center bg-bg-secondary px-4">
    <div class="max-w-md w-full space-y-8 bg-bg-primary p-8 rounded-xl border border-border-medium">
      <div class="text-center">
        <h1 class="text-3xl font-bold text-text-primary">Collab</h1>
        <p class="mt-2 text-text-secondary">Connexion à votre compte</p>
      </div>

      <!-- Island React pour le formulaire -->
      <LoginForm client:only="react" />

      <div class="text-center text-sm text-text-secondary">
        Pas encore de compte ?
        <a href="/register" class="text-accent hover:text-accent-hover font-semibold">
          S'inscrire
        </a>
      </div>
    </div>
  </div>
</BaseLayout>
```

### Page Dashboard

```astro
---
// src/pages/dashboard.astro
import AppLayout from '../layouts/AppLayout.astro';
import WorkspaceList from '../components/react/WorkspaceList';
import { $isAuthenticated } from '../lib/stores/auth';

// Rediriger si non authentifié
if (!$isAuthenticated.get()) {
  return Astro.redirect('/login');
}
---

<AppLayout title="Dashboard - Collab">
  <div class="p-8">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-3xl font-bold text-text-primary">Mes Workspaces</h1>
        <p class="text-text-secondary mt-1">Gérez vos espaces de collaboration</p>
      </div>
      <button class="btn-primary">
        Nouveau Workspace
      </button>
    </div>

    <!-- Island React pour la liste des workspaces -->
    <WorkspaceList client:load />
  </div>
</AppLayout>
```

### Page Éditeur

```astro
---
// src/pages/document/[id]/edit.astro
import AppLayout from '../../../layouts/AppLayout.astro';
import DocumentEditor from '../../../components/react/DocumentEditor';

const { id } = Astro.params;

// Récupérer le document (côté serveur)
// const document = await apiClient.getDocument(workspaceId, id);
---

<AppLayout title="Édition - Collab">
  <div class="h-screen flex flex-col">
    <!-- Toolbar -->
    <div class="h-16 border-b border-border-medium flex items-center justify-between px-6">
      <div class="flex items-center gap-4">
        <a href="/dashboard" class="text-text-secondary hover:text-text-primary">
          ← Retour
        </a>
        <h2 class="text-lg font-semibold text-text-primary">Titre du document</h2>
      </div>

      <div class="flex items-center gap-3">
        <button class="btn-secondary">Versions</button>
        <button class="btn-secondary">Partager</button>
        <button class="btn-secondary">Exporter</button>
      </div>
    </div>

    <!-- Éditeur -->
    <div class="flex-1">
      <DocumentEditor
        client:only="react"
        documentId={id}
        workspaceId="xxx"
        initialContent=""
        initialVersion={1}
        userPermission="EDITOR"
      />
    </div>
  </div>
</AppLayout>
```

---

## 🎨 Composants UI de Base

```astro
---
// src/components/ui/Button.astro
interface Props {
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  type?: 'button' | 'submit' | 'reset';
  disabled?: boolean;
}

const { variant = 'primary', size = 'md', type = 'button', disabled = false } = Astro.props;

const baseClasses = 'rounded-lg font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed';

const variantClasses = {
  primary: 'bg-accent text-white hover:bg-accent-hover',
  secondary: 'bg-bg-tertiary text-text-primary hover:bg-bg-hover border border-border-medium',
  danger: 'bg-error text-white hover:bg-error-dark',
};

const sizeClasses = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2 text-base',
  lg: 'px-6 py-3 text-lg',
};

const classes = `${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]}`;
---

<button type={type} class={classes} disabled={disabled}>
  <slot />
</button>
```

---

## 🚀 Commandes de Développement

```bash
# Installation
pnpm install

# Développement
pnpm dev
# → http://localhost:4321

# Build production
pnpm build
# → dist/

# Preview du build
pnpm preview

# Vérification TypeScript
pnpm check

# Linting
pnpm lint
```

---

## 📦 Déploiement

### Vercel (Recommandé)

```bash
# Installation Vercel CLI
pnpm install -g vercel

# Déploiement
vercel deploy --prod

# Variables d'environnement (configurer dans Vercel Dashboard)
PUBLIC_API_URL=https://api.collab.app/api
PUBLIC_WS_URL=wss://api.collab.app
```

### Netlify

```bash
# netlify.toml
[build]
  command = "pnpm build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

### Cloudflare Pages

```bash
wrangler pages deploy dist
```

### Docker

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN pnpm install
COPY . .
RUN pnpm build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## ✅ Checklist de Développement

### Phase 1: Setup & Auth (Semaine 1)

- [ ] Setup projet Astro + TailwindCSS
- [ ] Configuration dark/light mode
- [ ] API Client de base
- [ ] Nanostores (auth, theme)
- [ ] Page landing statique
- [ ] Pages login/register (islands React)
- [ ] AuthGuard/redirection
- [ ] Gestion JWT + refresh token

### Phase 2: Workspaces (Semaine 2)

- [ ] Dashboard avec liste workspaces
- [ ] Création de workspace
- [ ] Détails workspace
- [ ] Invitation de membres
- [ ] Gestion des permissions

### Phase 3: Documents (Semaine 3)

- [ ] Liste des documents
- [ ] Création de document
- [ ] Éditeur de base (Monaco)
- [ ] Sauvegarde automatique
- [ ] Suppression de document

### Phase 4: Collaboration (Semaine 4-5)

- [ ] Session de collaboration
- [ ] Polling des opérations
- [ ] Application des opérations (OT)
- [ ] Affichage des curseurs
- [ ] Indicateur de présence
- [ ] Résolution des conflits

### Phase 5: Versions (Semaine 6)

- [ ] Page historique des versions
- [ ] Création de snapshot
- [ ] Restauration de version
- [ ] Comparaison (diff)

### Phase 6: Features Avancées (Semaine 7-8)

- [ ] Export (txt, md, pdf)
- [ ] Partage via lien
- [ ] WebSocket (remplacer polling)
- [ ] Notifications
- [ ] Optimisations performance

### Phase 7: Polish & Tests (Semaine 9-10)

- [ ] Tests E2E (Playwright)
- [ ] Tests unitaires
- [ ] Responsive design
- [ ] Accessibilité (WCAG AA)
- [ ] Performance (Lighthouse > 90)
- [ ] Documentation

---

## 🎯 Roadmap Prioritaire

**MVP (Minimum Viable Product):**

1. Auth (login/register)
2. Workspaces (create, list)
3. Documents (create, edit, list)
4. Éditeur collaboratif basique (polling)

**V1.0:** 5. Curseurs temps réel 6. Versions 7. Permissions 8. Dark mode

**V1.5:** 9. WebSocket 10. Export 11. Partage via lien

**V2.0:** 12. Rich text editor 13. Commentaires 14. Notifications push

---

## 📚 Ressources

### Documentation

- **API Backend**: Voir `API_CONTROLLERS.md`
- **Astro**: <https://docs.astro.build>
- **TailwindCSS**: <https://tailwindcss.com/docs>
- **Nanostores**: <https://github.com/nanostores/nanostores>
- **Monaco Editor**: <https://microsoft.github.io/monaco-editor/>

### Inspiration Design

- Linear (<https://linear.app>) - Noir/blanc minimaliste
- Notion (<https://notion.so>) - Interface épurée
- GitHub Dark (<https://github.com>) - Excellent dark mode
- Vercel (<https://vercel.com>) - Design system sophistiqué

---

**Version**: 1.0
**Date**: 28 février 2026
**Stack**: Astro + React Islands + TailwindCSS + Nanostores
**Design**: Noir & Blanc + Indigo (accent) | Mode Clair/Sombre
**Statut**: 🚀 Prêt pour le développement
