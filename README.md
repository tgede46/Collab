# 🚀 Collab - Application Spring Boot

Application Spring Boot avec PostgreSQL et Kafka, déployée avec Docker.

## 📋 Prérequis

- Docker et Docker Compose installés
- Port 8080, 5432 et 9092 disponibles

## 🚀 Démarrage Rapide

### Démarrer l'application

```bash
docker compose up -d
```

L'application sera accessible sur **http://localhost:8080**

### Voir les logs

```bash
docker compose logs -f app
```

Appuyez sur `Ctrl + C` pour quitter.

### Arrêter l'application

```bash
docker compose down
```

### Arrêter et supprimer les données

```bash
docker compose down -v
```

## 📦 Services

| Service | Port | Identifiants |
|---------|------|--------------|
| Application | 8080 | - |
| PostgreSQL | 5432 | postgres/postgres |
| Kafka | 9092 | - |

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

---

**C'est prêt ! 🎉**
