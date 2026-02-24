# 🚀 Quick Start DevOps - Collab

## ⚡ Démarrage ultra-rapide

```bash
# 1. Démarrer le projet
make up

# 2. Voir les logs
make logs

# 3. Vérifier le statut
make status

# 4. Accéder à l'application
open http://localhost:8080
```

## 📦 Structure DevOps créée

```
Collab/
├── .github/
│   ├── workflows/
│   │   ├── ci-cd.yml              # Pipeline CI/CD complet
│   │   └── docker-publish.yml      # Publication Docker Hub
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md          # Template pour bugs
│   │   └── feature_request.md      # Template pour features
│   ├── pull_request_template.md   # Template pour PR
│   ├── dependabot.yml             # Mises à jour auto
│   └── GITHUB_ACTIONS.md          # Documentation GitHub Actions
├── scripts/
│   └── setup-github.sh            # Script de setup
├── Makefile                       # Commandes simplifiées
├── Dockerfile                     # Multi-stage build
├── compose.yaml                   # Docker Compose
└── .dockerignore                  # Fichiers ignorés
```

## 🎯 Fonctionnalités DevOps implémentées

### ✅ Docker & Containerisation

- [x] Multi-stage builds (optimisé)
- [x] Docker Compose avec tous les services
- [x] Health checks
- [x] Optimisation des layers
- [x] .dockerignore configuré

### ✅ CI/CD (GitHub Actions)

- [x] Build automatique
- [x] Tests automatiques
- [x] Build Docker image
- [x] Tests d'intégration Docker Compose
- [x] Scan de sécurité (Trivy)
- [x] Déploiement staging/production
- [x] Publication Docker Hub
- [x] Cache optimisé (Gradle + Docker)

### ✅ Automation

- [x] Makefile avec 25+ commandes
- [x] Dependabot (mises à jour auto)
- [x] Templates PR/Issues
- [x] Git hooks disponibles
- [x] Scripts de backup

### ✅ Monitoring & Observability

- [x] Health checks Docker
- [x] Logs centralisés
- [x] Stats des conteneurs
- [x] Badges status CI/CD

## 🔧 Commandes Makefile disponibles

| Commande         | Description                   |
| ---------------- | ----------------------------- |
| `make help`      | Afficher toutes les commandes |
| `make up`        | Démarrer les services         |
| `make down`      | Arrêter les services          |
| `make logs`      | Voir les logs de l'app        |
| `make status`    | Statut des conteneurs         |
| `make build`     | Build l'image Docker          |
| `make rebuild`   | Rebuild complet               |
| `make clean`     | Nettoyer tout                 |
| `make test`      | Lancer les tests              |
| `make shell`     | Shell dans le conteneur       |
| `make db-backup` | Backup de la DB               |
| `make health`    | Vérifier la santé             |
| `make stats`     | Stats des conteneurs          |

## 🚀 Workflows CI/CD

### 1. **CI/CD Pipeline** (Automatique)

**Déclencheurs:**

- Push sur `main`, `develop`, `devops`
- Pull Request vers `main`, `develop`

**Pipeline:**

```
Build & Test → Docker Build → Docker Compose Test → Security Scan → Deploy
```

**Jobs:**

1. **build-and-test**: Compile avec Gradle, génère JAR
2. **docker-build**: Construit l'image Docker
3. **docker-compose-test**: Tests d'intégration complets
4. **security-scan**: Analyse de sécurité avec Trivy
5. **deploy-staging**: Déploiement auto sur staging (develop)
6. **deploy-production**: Déploiement auto sur prod (main)
7. **notify**: Notifications de statut

### 2. **Docker Publish** (Manuel/Release)

**Déclencheurs:**

- Création d'une release GitHub
- Workflow manuel

**Actions:**

- Build et push vers Docker Hub
- Tags versionnés
- Multi-architecture (amd64, arm64)

## 🔐 Configuration GitHub (à faire)

### 1. Secrets à ajouter (optionnel)

Dans **Settings > Secrets and variables > Actions**:

```bash
DOCKER_USERNAME=votre-username
DOCKER_PASSWORD=votre-token-docker-hub
```

### 2. Environnements à créer

Dans **Settings > Environments**:

**staging:**

- Pas de protection
- Auto-deploy depuis `develop`

**production:**

- Required reviewers: 1+
- Auto-deploy depuis `main`

### 3. Branch protection rules

Sur la branche `main`:

- [x] Require pull request reviews (1)
- [x] Require status checks to pass
- [x] Require branches to be up to date
- [x] Include administrators

## 📊 Monitoring & Logs

```bash
# Logs en temps réel
make logs

# Tous les logs
make logs-all

# Stats des conteneurs
make stats

# Statut des services
make status

# Health check
make health
```

## 🗄️ Backup & Restore

```bash
# Créer un backup
make db-backup

# Restaurer un backup
make db-restore FILE=backups/backup_20260224_123456.sql
```

## 🧪 Tests en local

```bash
# Tests Gradle
make test

# Tests Docker
make test-docker

# Simulation CI complète
make ci

# Tests d'intégration
make up
# Attendre 30s
curl http://localhost:8080/actuator/health
make down
```

## 🎯 Workflow Git recommandé

### Nouvelle feature

```bash
git checkout -b feature/ma-feature
# ... développement ...
git add .
git commit -m "feat: Ma nouvelle feature"
git push origin feature/ma-feature
# Créer une PR vers develop
```

### Déploiement staging

```bash
git checkout develop
git merge feature/ma-feature
git push origin develop
# ✅ Deploy auto vers staging
```

### Déploiement production

```bash
git checkout main
git merge develop
git push origin main
# ✅ Deploy auto vers production (avec review)
```

### Créer une release

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
# ✅ Publie sur Docker Hub automatiquement
```

## 🔍 Debugging

### Le pipeline GitHub Actions échoue?

1. Vérifier les logs dans l'onglet **Actions**
2. Tester en local avec `make ci`
3. Vérifier les secrets GitHub
4. Voir `.github/GITHUB_ACTIONS.md`

### Docker ne démarre pas?

```bash
# Vérifier Docker
docker ps

# Rebuild complet
make rebuild

# Nettoyer et recommencer
make clean
make up
```

### Port déjà utilisé?

```bash
# Voir les processus sur le port
sudo lsof -i :8080
sudo lsof -i :5433

# Changer les ports dans compose.yaml
```

## 📚 Documentation complète

- **GitHub Actions**: [.github/GITHUB_ACTIONS.md](.github/GITHUB_ACTIONS.md)
- **README principal**: [README.md](README.md)
- **Makefile**: `make help`

## ✅ Checklist avant production

- [ ] Tests passent en local
- [ ] Docker Compose fonctionne
- [ ] GitHub Actions configuré
- [ ] Secrets ajoutés (si Docker Hub)
- [ ] Environnements créés
- [ ] Branch protection activée
- [ ] Monitoring configuré
- [ ] Documentation à jour
- [ ] Backup configuré
- [ ] Health checks fonctionnent

## 🎉 C'est prêt!

Votre projet a maintenant:

- ✅ Pipeline CI/CD complet
- ✅ Déploiement automatisé
- ✅ Tests automatiques
- ✅ Scan de sécurité
- ✅ Docker optimisé
- ✅ Makefile simple
- ✅ Templates PR/Issues

**Enjoy! 🚀**
