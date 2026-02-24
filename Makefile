.PHONY: help build up down restart logs clean test shell db-backup lint health status pull rebuild

# Variables
COMPOSE=sudo docker compose
APP_NAME=collab-app
DB_NAME=collab-postgres

help: ## Afficher l'aide
	@echo "📋 Commandes disponibles pour Collab:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'
	@echo ""

build: ## Construire l'image Docker
	@echo "🔨 Construction de l'image Docker..."
	$(COMPOSE) build --no-cache

up: ## Démarrer tous les services
	@echo "🚀 Démarrage des services..."
	$(COMPOSE) up -d
	@echo "✅ Services démarrés!"
	@echo "   Application: http://localhost:8080"
	@echo "   PostgreSQL: localhost:5433"
	@echo "   Kafka: localhost:9092"

down: ## Arrêter tous les services
	@echo "🛑 Arrêt des services..."
	$(COMPOSE) down
	@echo "✅ Services arrêtés!"

restart: ## Redémarrer les services
	@echo "🔄 Redémarrage des services..."
	$(COMPOSE) restart
	@echo "✅ Services redémarrés!"

logs: ## Afficher les logs de l'application
	$(COMPOSE) logs -f app

logs-all: ## Afficher tous les logs
	$(COMPOSE) logs -f

status: ## Voir le statut des services
	@echo "📊 Statut des conteneurs:"
	$(COMPOSE) ps

health: ## Vérifier la santé de l'application
	@echo "🏥 Vérification de la santé..."
	@curl -f http://localhost:8080/actuator/health 2>/dev/null && echo "✅ Application healthy!" || echo "❌ Application unhealthy!"

shell: ## Ouvrir un shell dans le conteneur app
	$(COMPOSE) exec app /bin/sh

db-shell: ## Ouvrir un shell PostgreSQL
	$(COMPOSE) exec postgres psql -U postgres -d collab

clean: ## Nettoyer tout (containers, volumes, images)
	@echo "🧹 Nettoyage complet..."
	$(COMPOSE) down -v
	docker system prune -af
	@echo "✅ Nettoyage terminé!"

clean-volumes: ## Supprimer uniquement les volumes
	@echo "🗑️  Suppression des volumes..."
	$(COMPOSE) down -v
	@echo "✅ Volumes supprimés!"

test: ## Lancer les tests (Gradle)
	@echo "🧪 Lancement des tests..."
	./gradlew test

test-docker: ## Tester le build Docker
	@echo "🐳 Test du build Docker..."
	docker build -t $(APP_NAME):test .
	docker run --rm $(APP_NAME):test java -version
	@echo "✅ Build Docker OK!"

db-backup: ## Créer un backup de la base de données
	@echo "💾 Backup de la base de données..."
	@mkdir -p backups
	$(COMPOSE) exec -T postgres pg_dump -U postgres collab > backups/backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "✅ Backup créé dans backups/"

db-restore: ## Restaurer la dernière sauvegarde (FILE=backup.sql)
	@if [ -z "$(FILE)" ]; then \
		echo "❌ Erreur: Spécifiez FILE=backup.sql"; \
		exit 1; \
	fi
	@echo "📥 Restauration de $(FILE)..."
	$(COMPOSE) exec -T postgres psql -U postgres collab < $(FILE)
	@echo "✅ Restauration terminée!"

pull: ## Télécharger les dernières images Docker
	@echo "⬇️  Téléchargement des images..."
	$(COMPOSE) pull

rebuild: down build up ## Rebuild complet (down + build + up)
	@echo "✅ Rebuild terminé!"

dev: ## Démarrer en mode développement avec logs
	@echo "👨‍💻 Mode développement..."
	$(COMPOSE) up

prod-build: ## Build optimisé pour production
	@echo "🏭 Build production..."
	./gradlew clean bootJar --no-daemon
	docker build -t $(APP_NAME):prod .
	@echo "✅ Build production OK!"

lint: ## Vérifier le code avec Gradle
	@echo "🔍 Vérification du code..."
	./gradlew check

watch: ## Suivre les logs en temps réel
	$(COMPOSE) logs -f --tail=100

stats: ## Afficher les statistiques des conteneurs
	docker stats collab-app collab-postgres collab-kafka collab-zookeeper

network: ## Afficher les informations réseau
	docker network inspect collab_default

volumes: ## Lister les volumes
	docker volume ls | grep collab

install-hooks: ## Installer les git hooks (pre-commit, pre-push)
	@echo "🪝 Installation des git hooks..."
	@mkdir -p .git/hooks
	@echo '#!/bin/sh\nmake lint' > .git/hooks/pre-commit
	@chmod +x .git/hooks/pre-commit
	@echo "✅ Hooks installés!"

ci: build test-docker ## Simulation CI local (build + test)
	@echo "✅ Pipeline CI local terminé!"

all: clean build up logs ## Tout reconstruire et afficher les logs
