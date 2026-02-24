#!/bin/bash

# 🚀 Installation rapide de Collab
# Script pour installer et démarrer le projet en une commande

set -e

echo "🚀 Installation de Collab..."
echo ""

# Vérifier Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé. Installez Docker d'abord:"
    echo "   https://docs.docker.com/get-docker/"
    exit 1
fi

echo "✅ Docker détecté"

# Vérifier Docker Compose
if ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé."
    exit 1
fi

echo "✅ Docker Compose détecté"
echo ""

# Copier .env.example si .env n'existe pas
if [ ! -f .env ]; then
    echo "📝 Création du fichier .env..."
    cp .env.example .env
    echo "✅ .env créé (modifiez-le si nécessaire)"
else
    echo "✅ .env existe déjà"
fi

echo ""

# Installer Prettier
if [ ! -d node_modules ]; then
    echo "📦 Installation de Prettier..."
    npm install
    echo "✅ Prettier installé"
else
    echo "✅ Prettier déjà installé"
fi

echo ""

# Installer les git hooks
echo "🪝 Installation des git hooks..."
make install-hooks
echo "✅ Git hooks installés"

echo ""
echo "🎉 Installation terminée !"
echo ""
echo "🚀 Pour démarrer l'application:"
echo "   make start"
echo ""
echo "📖 Pour voir le guide complet:"
echo "   cat QUICKSTART.md"
echo "   ou"
echo "   make QUICKSTART"
echo ""
echo "💡 Voir toutes les commandes:"
echo "   make help"
echo ""
