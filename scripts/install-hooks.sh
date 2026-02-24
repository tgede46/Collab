#!/bin/bash

# Installation des Git Hooks - Version SIMPLE et RAPIDE
# Seulement Prettier pour formater automatiquement le code

echo "🔧 Installation des Git Hooks (version légère)..."
echo ""

# Créer le dossier hooks si nécessaire
mkdir -p .git/hooks

# Pre-commit hook - SIMPLE: Juste Prettier !
cat > .git/hooks/pre-commit << 'EOFHOOK'
#!/bin/bash

echo "🎨 Formatage automatique avec Prettier..."

# Formater les fichiers modifiés
if command -v npx &> /dev/null; then
    FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(md|yml|yaml|json)$')
    if [ -n "$FILES" ]; then
        echo "$FILES" | xargs npx prettier --write 2>/dev/null
        echo "$FILES" | xargs git add
        echo "✅ Fichiers formatés!"
    else
        echo "✅ Aucun fichier à formater"
    fi
else
    echo "⚠️  npx non trouvé. Exécutez: npm install"
    echo "   (commit autorisé sans formatage)"
fi

exit 0
EOFHOOK

chmod +x .git/hooks/pre-commit

echo "✅ Hook pre-commit installé!"
echo ""
echo "📝 Le hook fait :"
echo "  - 🎨 Formate automatiquement avec Prettier (rapide!)"
echo ""
echo "💡 Astuce : Pour commit sans hook :"
echo "   git commit --no-verify -m \"message\""
echo ""

# Script de préparation pour le premier push vers GitHub
function setup_github() {
    echo ""
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
    git commit -m "feat: Add GitHub Actions CI/CD pipeline with Prettier

- Add CI/CD workflow with build, test, and deploy
- Add Prettier workflow for code formatting
- Add Docker publish workflow
- Add Dependabot configuration
- Add PR and issue templates
- Add Makefile for easy commands
- Add Git hooks for validation
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
}

# Demander si l'utilisateur veut aussi configurer GitHub
echo "Voulez-vous aussi préparer la configuration GitHub ? (y/N)"
read -r response
if [[ "$response" =~ ^[Yy]$ ]]; then
    setup_github
fi
