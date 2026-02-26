z# 🚀 Collab - Application Spring Boot

![CI/CD](https://github.com/VOTRE_USERNAME/Collab/workflows/CI/CD%20Pipeline/badge.svg)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=flat&logo=docker&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-green.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)

Application Spring Boot avec PostgreSQL et Kafka, déployée avec Docker.

---

## 📖 **[👉 Guide Démarrage Rapide (cliquez ici)](QUICKSTART.md)**

**Nouveau dans le projet ? Lisez le [QUICKSTART.md](QUICKSTART.md) pour démarrer
en 3 minutes !**

---

## 📋 Prérequis

- Docker et Docker Compose installés
- Port 8080, 5433 et 9092 disponibles

## 🚀 Démarrage Rapide

### Commandes Essentielles

```bash
# Démarrer l'application
make start

# Voir les logs
make logs

# Compiler l'application
make build

# Arrêter l'application
make stop

# Voir toutes les commandes
make help
```

L'application sera accessible sur **<http://localhost:8080>**

## 📦 Services

| Service     | Port | Identifiants      |
| ----------- | ---- | ----------------- |
| Application | 8080 | -                 |
| PostgreSQL  | 5433 | postgres/postgres |
| Kafka       | 9092 | -                 |

## 🔧 Rebuild après modifications

Si vous modifiez le code :

```bash
docker compose down
docker compose build --no-cache
docker compose up -d
```

## 🐛 Dépannage

### Vérifier l'état des services

```bash
docker compose ps
```

### Voir tous les logs

```bash
docker compose logs
```

### Nettoyer complètement

```bash
docker compose down -v
docker system prune -f
```

## 🔄 CI/CD

Le projet utilise GitHub Actions pour l'intégration et le déploiement continus.

Voir [.github/GITHUB_ACTIONS.md](.github/GITHUB_ACTIONS.md) pour plus de
détails.

**Workflows disponibles :**

- ✅ Build & Test automatique
- 🐳 Build Docker image
- 🧪 Tests d'intégration
- 🔒 Scan de sécurité
- 🚀 Déploiement automatique

---

**C'est prêt ! 🎉**
