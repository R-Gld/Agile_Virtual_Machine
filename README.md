[![Coverage](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=coverage&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)
[![Duplicated Lines (%)](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=duplicated_lines_density&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)
[![Lines of Code](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=ncloc&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)

# Projet AVM 2025 - Groupe 7

## Description du projet

Le projet **AVM 2025 (Agile Virtual Machine)** s’inscrit dans le cadre du module *Développement Agile de Machines Virtuelles* du Master Informatique de l’Université de Franche-Comté.  
L’objectif principal est de **concevoir, compiler et interpréter un langage de programmation minimaliste**, nommé **MiniJaja**, ainsi que sa version compilée **JajaCode**.

Le projet s’appuie sur une démarche **Agile** (type *Scrum*) : développement itératif, sprints de 2 semaines, intégration continue et validation régulière via des tests et revues de code.

---

## Objectifs pédagogiques

- **Maîtriser les concepts avancés de programmation et de compilation** :
    - Analyse lexicale et syntaxique ;
    - Construction d’arbres de syntaxe abstraite (ASA) ;
    - Contrôle de type et sémantique interprétative ;
    - Compilation MiniJaja → JajaCode ;
    - Interprétation des deux langages.

- **Appliquer les principes du Génie Logiciel** :
    - Architecture logicielle modulaire (analyseur, mémoire, interpréteur, IHM...) ;
    - Validation par tests unitaires, d’intégration et tests de recette ;
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
| **JUnit**        | Tests unitaires et d’intégration                              |
| **ANTLR**        | Génération automatique des analyseurs lexicaux et syntaxiques |


---

## Méthodologie de travail

1. **Initialisation du projet** : définition du backlog produit et des rôles dans l’équipe.
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
    - Compte rendu d’organisation agile ;
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