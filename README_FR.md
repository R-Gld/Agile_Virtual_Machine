[![Coverage](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=coverage&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)
[![Duplicated Lines (%)](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=duplicated_lines_density&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)
[![Lines of Code](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=ncloc&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)

# Projet AVM 2025 - Groupe 7

## Description du projet

Le projet **AVM 2025 (Agile Virtual Machine)** s'inscrit dans le cadre du module *Développement Agile de Machines Virtuelles* du Master Informatique de l'Université de Franche-Comté.
L'objectif principal est de **concevoir, compiler et interpréter un langage de programmation minimaliste**, nommé **MiniJaja**, ainsi que sa version compilée **JajaCode**.

Le projet s'appuie sur une démarche **Agile** (type *Scrum*) : développement itératif, sprints de 2 semaines, intégration continue et validation régulière via des tests et revues de code.

---

## Objectifs pédagogiques

- **Maîtriser les concepts avancés de programmation et de compilation** :
    - Analyse lexicale et syntaxique ;
    - Construction d'arbres de syntaxe abstraite (ASA) ;
    - Contrôle de type et sémantique interprétative ;
    - Compilation MiniJaja → JajaCode ;
    - Interprétation des deux langages.

- **Appliquer les principes du Génie Logiciel** :
    - Architecture logicielle modulaire (analyseur, mémoire, interpréteur, IHM...) ;
    - Validation par tests unitaires, d'intégration et tests de recette ;
    - Gestion de version avec Git et intégration continue avec GitLab CI/CD ;
    - Suivi qualité avec **SonarQube** et gestion des dépendances avec **Maven/Nexus**.

- **Travail en équipe agile** :
    - Découpage du backlog produit ;
    - Planification de sprints avec objectifs clairs ;
    - Pair programming, revues de code, et rétrospectives ;
    - Utilisation de Jira pour le suivi des tâches et des releases.

---

## Outils et environnement

| Outil            | Rôle                                                          |
|------------------|---------------------------------------------------------------|
| **Java 21**      | Langage principal du projet                                   |
| **Maven**        | Construction du projet et gestion des dépendances             |
| **Git / GitLab** | Gestion de version et intégration continue                    |
| **SonarQube**    | Analyse de la qualité du code et couverture des tests         |
| **Nexus**        | Dépôt de dépendances et artefacts Maven                       |
| **JavaFX**       | Interface graphique                                           |
| **JUnit**        | Tests unitaires et d'intégration                              |
| **ANTLR**        | Génération automatique des analyseurs lexicaux et syntaxiques |


---

## Architecture et modules

Le projet MiniJAJA est structuré autour de **7 modules Maven** :

| Module | Rôle | Technologies clés |
|--------|------|-------------------|
| **LexerParser** | Analyse lexicale/syntaxique, AST et interprétation | ANTLR 4, JUnit 5, Cucumber |
| **Memoire** | Gestion de la mémoire runtime et tables de symboles | Java natif |
| **CLI** | Interface en ligne de commande pour compilation/exécution | Apache Commons CLI |
| **GUI** | Interface graphique interactive avec JavaFX | JavaFX 23, TestFX |
| **ASTExporter** | Visualisation et export des arbres de syntaxe | ANTLR 4 |
| **CoverageReport** | Agrégation des rapports de couverture | JaCoCo |
| **Rapport** | Génération de documentation LaTeX | pdflatex |

### Dépendances entre modules

```
LexerParser (cœur du projet)
  └─ Memoire
CLI → LexerParser → Memoire
GUI → LexerParser + CLI → Memoire
ASTExporter → LexerParser
CoverageReport (agrégateur de tous les modules)
Rapport (documentation indépendante)
```

### Responsabilités des modules

- **LexerParser** : Cœur du projet. Implémente la compilation MiniJaja → JajaCode et l'interprétation des deux langages.
- **Memoire** : Abstractions pour la gestion d'état runtime (variables, portées, symboles).
- **CLI** : Point d'entrée pour l'utilisation en ligne de commande (compilation, exécution, debug).
- **GUI** : IDE JavaFX avec éditeur, console et visualisation d'exécution pas-à-pas.
- **ASTExporter** : Utilitaire pour générer des diagrammes des arbres de syntaxe abstraite.
- **CoverageReport** : Module d'agrégation pour les rapports de couverture JaCoCo.
- **Rapport** : Génération du document de synthèse du projet en PDF via LaTeX.

---

## Installation et configuration

### Prérequis

- **Java 21** ou supérieur
- **Maven 3.9.9** ou supérieur
- **Git** (pour clonage du dépôt)
- **pdflatex** (optionnel, pour générer le Rapport)

### Étapes d'installation

#### 1. Cloner le dépôt

```bash
git clone https://github.com/R-Gld/Agile_Virtual_Machine.git
cd Agile_Virtual_Machine
```

#### 2. Construction du projet

**Build complet :**
```bash
mvn clean install
```

**Build rapide (modules principaux uniquement) :**
```bash
mvn clean compile -pl '!Rapport'
```

**Installation des modules de base (sans tests) :**
```bash
mvn install -pl 'LexerParser,CLI' -am -DskipTests
```

#### 3. Exécution de l'application

**Interface CLI (compilateur) :**
```bash
# Après build avec maven package
java -jar CLI/target/CLI-2.1-SNAPSHOT-jar-with-dependencies.jar [options] <fichier.mj>
```

**Interface GUI (IDE interactif) :**
```bash
# Lancer directement via Maven
mvn javafx:run -pl GUI

# Ou après build avec maven package
java -jar GUI/target/GUI-2.1-SNAPSHOT-jar-with-dependencies.jar
```

**Exportateur AST (visualisation des arbres) :**
```bash
# Après build avec maven package
java -jar ASTExporter/target/ASTExporter-2.1-SNAPSHOT-shaded.jar <fichier.mj> <sortie.png>
```

#### 4. Configuration IDE

**IntelliJ IDEA / Eclipse :**
1. Ouvrir le projet comme projet Maven
2. L'IDE détecte automatiquement les 7 modules
3. Maven génère les fichiers ANTLR à la première compilation
4. Configurer le JDK 21 dans les paramètres du projet

---

## Tests et validation

### Exécution des tests

**Tests complets (modules principaux) :**
```bash
mvn test -pl '!GUI,!CoverageReport,!Rapport'
```

**Tests d'un module spécifique :**
```bash
mvn test -pl LexerParser
mvn test -pl CLI
mvn test -pl Memoire
```

**Tests GUI (nécessite installation préalable des dépendances) :**
```bash
mvn install -pl 'LexerParser,CLI' -am -DskipTests
mvn test -pl GUI
```

### Couverture de code (JaCoCo)

**Générer les rapports de couverture :**
```bash
mvn jacoco:prepare-agent test jacoco:report
```

**Rapport agrégé (tous les modules) :**
```bash
mvn clean test
mvn jacoco:report-aggregate -pl CoverageReport
```

Les rapports de couverture sont générés dans :
- Par module : `<module>/target/site/jacoco/index.html`
- Agrégé : `CoverageReport/target/site/jacoco-aggregate/index.html`

### Frameworks de test utilisés

| Framework | Utilisation |
|-----------|-------------|
| **JUnit 5** | Tests unitaires et tests d'intégration |
| **Cucumber** | Tests de comportement (BDD) avec fichiers `.feature` |
| **Mockito** | Mock d'objets pour tests isolés |
| **TestFX** | Tests de l'interface graphique JavaFX |
| **JaCoCo** | Métriques de couverture de code |

### Tests headless (serveurs sans interface graphique)

Le module GUI nécessite un affichage pour les tests. Sur un système sans interface X11 :

```bash
# Installation de Xvfb (Linux/Ubuntu)
sudo apt-get install xvfb

# Lancement des tests GUI en mode headless
xvfb-run -a mvn test -pl GUI
```

### Analyse de qualité

Les métriques de qualité (couverture, duplication, bugs) sont disponibles sur le tableau de bord SonarQube :
[Dashboard SonarQube - AVM 2025 Groupe 7](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)

---

## Méthodologie de travail

1. **Initialisation du projet** : définition du backlog produit et des rôles dans l'équipe.
2. **Sprints bi-hebdomadaires** :
    - Planification (user stories, estimations, priorités) ;
    - Développement en pair programming ;
    - Intégration continue et revue de code ;
    - Revue de sprint et rétrospective.
3. **Suivi via Jira** : gestion du backlog, des sprints, et traçabilité des tâches.
4. **CI/CD GitLab** :
    - Lancement automatique du build Maven ;
    - Exécution des tests ;
    - Analyse SonarQube ;
    - Déploiement des artefacts.

---

## Releases

- **Release 1** : première version fonctionnelle livrée début novembre.
    - Compilation partielle MiniJaja → JajaCode ;
    - Interprétation des programmes simples ;
    - Tests unitaires initiaux et CI configurée.

- **Release 2** : version finale livrée mi-décembre.
    - Compilation complète et exécution pas-à-pas ;
    - Interface graphique JavaFX intégrée ;
    - Jeux de tests complets et métriques de qualité conformes.

---

## Évaluation et livrables

- **Rapport final** (15 décembre – rendu Moodle) :
    - Architecture logicielle ;
    - Description des choix techniques et des tests ;
    - Résultats sur la batterie de tests fournie ;
    - Compte rendu d'organisation agile ;
    - Rétrospective du projet.

- **Soutenance** (18–19 décembre) :
    - 20 min de présentation technique ;
    - 15 min de démonstration logicielle ;
    - 15 min de questions/réponses.

---

## Équipe – Groupe 7

- **Romain GALLAND** ([@R-Gld](https://github.com/R-Gld) sur Github)
- **Lucas LAURET** ([@llauret](https://github.com/llauret) sur Github)
- **Léo MAUGERI** ([@PlsJustDoIt](https://github.com/PlsJustDoIt) sur Github)
- **Félix RIAT**
- **Théo VALFREY**
- **Ahmed DJEMAOUI** ([@djemaouiahmed](https://github.com/djemaouiahmed) sur Github)
- **Javad AFSHAR**
