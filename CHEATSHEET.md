# 📝 Cheatsheet Collab - Commandes Rapides

## 🚀 Démarrage

```bash
make start          # Démarrer tous les services
make stop           # Arrêter tous les services
make restart        # Redémarrer
make logs           # Voir les logs
```

## 💻 Développement

```bash
make build          # Compiler l'application
make lint           # Vérifier le code
make health         # Vérifier que ça tourne
```

## 🗄️ Base de données

```bash
make db-connect     # Se connecter à PostgreSQL
make db-backup      # Faire un backup
```

## 🔍 Debug

```bash
make logs           # Logs de l'app
make logs-all       # Logs de tout
make status         # État des services
docker compose ps   # Voir tous les conteneurs
```

## 🛠️ Maintenance

```bash
make clean          # Tout nettoyer
make rebuild        # Reconstruire de zéro
```

## 📊 Infos

- **App**: http://localhost:8080
- **PostgreSQL**: localhost:5433 (user: postgres, pass: postgres, db: collab)
- **Kafka**: localhost:9092

## 📚 Documentation

- `make help` - Voir toutes les commandes
- `make QUICKSTART` - Guide complet
- [README.md](README.md) - Documentation principale
- [DEVOPS.md](DEVOPS.md) - Infos DevOps

---

**💡 Astuce**: Tapez `make` suivi de TAB pour l'autocomplétion des commandes !
