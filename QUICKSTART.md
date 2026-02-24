# 🚀 Guide de Démarrage Rapide - Collab

Guide simple pour les développeurs de l'équipe.

## 📋 Prérequis

- Docker et Docker Compose installés
- Java 21 (optionnel, Docker suffit)
- Git

## ⚡ Démarrage en 3 étapes

### 1️⃣ Cloner et démarrer

```bash
# Cloner le projet
git clone <repo-url>
cd Collab

# Démarrer tous les services
make start
# ou
docker compose up -d
```

✅ L'application sera disponible sur http://localhost:8080

### 2️⃣ Vérifier que tout fonctionne

```bash
# Voir les logs
make logs

# Vérifier la santé des services
make health
```

### 3️⃣ Arrêter les services

```bash
make stop
# ou
docker compose down
```

## 🛠️ Commandes Essentielles

### Développement quotidien

```bash
# Formater le code avant commit
make format

# Vérifier le formatage
make format-check

# Redémarrer l'application
make restart

# Nettoyer et reconstruire
make clean build
```

### Debug

```bash
# Voir les logs en temps réel
make logs

# Voir les logs d'un service spécifique
docker compose logs -f collab-app
docker compose logs -f postgres
docker compose logs -f kafka
```

### Base de données

```bash
# Se connecter à PostgreSQL
make db-connect
# ou manuellement:
docker exec -it collab-postgres psql -U admin -d collab
```

## 📝 Workflow Git recommandé

### Avant chaque commit

```bash
# 1. Formater le code
make format

# 2. Vérifier que tout compile
make build

# 3. Faire le commit
git add .
git commit -m "feat: votre message"
```

💡 **Astuce**: Installez les hooks Git pour automatiser cela :

```bash
make install-hooks
```

Les hooks vont automatiquement :

- ✅ Formater votre code avec Prettier
- ✅ Vérifier la syntaxe Gradle
- ✅ Valider les fichiers YAML

### Créer une Pull Request

1. Créer une branche depuis `develop`

   ```bash
   git checkout develop
   git pull
   git checkout -b feature/ma-fonctionnalite
   ```

2. Développer et commiter

   ```bash
   make format
   git add .
   git commit -m "feat: ma fonctionnalité"
   ```

3. Pousser et créer la PR

   ```bash
   git push origin feature/ma-fonctionnalite
   ```

4. La CI va automatiquement :
   - ✅ Vérifier le formatage Prettier
   - ✅ Builder l'application
   - ✅ Tester avec Docker Compose
   - ✅ Scanner les vulnérabilités

## 🔧 Commandes Make disponibles

Tapez `make help` pour voir toutes les commandes disponibles.

### Les plus utilisées

| Commande          | Description                       |
| ----------------- | --------------------------------- |
| `make start`      | Démarrer tous les services        |
| `make stop`       | Arrêter tous les services         |
| `make restart`    | Redémarrer l'application          |
| `make logs`       | Voir les logs                     |
| `make format`     | Formater le code                  |
| `make build`      | Compiler l'application            |
| `make clean`      | Nettoyer les fichiers build       |
| `make health`     | Vérifier la santé des services    |
| `make db-connect` | Se connecter à la base de données |

## 🐛 Problèmes courants

### Le port 5432 est déjà utilisé

➡️ **Solution**: Le projet utilise le port **5433** pour PostgreSQL (pas 5432)

```yaml
# Dans compose.yaml
postgres:
  ports:
    - '5433:5432' # 5433 sur l'hôte, 5432 dans le conteneur
```

### L'application ne démarre pas

```bash
# Vérifier les logs
make logs

# Nettoyer et redémarrer
make clean
make start
```

### Problème de formatage

```bash
# Réinstaller Prettier
make install-prettier

# Formater tout
make format
```

### Erreur de connexion à la base de données

```bash
# Vérifier que PostgreSQL est démarré
docker compose ps

# Relancer PostgreSQL
docker compose restart postgres

# Attendre quelques secondes et relancer l'app
docker compose restart collab-app
```

## 📚 Documentation complète

- [README.md](README.md) - Vue d'ensemble du projet
- [DEVOPS.md](DEVOPS.md) - Documentation DevOps complète
- [.github/GITHUB_ACTIONS.md](.github/GITHUB_ACTIONS.md) - Workflows CI/CD

## 🆘 Besoin d'aide ?

1. Consultez la [documentation](README.md)
2. Tapez `make help` pour voir toutes les commandes
3. Demandez à l'équipe sur le channel Slack

---

**🎉 C'est tout ! Bon développement !**
