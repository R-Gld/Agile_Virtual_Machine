#!/bin/bash

# Script de compilation du rapport LaTeX
# Usage: ./compile.sh

set -e

echo "====================================="
echo "Compilation du rapport MiniJAJA"
echo "====================================="
echo ""

# Vérifier si pdflatex est installé
if ! command -v pdflatex &> /dev/null; then
    echo "ERREUR: pdflatex n'est pas installé."
    echo "Veuillez installer une distribution LaTeX (TeX Live, MiKTeX, etc.)"
    exit 1
fi

echo "Méthode de compilation:"
echo "  1) Maven (recommandé)"
echo "  2) pdflatex (manuel)"
echo ""
read -p "Choisissez une méthode (1 ou 2): " method

case $method in
    1)
        echo ""
        echo "Compilation avec Maven..."
        mvn clean compile
        echo ""
        echo "✓ Compilation terminée!"
        echo "Le PDF se trouve dans: target/latex/rapport_principal.pdf"

        # Ouvrir le PDF si possible
        if [[ "$OSTYPE" == "darwin"* ]]; then
            read -p "Voulez-vous ouvrir le PDF? (o/n): " open_pdf
            if [ "$open_pdf" = "o" ]; then
                open target/latex/rapport_principal.pdf
            fi
        fi
        ;;
    2)
        echo ""
        echo "Compilation avec pdflatex..."
        cd src/main/latex

        # Première passe
        echo "Première passe..."
        pdflatex -interaction=nonstopmode rapport_principal.tex

        # Deuxième passe pour les références
        echo "Deuxième passe (pour les références)..."
        pdflatex -interaction=nonstopmode rapport_principal.tex

        # Nettoyage des fichiers temporaires
        echo "Nettoyage des fichiers temporaires..."
        rm -f *.aux *.log *.out *.toc

        echo ""
        echo "✓ Compilation terminée!"
        echo "Le PDF se trouve dans: src/main/latex/rapport_principal.pdf"

        # Ouvrir le PDF si possible
        if [[ "$OSTYPE" == "darwin"* ]]; then
            read -p "Voulez-vous ouvrir le PDF? (o/n): " open_pdf
            if [ "$open_pdf" = "o" ]; then
                open rapport_principal.pdf
            fi
        fi

        cd ../../..
        ;;
    *)
        echo "Choix invalide. Utilisation de Maven par défaut..."
        mvn clean compile
        ;;
esac

echo ""
echo "====================================="
