#!/bin/bash

# Script de préparation pour le premier push vers GitHub

echo "🚀 Configuration du repository GitHub Actions"
echo ""

# Vérifier si git est initialisé
if [ ! -d .git ]; then
    echo "❌ Erreur: Ce n'est pas un repository git"
    exit 1
fi

# Ajouter tous les fichiers
echo "📦 Ajout des fichiers..."
git add .

# Commit
echo "💾 Création du commit..."
git commit -m "feat: Add GitHub Actions CI/CD pipeline

- Add CI/CD workflow with build, test, and deploy
- Add Docker publish workflow
- Add Dependabot configuration
- Add PR and issue templates
- Add Makefile for easy commands
- Configure multi-stage builds
- Add health checks and monitoring
"

echo ""
echo "✅ Commit créé!"
echo ""
echo "📋 Prochaines étapes:"
echo ""
echo "1. Créer un repository sur GitHub"
echo "2. Ajouter le remote:"
echo "   git remote add origin https://github.com/VOTRE_USERNAME/Collab.git"
echo ""
echo "3. Pousser le code:"
echo "   git push -u origin main"
echo ""
echo "4. Configurer les secrets (optionnel):"
echo "   - DOCKER_USERNAME"
echo "   - DOCKER_PASSWORD"
echo ""
echo "5. Activer les environnements:"
echo "   - staging"
echo "   - production"
echo ""
echo "6. Voir la documentation: .github/GITHUB_ACTIONS.md"
echo ""
echo "🎉 Prêt à déployer!"
