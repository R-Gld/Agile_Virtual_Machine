# Arboresence des tickets (AVANT MISE AU PROPRE)
Legende :   
**EPIC**  
*Story*  
Taches

* **[(#1)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-1) IHM**
  * *[(#37)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-37) Editeur de texte*
    * [(#38)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-38) Avoir une zone d'ecriture
    * [(#39)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-39) Numerotation de ligne
    * [(#40)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-40) Sauvegarde de fichier
    * [(#41)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-41) Ouverture d'un fichier
  * *[(#42)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-42) Déboguage éditeur*
    * [(#43)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-43) Executer en mode debogue
    * [(#44)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-44) Mise en place de breakpoints
  * *[(#45)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-45) compilation interface*
    * [(#46)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-46) Bouton compilation
    * [(#47)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-47) Bouton compilation/execution
* **[(#2)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-2) Compilation du minijaja**
  * *[(#9)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-9) Définition de la grammaire*
    * [(#14)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-14) fichier de grammaire ANTLR (.g4)
    * [(#15)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-15) Visiteur/listener
    * [(#16)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-16) Créer un AST à partir du parse tree (Généré par ANTLR)
    * [(#18)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-18) Contrôle de Type et Table des Symboles > Implémenter le contrôle de types sur l'AST
  * [(#13)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-13) Analyse Lexicale et Syntaxique (Parsing) > Mettre en place l'analyseur lexical et syntaxique avec ANTLR.
    * [(#14)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-14) fichier de grammaire ANTLR (.g4)
  * [(#19)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-19) Table des symboles
  * [(#21)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-21) Génération du JajaCode > Générer le code JajaCode à partir de l'AST validé.
    * [(#22)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-22) Structure du JajaCode
    * [(#23)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-23) Visiteur de compilation
* **[(#3)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-3) Interprétation du jajacode**
  * [(#49)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-49) Créer modèle d'exécution pour l'AST (jajacode)
* **[(#6)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-6) Interprétation du minijaja**
  * [(#48)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-48) Créer modèle d'exécution pour l'AST (minijaja)
  * [(#50)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-50) Interprétation pas-à-pas (minijaja)
* *[(#5)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-5) Message d'erreur*
  * [(#31)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-31) Établir une liste et catégorisée des erreurs possibles (lexicales, syntaxiques, sémantiques, typage, exécution)
  * [(#32)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-32) Implémenter la détection et l’affichage des erreurs lexicales (caractères invalides, tokens inconnus, etc.)
  * [(#33)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-33) Implémenter l’affichage des erreurs syntaxiques (ex : symbole attendu manquant, parenthèse non fermée, etc.)
  * [(#34)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-34) Implémenter l’affichage des erreurs liées à la sémantique (ex : variable non déclarée) et au typage (ex : incompatibilité int/bool)
  * [(#35)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-35) Définir un format standard pour tous les messages d’erreurs (préfixe, structure, lisibilité)
  * [(#36)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-36) Améliorer la sortie console/IHM lors de la compilation pour que les erreurs soient immédiatement visibles
* *[(#4)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-4) Visualisation de la mémoire*
* Taches orphelines
  * [(#8)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-8) compréhension de miniJAJA
  * [(#20)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-20) Visiteur de contrôle de type
  * [(#51)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-51) mapping code source vers ast pour surligner le code source
  * [(#52)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-52) Contrôles d'exécution (fonctions play/pause/step-into/step-over/step-out/reset) 
  * [(#53)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-53) Ajout breakpoints
  * [(#54)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-54) Messages d'erreur runtime avec position et état
  * [(#55)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-55) Mettre en place le versionnement du projet
  * [(#56)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-56) Trouver des tickets à rajouter sur jira

# Arboressence ticket (Après remise au propre)
Legende :   
**EPIC**  
*Story*  
Taches

* **[(#1)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-1) IHM** <span style="color:blue; font-weight:bold">Module interface</span>
  * *[(#37)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-37) Editeur de texte*
    * [(#38)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-38) Avoir une zone d'ecriture <span style="color:red; font-weight:bold">Release 1</span>
    * [(#39)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-39) Numerotation de ligne <span style="color:red; font-weight:bold">Release 1</span>
    * [(#40)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-40) Sauvegarde de fichier <span style="color:gold; font-weight:bold">Release 2</span>
    * [(#41)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-41) Ouverture d'un fichier <span style="color:gold; font-weight:bold">Release 2</span>
  * *[(#42)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-42) Déboguage éditeur*
    * [(#43)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-43) Executer en mode debogue <span style="color:gold; font-weight:bold">Release 2</span>
    * [(#44)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-44) Mise en place de breakpoints <span style="color:gold; font-weight:bold">Release 2</span>
  * *[(#45)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-45) compilation interface*
    * [(#46)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-46) Bouton compilation <span style="color:red; font-weight:bold">Release 1</span>
    * [(#47)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-47) Bouton compilation/execution <span style="color:red; font-weight:bold">Release 1</span>
* **[(#2)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-2) Compilation du minijaja**
  * *[(#9)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-9) Définition de la grammaire* <span style="color:blue; font-weight:bold">Module analyse minijaja</span>
    * <s>[(#14)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-14) fichier de grammaire ANTLR (.g4)</s> [(new)](#grammaire-basique) grammaire basique initial <span style="color:red; font-weight:bold">Release 1</span>
    * [(new)](#grammaire-complète) Grammaire complète <span style="color:gold; font-weight:bold">Release 2</span>
    * [(#15)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-15) Visiteur/listener <span style="color:red; font-weight:bold">Release 1</span>
    * [(#16)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-16) Créer un AST à partir du parse tree (Généré par ANTLR) <span style="color:red; font-weight:bold">Release 1</span>
    * [(#18)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-18) Contrôle de Type et Table des Symboles > Implémenter le contrôle de types sur l'AST <span style="color:gold; font-weight:bold">Release 2</span>
  * <s>[(#13)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-13) Analyse Lexicale et Syntaxique (Parsing) > Mettre en place l'analyseur lexical et syntaxique avec ANTLR.
    * [(#14)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-14) fichier de grammaire ANTLR (.g4)</s>
  * <s>**[(#6)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-6) Interprétation du minijaja**</s> *[(#6)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-6) Interprétation du minijaja* <span style="color:blue; font-weight:bold">Module Interpretation minijaja</span>
    * <s>[(#48)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-48) Créer modèle d'exécution pour l'AST (minijaja)</s>
    * <s>[(#50)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-50) Interprétation pas-à-pas (minijaja)</s>
    * [(#21)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-21) Génération du JajaCode > Générer le code JajaCode à partir de l'AST <s>validé.</s> <span style="color:red; font-weight:bold">Release 1</span>
    * [(new)]() Ecrire la grammaire du jajacode <span style="color:red; font-weight:bold">Release 1</span>
  * *[(#5)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-5) Message d'erreur* <span style="color:blue; font-weight:bold">Module gestion d'erreur ? (pas sur car si erreur géré lors de la compil compliquer de sortir cette partie je pense)</span>
    * [(new)]() Reperage du lieu de l'erreur sans classifier l'erreur <span style="color:red; font-weight:bold">Release 1</span>
    * [(#31)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-31) Établir une liste et catégorisée des erreurs possibles (lexicales, syntaxiques, sémantiques, typage, exécution) <span style="color:gold; font-weight:bold">Release 2</span>
    * [(#32)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-32) Implémenter la détection et l’affichage des erreurs lexicales (caractères invalides, tokens inconnus, etc.) <span style="color:gold; font-weight:bold">Release 2</span>
    * [(#33)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-33) Implémenter l’affichage des erreurs syntaxiques (ex : symbole attendu manquant, parenthèse non fermée, etc.) <span style="color:gold; font-weight:bold">Release 2</span>
    * [(#34)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-34) Implémenter l’affichage des erreurs liées à la sémantique (ex : variable non déclarée) et au typage (ex : incompatibilité int/bool) <span style="color:gold; font-weight:bold">Release 2</span>
    * [(#35)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-35) Définir un format standard pour tous les messages d’erreurs (préfixe, structure, lisibilité) <span style="color:red; font-weight:bold">Release 1</span>
    * [(#36)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-36) Améliorer la sortie console/IHM lors de la compilation pour que les erreurs soient immédiatement visibles <span style="color:red; font-weight:bold">Release 1</span>

  * [(#19)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-19) Table des symboles
  * [(#21)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-21) Génération du JajaCode > Générer le code JajaCode à partir de l'AST validé.
    * <s>[(#22)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-22) Structure du JajaCode</s>
    * [(#23)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-23) Visiteur de compilation
* *[(#4)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-4) Visualisation de la mémoire* <span style="color:blue; font-weight:bold">Module Mémoire</span>


## Remarques

### Inverser le contenu de la story 9 avec l'epic 2 :  
epic 2 `Compilation du minijaja`:
```
Avant : 
En tant que utilisateur
j'aimerai pouvoir compiler mon code Minijaja
Afin de l'exploiter

Après:
En tant qu'utilisateur 
Je veux pouvoir utiliser le langage minijaja
Afin de réaliser des programmes
```
story 9 `Definitaion de la grammaire`:
```
Avant : 
En tant qu'utilisateur 
Je veux pouvoir utiliser le langage minijaja
Afin de réaliser des programmes

Après : 
En tant que utilisateur
j'aimerai que le minijaja réponde à des règles de grammaires spécifiques
Afin que mes programmes puissent fonctionner (a voir si on formule autrement)
```

### Modification epic 6
```
Avant : 
En tant qu'utilisateur,
je veux observer l’interprétation pas à pas du minijaja
afin d'avoir le résultat de mon programme

Après : 
En tant qu'utilisateur
Je veux obtenir du jajacode comme résultat de compilation du minijaja
Afin d'observer la transformation effectuer par le compilateur
```

### Remarques diverse
* Le tiket 16 est un résultat du ticket 15, on le garde ?
* Le ticket 18 fait plus parti de la gestion d'erreur
* Pour les ticket 15 et 16, est-ce qu'on les séparent comme pour la grammaire pour les avoirs sur les deux release ?
* Supression epic 3 ?
* Pour l'epic 6 est-ce qu'on garde l'epic et on refais les même story que pour la compilation du minijaja mais pour le jajacode ? ou on la transforme comme une story de la compilation du minijaja
* Est-ce qu'on mets le jajacode pour la release 1 ? (je voit pas trop comment faire sans)
* Gestion d'erreur :
  * A la compilation : récupération de la ligne plus simple je pense
  * A l'execution : plus compliquer car le "code" qu'on execute n'est plus du minijaja mais du jajacode donc les numéro de lignes change
* #23 et #15 et #20 pareil, mais 23 peut-être plus détaillé
* #19 en gros c'est la mémoire ?

### Grammaire basique

Ecriture du fichier de grammaire antlr basique pour la release 1 :  
* Création de classe
* methode main
* initialisation de variable
* affectation de variable
* et plus si on a du temps

Exemple de code de la release 1 : 
```
class C {
  int x = 0;

  main {
    x = 12;
  }
}
```

### Grammaire complète
Ecriture du fichier de grammaire avec les règles non implémenter lors de la release 1


<span style="color:red; font-weight:bold">Release 1</span>
<span style="color:gold; font-weight:bold">Release 2</span>
<span style="color:blue; font-weight:bold">Module </span>


* **[(#3)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-3) Interprétation du jajacode**
  * [(#49)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-49) Créer modèle d'exécution pour l'AST (jajacode)

* Taches orphelines
  * [(#8)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-8) compréhension de miniJAJA
  * [(#51)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-51) mapping code source vers ast pour surligner le code source
  * [(#52)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-52) Contrôles d'exécution (fonctions play/pause/step-into/step-over/step-out/reset) 
  * [(#53)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-53) Ajout breakpoints
  * [(#54)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-54) Messages d'erreur runtime avec position et état
  * [(#55)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-55) Mettre en place le versionnement du projet
  * [(#56)](https://disc.univ-fcomte.fr/cr700-jira/browse/AVM25G7-56) Trouver des tickets à rajouter sur jira
