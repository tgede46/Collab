# 🔧 Configuration GitHub Actions

## 📋 Secrets à configurer

Pour que le pipeline fonctionne, ajoutez ces secrets dans **Settings > Secrets and variables > Actions** :

### Secrets obligatoires pour Docker Hub (optionnel)

- `DOCKER_USERNAME` : Votre nom d'utilisateur Docker Hub
- `DOCKER_PASSWORD` : Votre token d'accès Docker Hub

### Comment générer un token Docker Hub ?

1. Connectez-vous à [Docker Hub](https://hub.docker.com)
2. Allez dans **Account Settings > Security**
3. Cliquez sur **New Access Token**
4. Donnez un nom (ex: `github-actions`)
5. Copiez le token et ajoutez-le dans GitHub Secrets

## 🚀 Workflows disponibles

### 1. **CI/CD Pipeline** (`.github/workflows/ci-cd.yml`)

**Déclencheurs :**
- Push sur `main`, `develop`, `devops`
- Pull Request vers `main`, `develop`

**Jobs :**
- ✅ **build-and-test** : Compile avec Gradle et génère le JAR
- 🐳 **docker-build** : Construit l'image Docker
- 🧪 **docker-compose-test** : Teste avec Docker Compose
- 🔒 **security-scan** : Scan de sécurité avec Trivy
- 🚀 **deploy-staging** : Déploiement staging (branche `develop`)
- 🚀 **deploy-production** : Déploiement production (branche `main`)
- 📢 **notify** : Notifications de statut

### 2. **Docker Publish** (`.github/workflows/docker-publish.yml`)

**Déclencheurs :**
- Création d'une release GitHub
- Manuel via `workflow_dispatch`

**Actions :**
- Publie l'image sur Docker Hub avec tags versionnés
- Support multi-architecture (amd64, arm64)

### 3. **Dependabot** (`.github/dependabot.yml`)

**Actions :**
- Mise à jour automatique des GitHub Actions
- Mise à jour des dépendances Gradle
- Mise à jour des images Docker

## 📊 Badges pour README.md

Ajoutez ces badges dans votre README.md :

```markdown
![CI/CD](https://github.com/VOTRE_USERNAME/Collab/workflows/CI/CD%20Pipeline/badge.svg)
![Docker](https://github.com/VOTRE_USERNAME/Collab/workflows/Docker%20Publish/badge.svg)
```

## 🔧 Environnements GitHub

Pour les déploiements, créez ces environnements dans **Settings > Environments** :

1. **staging**
   - Protection rules : Aucune
   - Auto-deploy : branche `develop`

2. **production**
   - Protection rules : Required reviewers
   - Auto-deploy : branche `main`

## 🧪 Tester localement

### Tester le build Gradle
```bash
./gradlew clean build --no-daemon
```

### Tester le build Docker
```bash
docker build -t collab-app:test .
```

### Tester Docker Compose
```bash
docker compose up -d
docker compose ps
docker compose logs -f
docker compose down -v
```

## 🚀 Workflow de développement

### Feature branches
```bash
git checkout -b feature/ma-fonctionnalite
git push origin feature/ma-fonctionnalite
# Créer une PR vers develop
```

### Déploiement staging
```bash
git checkout develop
git merge feature/ma-fonctionnalite
git push origin develop
# Le pipeline déploie automatiquement vers staging
```

### Déploiement production
```bash
git checkout main
git merge develop
git push origin main
# Le pipeline déploie automatiquement vers production
```

## 📦 Créer une release

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

Ou via GitHub :
1. **Releases > Create a new release**
2. Choisir un tag (ex: `v1.0.0`)
3. Publier
4. Le pipeline publie automatiquement sur Docker Hub

## 🔍 Monitoring des pipelines

- Voir les runs : **Actions** tab sur GitHub
- Logs détaillés : Cliquer sur un workflow run
- Artifacts : Téléchargeables pendant 7 jours

## ⚡ Optimisations

Le pipeline utilise :
- ✅ Cache Gradle pour builds plus rapides
- ✅ Cache Docker layers (GitHub Actions Cache)
- ✅ Build multi-stage Docker
- ✅ Parallel jobs quand possible

## 🛠️ Personnalisation

### Désactiver le push Docker Hub

Dans `.github/workflows/ci-cd.yml`, commentez :
```yaml
# - name: Log in to Docker Hub (optional)
#   if: github.event_name != 'pull_request'
#   ...
```

### Ajouter des notifications Slack

Ajoutez dans le job `notify` :
```yaml
- name: Slack notification
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## 📝 Notes importantes

- Le pipeline fonctionne **sans secrets Docker** (mode local uniquement)
- Le scan de sécurité continue même en cas d'erreur (`continue-on-error: true`)
- Les tests Docker Compose attendent 60s pour le démarrage des services
- Les artifacts sont conservés 7 jours

## ✅ Checklist de mise en production

- [ ] Secrets Docker Hub configurés
- [ ] Environnements staging/production créés
- [ ] Branch protection rules activées sur `main`
- [ ] Dependabot activé
- [ ] Notifications configurées
- [ ] Tests passent localement
- [ ] Documentation à jour
