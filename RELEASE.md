# Guide de Release

Ce document décrit le processus de création de releases pour le projet MiniJAJA via GitLab CI/CD.

## Prérequis

### Variables d'environnement GitLab CI/CD

Les variables suivantes doivent être configurées dans **Settings > CI/CD > Variables** :

| Variable | Description | Protected | Masked |
|----------|-------------|-----------|--------|
| `MAVEN_ENT_USERNAME` | Username pour Nexus | Oui | Non |
| `MAVEN_ENT_PASSWORD` | Password pour Nexus | Oui | Oui |
| `MAVEN_MASTER_PASSWORD` | Master password Maven | Oui | Oui |
| `SONAR_HOST_URL` | URL SonarQube | Non | Non |
| `SONAR_TOKEN` | Token SonarQube | Oui | Oui |
| `GITLAB_SSH_PRIVATE_KEY` | Clé SSH pour push Git | Oui | Oui |

### Configuration de la clé SSH

Pour générer et configurer la clé SSH permettant au CI de pusher les tags :

```bash
# Générer une clé SSH dédiée au CI
ssh-keygen -t rsa -b 4096 -C "ci-bot@groupe7.fr" -f gitlab-ci-key

# Ajouter la clé publique dans GitLab
# Settings > Repository > Deploy Keys
# - Copier le contenu de gitlab-ci-key.pub
# - Cocher "Write access allowed"

# Ajouter la clé privée dans les variables CI/CD
# Settings > CI/CD > Variables
# - Nom : GITLAB_SSH_PRIVATE_KEY
# - Valeur : contenu de gitlab-ci-key (attention aux retours à la ligne)
# - Type : File (recommandé) ou Variable
# - Protected : Oui
# - Masked : Non (les clés SSH sont trop longues)
```

## Processus de release

Le système de release utilise le **Maven Release Plugin** et se décompose en deux étapes :

### 1. Release Prepare

Cette étape :
- Vérifie qu'il n'y a pas de modifications non commitées
- Demande la version de release et la prochaine version SNAPSHOT
- Met à jour les versions dans tous les pom.xml (multi-module)
- Commit les changements avec le préfixe `[Release]`
- Crée un tag Git (format : `v1.0.0`)
- Push les changements et le tag vers le dépôt

### 2. Release Perform

Cette étape :
- Checkout le tag créé
- Construit le projet depuis le tag
- Exécute les tests (optionnel, actuellement skippés)
- Déploie les artefacts vers Nexus (repository maven-releases)

## Utilisation

### Créer une release depuis l'interface GitLab

1. Aller sur la branche `main`
2. Ouvrir **CI/CD > Pipelines**
3. Cliquer sur le dernier pipeline de `main`
4. Dans le stage `release`, cliquer sur le bouton play de `release:prepare`
5. Maven vous demandera :
   - **Release version** : version à publier (ex: `1.0.0`)
   - **Next development version** : prochaine version SNAPSHOT (ex: `1.1-SNAPSHOT`)
6. Attendre la fin de `release:prepare`
7. Vérifier que le tag a bien été créé : **Repository > Tags**
8. Lancer `release:perform` manuellement
9. Vérifier le déploiement sur Nexus

### Versions interactives vs automatiques

Par défaut, Maven Release Plugin demande confirmation pour les versions. Pour automatiser :

```bash
mvn release:prepare -DreleaseVersion=1.0.0 -DdevelopmentVersion=1.1-SNAPSHOT -B
```

Cette option peut être ajoutée au job CI si vous souhaitez passer les versions en paramètres du job manuel.

## Configuration Maven Release Plugin

Le plugin est configuré dans le `pom.xml` racine :

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-release-plugin</artifactId>
    <version>3.1.1</version>
    <configuration>
        <tagNameFormat>v@{project.version}</tagNameFormat>
        <autoVersionSubmodules>true</autoVersionSubmodules>
        <releaseProfiles>release</releaseProfiles>
        <scmCommentPrefix>[Release] </scmCommentPrefix>
        <goals>deploy</goals>
    </configuration>
</plugin>
```

### Paramètres importants

- `tagNameFormat` : format du tag Git (v1.0.0, v1.1.0, etc.)
- `autoVersionSubmodules` : met à jour tous les modules avec la même version
- `releaseProfiles` : active le profil Maven `release` (si défini)
- `scmCommentPrefix` : préfixe des commits de release
- `goals` : objectif Maven à exécuter lors du `release:perform`

## Workflow complet

```
main (1.1-SNAPSHOT)
    |
    | [CI] release:prepare (manuel)
    |   - Version release : 1.1.0
    |   - Prochaine version : 1.2-SNAPSHOT
    |
    +---> Commit : [Release] prepare release v1.1.0
    |
    +---> Tag : v1.1.0
    |
    +---> Commit : [Release] prepare for next development iteration
    |
main (1.2-SNAPSHOT)
    |
    | [CI] release:perform (manuel)
    |   - Checkout tag v1.1.0
    |   - Build & Deploy vers Nexus
    |
Nexus : artefact 1.1.0 publié
```

## Rollback en cas d'erreur

Si `release:prepare` échoue ou si vous souhaitez annuler :

```bash
# Localement (pour test)
mvn release:rollback

# Supprimer le tag local et distant
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0

# Réinitialiser les modifications
git reset --hard HEAD~2
```

En CI, il suffit de :
1. Supprimer le tag dans GitLab : **Repository > Tags > Delete**
2. Réinitialiser le commit sur `main` si nécessaire

## Bonnes pratiques

1. Toujours créer les releases depuis `main`
2. S'assurer que tous les tests passent avant de lancer une release
3. Vérifier SonarQube avant la release
4. Suivre le versioning sémantique : `MAJOR.MINOR.PATCH`
   - MAJOR : changements incompatibles
   - MINOR : nouvelles fonctionnalités rétrocompatibles
   - PATCH : corrections de bugs
5. Documenter les changements dans un CHANGELOG
6. Tester le déploiement sur un environnement de staging si possible

## Dépannage

### Erreur : "Nothing to commit"

Le working directory contient des modifications. Committez ou stash avant de lancer la release.

### Erreur : "Authentication failed"

Vérifiez que `GITLAB_SSH_PRIVATE_KEY` est correctement configurée et que la clé publique est ajoutée en Deploy Key avec write access.

### Erreur : "Failed to deploy"

Vérifiez les credentials Nexus (`MAVEN_ENT_USERNAME` et `MAVEN_ENT_PASSWORD`).

### Le tag n'est pas poussé

Vérifiez que `after_script: git push --tags` est présent dans le job `release:prepare`.

## Améliorations futures

- Génération automatique de CHANGELOG
- Création de GitHub/GitLab Releases avec notes
- Notifications Slack/Email lors des releases
- Rollback automatique en cas d'échec
- Signature GPG des artefacts
- Tests d'intégration avant release:perform
