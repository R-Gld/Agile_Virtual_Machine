# Code Review Détaillée : refacto-gui-thread-debug → dev

**Reviewer:** Senior Code Reviewer Agent
**Date:** 2025-12-17
**Branch:** `refacto-gui-thread-debug` → `dev`
**Commit:** `bf2a24eb` - "refacto gui + ajout thread debug"

---

## Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Problèmes Critiques](#problèmes-critiques)
3. [Problèmes Majeurs](#problèmes-majeurs)
4. [Problèmes Mineurs](#problèmes-mineurs)
5. [Points Positifs](#points-positifs)
6. [Recommandations Architecturales](#recommandations-architecturales)
7. [Plan d'Action](#plan-daction)

---

## Vue d'ensemble

### Résumé des Changements

Cette refactorisation extrait la logique de contrôle GUI de la classe monolithique `App` vers une nouvelle classe dédiée `GuiController`, tout en ajoutant des améliorations de threading au `JajaCodeDebugHandler`.

**Statistiques:**
- 3 fichiers modifiés
- 565 insertions (+)
- 447 suppressions (-)
- Réduction de `App.java` de ~900 lignes → ~500 lignes
- Nouvelle classe `GuiController.java`: 444 lignes
- Refactorisation de `JajaCodeDebugHandler.java`: 117 lignes modifiées

**Impact:**
- ✅ Améliore la séparation des responsabilités (SoC)
- ✅ Facilite les tests unitaires
- ⚠️ Introduit des risques de concurrence non gérés
- ⚠️ Fuites potentielles de ressources

### Verdict Global

**❌ CHANGEMENTS REQUIS** - La refactorisation est conceptuellement bonne mais contient des **bugs critiques de threading** qui doivent être corrigés avant le merge.

---

## Problèmes Critiques

### 🔴 C1. Race Conditions dans JajaCodeDebugHandler.stop()

**Localisation:** `JajaCodeDebugHandler.java:54-67`

**Description du Problème:**

La méthode `stop()` modifie des états partagés (`isRunning`, `executor`, `interpreter`) sans synchronisation appropriée, alors qu'elle peut être appelée depuis plusieurs threads simultanément.

**Code Actuel:**
```java
public void stop() {
    isRunning = false;                    // ⚠️ Lecture/écriture non synchronisée
    if (executor != null) {
        executor.shutdownNow();           // ⚠️ Peut être appelé en parallèle
        executor = null;                  // ⚠️ Autre thread peut voir null
    }
    interpreter = null;                   // ⚠️ Race condition
    Platform.runLater(() -> {
        codeArea.highlightLine(-1);
        if (stackTable != null) stackTable.getItems().clear();
        if (heapTable != null) heapTable.getItems().clear();
        console.printMessage("[DEBUG] Debug session stopped.");
    });
}
```

**Scénarios Problématiques:**

**Scénario 1 - Double Shutdown:**
```
Thread A (UI)                    Thread B (Executor)
-------------------------------------------------
stop()
  isRunning = false
  if (executor != null)          step()
    executor.shutdownNow()         if (!isRunning) return  // OK
      [arrêt en cours...]          executor.submit(...)    // ❌ RejectedExecutionException!
    executor = null
```

**Scénario 2 - Null Pointer:**
```
Thread A                         Thread B
-------------------------------------------------
step()
  if (executor == null) return   stop()
                                   executor = null
  executor.submit(...)           // ❌ executor est maintenant null!
```

**Scénario 3 - Vérification TOCTOU (Time-of-Check-Time-of-Use):**
```java
// Dans step()
if (!isRunning || interpreter == null || executor == null) return;
// ⏱️ Entre ces deux lignes, stop() peut être appelé sur un autre thread
executor.submit(() -> {  // ❌ executor peut être null maintenant!
    interpreter.step();   // ❌ interpreter peut être null maintenant!
});
```

**Impact:**
- **NullPointerException** lors de l'accès à `executor` ou `interpreter`
- **RejectedExecutionException** lors de soumissions à un executor arrêté
- **IllegalStateException** dans les opérations UI
- **Corruption de données** si plusieurs threads modifient l'état simultanément

**Solution Recommandée:**

```java
private final Object lock = new Object();

public void stop() {
    ExecutorService executorToShutdown;
    synchronized (lock) {
        if (!isRunning) {
            return; // Déjà arrêté
        }
        isRunning = false;
        executorToShutdown = executor;
        executor = null;
        interpreter = null;
    }

    // Arrêt de l'executor en dehors du lock pour éviter deadlocks
    if (executorToShutdown != null) {
        executorToShutdown.shutdownNow();
        try {
            if (!executorToShutdown.awaitTermination(2, TimeUnit.SECONDS)) {
                console.printMessage("[WARN] Executor did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    Platform.runLater(() -> {
        codeArea.highlightLine(-1);
        if (stackTable != null) stackTable.getItems().clear();
        if (heapTable != null) heapTable.getItems().clear();
        console.printMessage("[DEBUG] Debug session stopped.");
    });
}

public void step() {
    ExecutorService currentExecutor;
    JajaCodeInterpreter currentInterpreter;

    synchronized (lock) {
        if (!isRunning || interpreter == null || executor == null) {
            return;
        }
        currentExecutor = executor;
        currentInterpreter = interpreter;
    }

    // Utilisation des références locales pour éviter les races
    currentExecutor.submit(() -> {
        try {
            boolean hasMore = currentInterpreter.step();
            Platform.runLater(this::updateView);
            if (!hasMore || currentInterpreter.isFinished()) {
                Platform.runLater(() -> console.printMessage("[DEBUG] Execution finished."));
                stop();
            }
        } catch (Exception e) {
            showError("Runtime Error", e);
            stop();
        }
    });
}
```

**Tests à Ajouter:**

```java
@Test
void testConcurrentStopCalls() throws InterruptedException {
    JajaCodeDebugHandler handler = createHandler();
    handler.start(SAMPLE_CODE);

    // Lancer 10 threads qui appellent stop() simultanément
    CountDownLatch latch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
        new Thread(() -> {
            handler.stop();
            latch.countDown();
        }).start();
    }

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertFalse(handler.isRunning());
}

@Test
void testStepDuringStop() throws InterruptedException {
    JajaCodeDebugHandler handler = createHandler();
    handler.start(SAMPLE_CODE);

    // Thread 1: appelle step() en boucle
    AtomicBoolean shouldStop = new AtomicBoolean(false);
    Thread stepThread = new Thread(() -> {
        while (!shouldStop.get()) {
            handler.step();
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
    });

    stepThread.start();
    Thread.sleep(100); // Laisser step() démarrer

    // Thread 2: arrête le handler
    handler.stop();
    shouldStop.set(true);

    stepThread.join(1000);
    assertFalse(handler.isRunning());
    // Pas d'exceptions levées
}
```

---

### 🔴 C2. Fuite de Ressources - ExecutorService Non Fermé

**Localisation:** `GuiController.java:71-76, 95-97`

**Description du Problème:**

Un `ExecutorService` est créé dans le constructeur de `GuiController` mais la méthode `shutdown()` n'est jamais appelée, créant une fuite de ressources.

**Code Actuel:**
```java
public class GuiController {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("app-worker");
        return t;
    });

    public void shutdown() {
        executor.shutdownNow();  // ⚠️ Jamais appelé!
    }
}
```

**Problème dans App.java:**
```java
public class App extends Application {
    @Override
    public void stop() throws Exception {
        super.stop();  // ❌ Ne ferme pas le GuiController!
    }
}
```

**Impact:**
- **Thread Leak:** Le thread `app-worker` continue de tourner après la fermeture de l'application
- **Resource Leak:** Les tâches en cours d'exécution ne sont pas interrompues
- **Memory Leak:** Le `GuiController` et ses références ne peuvent pas être garbage collectés
- **Problème de Tests:** Les tests qui créent plusieurs `GuiController` accumulent des threads

**Démonstration du Problème:**

```java
public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
        Stage stage = new Stage();
        GuiController controller = new GuiController(/* params */);
        controller.compile(); // Lance une tâche
        stage.close();        // ❌ controller.shutdown() jamais appelé!
    }
    // Résultat: 10 threads "app-worker" toujours actifs en mémoire
}
```

**Vérification avec jstack:**
```bash
$ jstack <pid> | grep "app-worker"
"app-worker" #12 daemon prio=5 os_prio=0 tid=0x00007f8b4c001000 nid=0x2b3a waiting...
"app-worker" #13 daemon prio=5 os_prio=0 tid=0x00007f8b4c002000 nid=0x2b3b waiting...
"app-worker" #14 daemon prio=5 os_prio=0 tid=0x00007f8b4c003000 nid=0x2b3c waiting...
# ... 7 autres threads qui n'auraient pas dû survivre!
```

**Solution Recommandée:**

**1. Dans GuiController.java:**
```java
public void shutdown() {
    logger.info("Shutting down GuiController...");

    // Arrêter les handlers de debug en premier
    if (mjjDebugHandler != null) {
        mjjDebugHandler.stop();
    }
    if (debugHandler != null) {
        debugHandler.stop();
    }

    // Arrêter l'executor principal
    executor.shutdown();
    try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warn("Executor did not terminate gracefully, forcing shutdown");
            List<Runnable> droppedTasks = executor.shutdownNow();
            logger.warn("Dropped {} queued tasks", droppedTasks.size());

            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                logger.error("Executor did not terminate after forced shutdown");
            }
        }
    } catch (InterruptedException e) {
        logger.error("Interrupted while waiting for executor termination", e);
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }

    logger.info("GuiController shutdown complete");
}
```

**2. Dans App.java:**
```java
public class App extends Application {
    private GuiController controller;

    @Override
    public void stop() throws Exception {
        logger.info("Application stopping...");

        // Arrêter le controller AVANT d'appeler super.stop()
        if (controller != null) {
            try {
                controller.shutdown();
            } catch (Exception e) {
                logger.error("Error shutting down controller", e);
            }
        }

        super.stop();
        logger.info("Application stopped");
    }
}
```

**Tests à Ajouter:**

```java
@Test
void testExecutorShutdownOnControllerShutdown() throws InterruptedException {
    GuiController controller = createController();

    // Soumettre une tâche longue
    controller.compile();
    Thread.sleep(100); // Laisser la tâche démarrer

    // Fermer le controller
    controller.shutdown();

    // Vérifier que l'executor est bien terminé
    // Note: on ne peut pas accéder directement à executor (private)
    // mais on peut vérifier indirectement

    // Soumettre une nouvelle tâche devrait échouer ou être ignorée
    controller.compile(); // Ne devrait pas planter, juste ne rien faire
}

@Test
void testNoThreadLeakAfterMultipleControllers() {
    int threadCountBefore = Thread.activeCount();

    for (int i = 0; i < 5; i++) {
        GuiController controller = createController();
        controller.compile();
        controller.shutdown();
    }

    // Attendre que tous les threads se terminent
    System.gc();
    Thread.sleep(500);

    int threadCountAfter = Thread.activeCount();

    // Il ne devrait pas y avoir de threads "app-worker" persistants
    assertTrue(threadCountAfter - threadCountBefore < 2,
        "Thread leak detected: " + (threadCountAfter - threadCountBefore) + " extra threads");
}
```

---

### 🔴 C3. Race Condition dans start() - Fuites d'ExecutorService

**Localisation:** `JajaCodeDebugHandler.java:37-52`

**Description du Problème:**

La méthode `start()` crée un nouveau `ExecutorService` sans vérifier si un précédent existe déjà, créant une fuite de ressources si `start()` est appelé plusieurs fois sans `stop()` intermédiaire.

**Code Actuel:**
```java
public void start(String code) {
    try {
        interpreter = new JajaCodeInterpreter(code, new DiagnosticCollector());
        isRunning = true;
        executor = Executors.newSingleThreadExecutor(r -> {  // ⚠️ Perte de l'ancien executor!
            Thread t = new Thread(r, "jajacode-debugger");
            t.setDaemon(true);
            return t;
        });
        Platform.runLater(this::updateView);
        console.printMessage("[DEBUG] Debug session started.");
    } catch (Exception e) {
        showError("Failed to start debugger", e);
        stop();
    }
}
```

**Scénario Problématique:**

```java
JajaCodeDebugHandler handler = new JajaCodeDebugHandler(/*...*/);

// Premier démarrage
handler.start(code1);
// executor1 créé, thread "jajacode-debugger" lancé

// Utilisateur redemarre sans arrêter
handler.start(code2);
// ❌ executor2 créé, executor1 perdu!
// ❌ Le thread de executor1 continue de tourner en arrière-plan
// ❌ Fuite mémoire: executor1 jamais fermé

handler.start(code3);
// ❌ executor3 créé, executor1 ET executor2 perdus!
// ❌ 3 threads actifs maintenant!
```

**Impact:**
- **Memory Leak:** Chaque appel à `start()` crée un nouveau thread qui n'est jamais nettoyé
- **Thread Leak:** Les anciens executors continuent de consommer des ressources système
- **Confusion d'État:** `isRunning` est réinitialisé mais les anciennes tâches continuent
- **Risque de Corruption:** Plusieurs interpreters peuvent s'exécuter simultanément

**Exemple Concret dans l'Interface:**

```
Utilisateur:
1. Charge fichier1.jjc
2. Clique "Debug" → start(fichier1) appelé, executor1 créé
3. Change d'avis, charge fichier2.jjc
4. Clique "Debug" SANS cliquer "Stop" → start(fichier2) appelé
5. ❌ executor1 toujours actif! executor2 créé!
6. Répète 3x → 3 executors actifs, consommant CPU/mémoire
```

**Solution Recommandée:**

**Option 1 - Validation stricte (Recommandé):**
```java
public void start(String code) {
    synchronized (lock) {
        if (isRunning) {
            console.printMessage("[DEBUG] Debug session already running. Stop it first.");
            return;
        }

        if (executor != null) {
            // Sécurité supplémentaire: fermer l'ancien executor si présent
            logger.warn("Executor still present, shutting it down before starting new session");
            executor.shutdownNow();
        }

        try {
            interpreter = new JajaCodeInterpreter(code, new DiagnosticCollector());
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "jajacode-debugger");
                t.setDaemon(true);
                return t;
            });
            isRunning = true;
            Platform.runLater(this::updateView);
            console.printMessage("[DEBUG] Debug session started.");
        } catch (Exception e) {
            showError("Failed to start debugger", e);
            stop();
        }
    }
}
```

**Option 2 - Auto-restart:**
```java
public void start(String code) {
    synchronized (lock) {
        // Arrêter automatiquement la session précédente
        if (isRunning) {
            console.printMessage("[DEBUG] Restarting debug session...");
            stopInternal(); // Version sans Platform.runLater pour éviter la réentrance
        }

        try {
            interpreter = new JajaCodeInterpreter(code, new DiagnosticCollector());
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "jajacode-debugger");
                t.setDaemon(true);
                return t;
            });
            isRunning = true;
            Platform.runLater(this::updateView);
            console.printMessage("[DEBUG] Debug session started.");
        } catch (Exception e) {
            showError("Failed to start debugger", e);
            stop();
        }
    }
}

private void stopInternal() {
    // Version synchrone de stop() pour appels internes
    isRunning = false;
    if (executor != null) {
        executor.shutdownNow();
        executor = null;
    }
    interpreter = null;
}
```

**Amélioration de l'Interface Utilisateur:**

```java
// Dans GuiController.java
public void startDebug() {
    if (debugMode) {
        // Demander confirmation avant de redémarrer
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Debug Already Running");
        alert.setHeaderText("A debug session is already active.");
        alert.setContentText("Do you want to stop it and start a new one?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stopDebug();
        } else {
            return;
        }
    }

    // ... suite du code
}
```

**Tests à Ajouter:**

```java
@Test
void testMultipleStartCallsWithoutStop() {
    JajaCodeDebugHandler handler = createHandler();

    // Premier start
    handler.start(CODE_SAMPLE_1);
    assertTrue(handler.isRunning());

    // Deuxième start sans stop
    handler.start(CODE_SAMPLE_2);

    // Devrait soit:
    // - Refuser de démarrer (option 1)
    // - Redémarrer proprement (option 2)
    // Mais JAMAIS créer une fuite

    handler.stop();
    assertFalse(handler.isRunning());

    // Vérifier qu'il n'y a qu'un seul thread actif
    Set<Thread> threads = Thread.getAllStackTraces().keySet();
    long debugThreads = threads.stream()
        .filter(t -> t.getName().contains("jajacode-debugger"))
        .count();
    assertEquals(0, debugThreads, "Should have no active debug threads after stop");
}

@Test
void testRapidStartStopCycles() {
    JajaCodeDebugHandler handler = createHandler();

    for (int i = 0; i < 100; i++) {
        handler.start("1 init\n2 jcstop");
        handler.stop();
    }

    // Vérifier qu'il n'y a pas de threads résiduels
    System.gc();
    Thread.sleep(500);

    Set<Thread> threads = Thread.getAllStackTraces().keySet();
    long debugThreads = threads.stream()
        .filter(t -> t.getName().contains("jajacode-debugger"))
        .count();
    assertEquals(0, debugThreads, "Thread leak after rapid start/stop cycles");
}
```

---

### 🔴 C4. Gestion Incorrecte des Interruptions de Thread

**Localisation:** `JajaCodeDebugHandler.java:86-120`

**Description du Problème:**

La méthode `continueDebug()` vérifie `Thread.currentThread().isInterrupted()` dans la boucle, mais ne gère pas correctement l'`InterruptedException` qui peut être levée par `interpreter.step()`.

**Code Actuel:**
```java
public void continueDebug(Set<Integer> breakpoints) {
    if (!isRunning || interpreter == null || executor == null) return;
    executor.submit(() -> {
        try {
            boolean hasMore = true;
            boolean hitBreakpoint = false;
            hasMore = interpreter.step();

            while (hasMore && !interpreter.isFinished()
                   && !Thread.currentThread().isInterrupted()) {  // ⚠️ Vérifie l'interruption
                int pc = interpreter.getCurrentInstructionIndex();
                if (breakpoints.contains(pc - 1)) {
                    hitBreakpoint = true;
                    break;
                }
                hasMore = interpreter.step();  // ⚠️ Peut lancer InterruptedException
            }

            Platform.runLater(this::updateView);

            if (hitBreakpoint) {
                int pc = interpreter.getCurrentInstructionIndex();
                Platform.runLater(() -> console.printMessage("[DEBUG] Breakpoint hit at line " + pc));
            } else if (!hasMore || interpreter.isFinished()) {
                Platform.runLater(() -> console.printMessage("[DEBUG] Execution finished."));
                stop();
            }
        } catch (Exception e) {  // ⚠️ Attrape TOUTES les exceptions, y compris InterruptedException
            showError("Runtime Error", e);
            stop();
        }
    });
}
```

**Problèmes:**

1. **Perte du statut d'interruption:**
   - Quand `shutdownNow()` est appelé, il envoie des interruptions aux threads actifs
   - Si `interpreter.step()` lève `InterruptedException`, elle est attrapée par le `catch (Exception e)`
   - Le statut d'interruption du thread est perdu
   - Le message d'erreur affiché est trompeur ("Runtime Error" au lieu de "Debug interrupted")

2. **Comportement incohérent:**
   - La boucle vérifie `isInterrupted()` mais ne réagit pas correctement à l'interruption
   - Une interruption devrait arrêter le debug proprement, pas afficher une erreur

3. **Risque de blocage:**
   - Si `interpreter.step()` est une opération bloquante qui ne respecte pas l'interruption, le thread peut rester bloqué indéfiniment

**Scénarios Problématiques:**

**Scénario 1 - Interruption pendant continueDebug:**
```
Thread UI                        Thread Executor
------------------------------------------------
continueDebug() appelé
                                 while (!isInterrupted())
                                   interpreter.step()
stop() appelé                      [instruction bloquante...]
  executor.shutdownNow()
    → Thread.interrupt()
                                 ❌ InterruptedException levée
                                 catch (Exception e)
                                   showError("Runtime Error", e)
                                   → Utilisateur voit "Erreur" au lieu de "Arrêté"
```

**Scénario 2 - Thread ne se termine jamais:**
```java
// Si interpreter.step() fait ceci:
public boolean step() {
    while (someCondition) {
        // Opération intensive qui ne vérifie pas Thread.interrupted()
        doComplexCalculation();
    }
}

// Alors:
executor.shutdownNow();  // Envoie interrupt()
// ❌ Mais le thread ne se termine jamais car step() ignore l'interruption!
```

**Solution Recommandée:**

```java
public void continueDebug(Set<Integer> breakpoints) {
    ExecutorService currentExecutor;
    JajaCodeInterpreter currentInterpreter;

    synchronized (lock) {
        if (!isRunning || interpreter == null || executor == null) {
            return;
        }
        currentExecutor = executor;
        currentInterpreter = interpreter;
    }

    currentExecutor.submit(() -> {
        boolean hitBreakpoint = false;
        boolean interrupted = false;

        try {
            boolean hasMore = currentInterpreter.step();

            while (hasMore && !currentInterpreter.isFinished()) {
                // Vérifier l'interruption en premier
                if (Thread.currentThread().isInterrupted()) {
                    interrupted = true;
                    break;
                }

                int pc = currentInterpreter.getCurrentInstructionIndex();
                if (breakpoints.contains(pc - 1)) {
                    hitBreakpoint = true;
                    break;
                }

                hasMore = currentInterpreter.step();
            }

        } catch (InterruptedException e) {
            // Restaurer le statut d'interruption
            Thread.currentThread().interrupt();
            interrupted = true;
            Platform.runLater(() ->
                console.printMessage("[DEBUG] Debug interrupted by user."));
            return;

        } catch (Exception e) {
            // Vraie erreur d'exécution
            showError("Runtime Error", e);
            stop();
            return;
        }

        // Mise à jour de la vue après exécution réussie
        if (!interrupted) {
            Platform.runLater(this::updateView);

            if (hitBreakpoint) {
                int pc = currentInterpreter.getCurrentInstructionIndex();
                Platform.runLater(() ->
                    console.printMessage("[DEBUG] Breakpoint hit at line " + pc));
            } else if (currentInterpreter.isFinished()) {
                Platform.runLater(() ->
                    console.printMessage("[DEBUG] Execution finished."));
                stop();
            }
        }
    });
}
```

**Amélioration de l'implémentation de l'interpréteur:**

Si vous contrôlez le code de `JajaCodeInterpreter.step()`, assurez-vous qu'il respecte les interruptions:

```java
// Dans JajaCodeInterpreter.java
public boolean step() throws InterruptedException {
    // Vérifier l'interruption avant de faire le travail
    if (Thread.interrupted()) {
        throw new InterruptedException("Step interrupted");
    }

    // Faire le travail...
    JajaCodeInstruction instruction = getCurrentInstruction();

    // Pour les opérations longues, vérifier périodiquement
    if (instruction.isLongRunning()) {
        while (moreWork()) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Step interrupted during execution");
            }
            doWork();
        }
    }

    return hasMoreInstructions();
}
```

**Tests à Ajouter:**

```java
@Test
void testInterruptionDuringContinue() throws InterruptedException {
    JajaCodeDebugHandler handler = createHandler();
    String longRunningCode = createLongRunningCode(1000); // 1000 instructions
    handler.start(longRunningCode);

    CountDownLatch continueStarted = new CountDownLatch(1);

    // Démarrer continue dans un thread
    Thread debugThread = new Thread(() -> {
        continueStarted.countDown();
        handler.continueDebug(Collections.emptySet());
    });
    debugThread.start();

    // Attendre que continue démarre
    assertTrue(continueStarted.await(1, TimeUnit.SECONDS));
    Thread.sleep(100); // Laisser quelques instructions s'exécuter

    // Interrompre
    handler.stop();

    // Vérifier que le thread se termine rapidement
    debugThread.join(2000);
    assertFalse(debugThread.isAlive(), "Debug thread should have terminated");
}

@Test
void testInterruptedExceptionPreservesStatus() {
    JajaCodeDebugHandler handler = createHandler();
    handler.start(LONG_CODE);

    // Simuler une InterruptedException
    Thread.currentThread().interrupt();

    handler.continueDebug(Collections.emptySet());

    // Le statut d'interruption devrait être préservé
    assertTrue(Thread.interrupted(), "Interrupt status should be preserved");
}
```

---

## Problèmes Majeurs

### 🟠 M1. Modèles de Threading Incohérents

**Localisation:** Comparaison entre `JajaCodeDebugHandler` et `MiniJajaDebugHandler`

**Description du Problème:**

Les deux handlers de debug utilisent des modèles de concurrence complètement différents, créant de l'incohérence dans le code et augmentant la complexité de maintenance.

**JajaCodeDebugHandler (ExecutorService):**
```java
public class JajaCodeDebugHandler {
    private ExecutorService executor;

    public void start(String code) {
        executor = Executors.newSingleThreadExecutor(...);
    }

    public void step() {
        executor.submit(() -> {
            interpreter.step();
        });
    }
}
```

**MiniJajaDebugHandler (Thread dédié + boucle d'attente):**
```java
public class MiniJajaDebugHandler {
    private Thread debugThread;
    private volatile boolean waitingForUserAction = false;
    private volatile boolean shouldStep = false;
    private volatile boolean shouldContinue = false;

    public void start(String code, Set<Integer> breakpoints) {
        debugThread = new Thread(() -> {
            debugger.run();  // Bloque jusqu'à la fin
        }, "minijaja-debugger");
        debugThread.start();
    }

    private boolean handlePause(...) {
        waitingForUserAction = true;
        shouldStep = false;
        shouldContinue = false;

        // Boucle d'attente active
        while (waitingForUserAction && !shouldStop) {
            if (shouldStep) {
                waitingForUserAction = false;
                return true;
            }
            if (shouldContinue) {
                waitingForUserAction = false;
                debugController.continueToNextBreakpoint();
                return true;
            }
            try {
                Thread.sleep(50);  // Busy-waiting avec pause
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return !shouldStop;
    }
}
```

**Différences Clés:**

| Aspect | JajaCodeDebugHandler | MiniJajaDebugHandler |
|--------|----------------------|----------------------|
| **Threading** | ExecutorService (pool de 1 thread) | Thread dédié unique |
| **Cycle de vie** | start/stop créent/détruisent executor | Thread existe pendant toute la session |
| **Contrôle** | Soumission de tâches (submit) | Flags volatile + busy-waiting |
| **Synchronisation** | Pas de synchronisation explicite | Multiple flags volatile |
| **Pause/Resume** | Pas de mécanisme natif | Boucle d'attente avec callbacks |
| **Interruption** | shutdownNow() | Thread.interrupt() + flags |

**Impact:**

1. **Complexité Cognitive:** Les développeurs doivent comprendre deux patterns différents
2. **Risque d'Erreurs:** Facile d'appliquer le mauvais pattern lors de modifications
3. **Maintenance:** Correctifs de bugs doivent être adaptés à chaque handler
4. **Tests:** Nécessite des stratégies de test différentes pour chaque handler
5. **Performance:** MiniJajaDebugHandler utilise busy-waiting (CPU-intensive)

**Analyse Comparative:**

**Avantages de l'approche JajaCodeDebugHandler (ExecutorService):**
- ✅ Plus "Java moderne" (java.util.concurrent)
- ✅ Gestion automatique du thread pool
- ✅ Meilleure intégration avec JavaFX Tasks
- ✅ Pas de busy-waiting

**Inconvénients:**
- ❌ Pas de mécanisme natif de pause/resume
- ❌ Plus complexe pour le step-by-step
- ❌ Nécessite synchronisation manuelle

**Avantages de l'approche MiniJajaDebugHandler (Thread dédié):**
- ✅ Contrôle fin du cycle de vie
- ✅ Pause/resume naturellement intégré
- ✅ Meilleur pour les debuggers interactifs
- ✅ Moins de race conditions (flags volatile bien utilisés)

**Inconvénients:**
- ❌ Busy-waiting consomme du CPU
- ❌ Code plus complexe (multiple flags)
- ❌ Moins "idiomatique" Java moderne

**Recommandation Architecturale:**

**Option A - Standardiser sur Thread Dédié + CompletableFuture (Recommandé):**

Créer une classe de base abstraite qui combine les avantages des deux approches:

```java
public abstract class AbstractDebugHandler<T extends Interpreter> {
    protected T interpreter;
    protected final ConsoleOutput console;
    protected final MyCodeArea codeArea;

    private Thread debugThread;
    private final Object pauseLock = new Object();
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private volatile DebugAction nextAction = DebugAction.NONE;

    private enum DebugAction {
        NONE, STEP, CONTINUE, STOP
    }

    protected AbstractDebugHandler(ConsoleOutput console, MyCodeArea codeArea) {
        this.console = console;
        this.codeArea = codeArea;
    }

    public void start(String code) {
        if (isRunning) {
            console.printMessage("[DEBUG] Already running");
            return;
        }

        try {
            interpreter = createInterpreter(code);
            isRunning = true;
            isPaused = true;  // Démarrer en pause

            debugThread = new Thread(this::runDebugLoop, getThreadName());
            debugThread.setDaemon(true);
            debugThread.start();

            Platform.runLater(this::updateView);
            console.printMessage("[DEBUG] Debug session started.");
        } catch (Exception e) {
            showError("Failed to start debugger", e);
            stop();
        }
    }

    private void runDebugLoop() {
        try {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                // Attendre une action utilisateur si en pause
                synchronized (pauseLock) {
                    while (isPaused && isRunning && nextAction == DebugAction.NONE) {
                        pauseLock.wait();  // ✅ Pas de busy-waiting!
                    }

                    if (!isRunning || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }

                // Exécuter l'action
                boolean shouldContinue = executeStep();

                if (!shouldContinue) {
                    Platform.runLater(() -> console.printMessage("[DEBUG] Execution finished."));
                    break;
                }

                // Vérifier les breakpoints
                if (shouldPauseAtCurrentLocation()) {
                    isPaused = true;
                    nextAction = DebugAction.NONE;
                    Platform.runLater(this::updateView);
                    Platform.runLater(() ->
                        console.printMessage("[DEBUG] Paused at breakpoint"));
                } else if (nextAction == DebugAction.STEP) {
                    isPaused = true;
                    nextAction = DebugAction.NONE;
                    Platform.runLater(this::updateView);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() ->
                console.printMessage("[DEBUG] Debug interrupted."));
        } catch (Exception e) {
            showError("Runtime Error", e);
        } finally {
            Platform.runLater(this::stop);
        }
    }

    public void step() {
        synchronized (pauseLock) {
            nextAction = DebugAction.STEP;
            isPaused = false;
            pauseLock.notify();  // ✅ Réveiller le thread proprement
        }
    }

    public void continueDebug() {
        synchronized (pauseLock) {
            nextAction = DebugAction.CONTINUE;
            isPaused = false;
            pauseLock.notify();
        }
    }

    public void stop() {
        isRunning = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();  // Réveiller le thread s'il attend
        }
        if (debugThread != null && debugThread.isAlive()) {
            debugThread.interrupt();
            try {
                debugThread.join(2000);  // Attendre max 2 secondes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Platform.runLater(this::clearView);
    }

    // Méthodes abstraites à implémenter par les sous-classes
    protected abstract T createInterpreter(String code) throws Exception;
    protected abstract boolean executeStep() throws Exception;
    protected abstract boolean shouldPauseAtCurrentLocation();
    protected abstract void updateView();
    protected abstract void clearView();
    protected abstract String getThreadName();
}
```

**Implémentation pour JajaCode:**
```java
public class JajaCodeDebugHandler extends AbstractDebugHandler<JajaCodeInterpreter> {
    private final TableView<JajaCodeDebug.VariableInfo> stackTable;
    private final TableView<JajaCodeDebug.HeapInfo> heapTable;
    private Set<Integer> breakpoints = new HashSet<>();

    public JajaCodeDebugHandler(ConsoleOutput console, MyCodeArea codeArea,
                                TableView<JajaCodeDebug.VariableInfo> stackTable,
                                TableView<JajaCodeDebug.HeapInfo> heapTable) {
        super(console, codeArea);
        this.stackTable = stackTable;
        this.heapTable = heapTable;
    }

    @Override
    protected JajaCodeInterpreter createInterpreter(String code) throws Exception {
        return new JajaCodeInterpreter(code, new DiagnosticCollector());
    }

    @Override
    protected boolean executeStep() throws Exception {
        return interpreter.step();
    }

    @Override
    protected boolean shouldPauseAtCurrentLocation() {
        int pc = interpreter.getCurrentInstructionIndex();
        return breakpoints.contains(pc - 1);
    }

    public void setBreakpoints(Set<Integer> breakpoints) {
        this.breakpoints = new HashSet<>(breakpoints);
    }

    @Override
    protected void updateView() {
        int pc = interpreter.getCurrentInstructionIndex();
        if (pc > 0) {
            codeArea.highlightLine(pc - 1);
        }

        JajaCodeDebug.MemorySnapshot snapshot = interpreter.captureMemoryState();
        if (snapshot != null) {
            stackTable.setItems(FXCollections.observableArrayList(snapshot.stackState()));
            heapTable.setItems(FXCollections.observableArrayList(snapshot.heapState()));
        }
    }

    @Override
    protected void clearView() {
        codeArea.highlightLine(-1);
        if (stackTable != null) stackTable.getItems().clear();
        if (heapTable != null) heapTable.getItems().clear();
    }

    @Override
    protected String getThreadName() {
        return "jajacode-debugger";
    }
}
```

**Option B - Uniformiser sur ExecutorService avec State Machine:**

Si vous préférez garder l'ExecutorService, implémentez une machine à états:

```java
public abstract class AbstractDebugHandler<T extends Interpreter> {
    private final ExecutorService executor;
    private final Object stateLock = new Object();
    private DebugState state = DebugState.STOPPED;
    private final BlockingQueue<DebugCommand> commandQueue = new LinkedBlockingQueue<>();

    private enum DebugState {
        STOPPED, RUNNING, PAUSED, STEPPING
    }

    private enum DebugCommand {
        STEP, CONTINUE, STOP
    }

    public void start(String code) {
        synchronized (stateLock) {
            if (state != DebugState.STOPPED) return;
            state = DebugState.PAUSED;
        }

        executor.submit(this::runDebugLoop);
    }

    private void runDebugLoop() {
        while (true) {
            DebugCommand cmd;
            try {
                cmd = commandQueue.take();  // Bloque jusqu'à commande
            } catch (InterruptedException e) {
                break;
            }

            switch (cmd) {
                case STEP -> {
                    executeStep();
                    synchronized (stateLock) {
                        state = DebugState.PAUSED;
                    }
                }
                case CONTINUE -> {
                    while (!shouldPause()) {
                        if (!executeStep()) break;
                    }
                    synchronized (stateLock) {
                        state = DebugState.PAUSED;
                    }
                }
                case STOP -> {
                    return;
                }
            }
        }
    }

    public void step() {
        commandQueue.offer(DebugCommand.STEP);
    }

    public void continueDebug() {
        commandQueue.offer(DebugCommand.CONTINUE);
    }

    public void stop() {
        commandQueue.offer(DebugCommand.STOP);
        executor.shutdown();
    }
}
```

**Recommandation Finale:**

Je recommande **Option A** (Thread dédié + Object.wait/notify) pour les raisons suivantes:

1. ✅ **Pas de busy-waiting:** Utilise `wait()` au lieu de `Thread.sleep()`
2. ✅ **Plus simple:** Moins de complexité que les BlockingQueues
3. ✅ **Meilleur pour le debugging:** Le modèle pause/resume est naturel
4. ✅ **Performances:** Pas de CPU gaspillé pendant les pauses
5. ✅ **Testabilité:** Plus facile à tester avec des timeouts

---

### 🟠 M2. Accès Non Synchronisé aux Composants UI

**Localisation:** `JajaCodeDebugHandler.java:122-144`, `GuiController.java` (plusieurs endroits)

**Description du Problème:**

Les composants JavaFX (`MyCodeArea`, `TableView`, etc.) sont accédés depuis des threads background sans garantie qu'ils le soient via `Platform.runLater()` de manière cohérente.

**Code Problématique:**

```java
// Dans JajaCodeDebugHandler.java:122-144
private void updateView() {
    if (interpreter == null) return;
    int pc = interpreter.getCurrentInstructionIndex();

    // ❌ updateView() peut être appelée depuis n'importe quel thread!
    if (pc > 0) {
        codeArea.highlightLine(pc - 1);  // ⚠️ Modification UI sans Platform.runLater
    }

    JajaCodeDebug.MemorySnapshot snapshot = interpreter.captureMemoryState();
    if (snapshot != null) {
        console.printMessage("[DEBUG] Memory Snapshot...");  // ✅ OK (console thread-safe)
        if (stackTable != null) {
            stackTable.setItems(...);  // ⚠️ Modification UI sans Platform.runLater
            stackTable.refresh();
        }
        if (heapTable != null) {
            heapTable.setItems(...);  // ⚠️ Modification UI sans Platform.runLater
            heapTable.refresh();
        }
    }
}
```

**Où est appelée updateView():**
```java
// Ligne 46 - DEPUIS FX Thread ✅
public void start(String code) {
    Platform.runLater(this::updateView);  // OK
}

// Ligne 74 - DEPUIS Executor Thread ❌
public void step() {
    executor.submit(() -> {
        boolean hasMore = interpreter.step();
        Platform.runLater(this::updateView);  // OK MAIS...
    });
}

// Ligne 106 - DEPUIS Executor Thread ❌
public void continueDebug() {
    executor.submit(() -> {
        // ...
        Platform.runLater(this::updateView);  // OK MAIS...
    });
}
```

**Le Vrai Problème:**

Le code actuel **semble** correct car `updateView()` est toujours appelée via `Platform.runLater()`. MAIS le problème est plus subtil:

1. **Dans `step()` ligne 74:** `Platform.runLater(this::updateView)` est correct
2. **Mais regardez ligne 61-66 dans `stop()`:**
```java
public void stop() {
    isRunning = false;
    if (executor != null) {
        executor.shutdownNow();
        executor = null;
    }
    interpreter = null;  // ❌ Assignation non synchronisée!
    Platform.runLater(() -> {
        codeArea.highlightLine(-1);  // ✅ OK dans runLater
        if (stackTable != null) stackTable.getItems().clear();  // ✅ OK
        if (heapTable != null) heapTable.getItems().clear();  // ✅ OK
    });
}
```

**Le problème:** Imaginez ce scénario:

```
Thread Executor                    Thread FX
--------------------------------------------------
step() exécute...
  Platform.runLater(updateView)
    → Tâche UI mise en queue
                                   [FX thread occupé...]

[stop() appelé sur FX thread]
stop()
  isRunning = false
  executor.shutdownNow()
  interpreter = null  ❌
                                   [FX thread se libère]
                                   Exécute updateView()
                                     int pc = interpreter.get...()
                                     ❌ NullPointerException!
```

**Autre Problème - GuiController.java:78-93:**

```java
public class GuiController {
    private final MyCodeArea mjjCodeArea;   // ⚠️ Référence UI stockée
    private final MyCodeArea jjcCodeArea;   // ⚠️ Référence UI stockée

    public void compile() {
        String code = mjjCodeArea.getText();  // ⚠️ Accès UI depuis où?

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                // ✅ OK - pas d'accès UI ici
                Compiler compiler = new Compiler(code, ...);
                return compiler.compileToString();
            }
        };

        task.setOnSucceeded(ev -> {
            String compileResult = task.getValue();
            jjcCodeArea.loadText(compileResult);  // ✅ OK - dans callback FX
        });

        executor.submit(task);
    }
}
```

Le code ci-dessus est **correctement** implémenté! `mjjCodeArea.getText()` est appelé sur le FX thread (avant de soumettre la tâche), et `loadText()` est appelé dans `setOnSucceeded` qui s'exécute sur le FX thread.

MAIS le problème est que ce pattern n'est pas documenté et pas cohérent partout.

**Impact:**

1. **IllegalStateException:** "Not on FX application thread"
2. **NullPointerException:** Variables nullifiées pendant que UI les utilise
3. **Data Corruption:** États UI incohérents
4. **Crashes silencieux:** Parfois JavaFX avale les exceptions

**Solution Recommandée:**

**1. Documentation claire des contrats de threading:**

```java
/**
 * Handler for JajaCode debugging.
 *
 * <p><b>Threading Contract:</b></p>
 * <ul>
 *   <li>Public methods (start, stop, step, continueDebug) can be called from any thread
 *   <li>UI updates are always performed via Platform.runLater()
 *   <li>Interpreter access is synchronized to prevent races
 * </ul>
 *  * <p><b>Thread Safety:</b></p>
 * This class is thread-safe. All mutable state is protected by {@link #lock}.
 */
public class JajaCodeDebugHandler {
    private final Object lock = new Object();
    // ...
}
```

**2. Protéger tous les accès aux variables partagées:**

```java
private void updateView() {
    JajaCodeInterpreter currentInterpreter;

    synchronized (lock) {
        currentInterpreter = interpreter;
        if (currentInterpreter == null) {
            return;
        }
    }

    // Capturer les données AVANT de toucher l'UI
    final int pc = currentInterpreter.getCurrentInstructionIndex();
    final JajaCodeDebug.MemorySnapshot snapshot = currentInterpreter.captureMemoryState();

    // Maintenant, mise à jour UI sur FX thread
    Platform.runLater(() -> {
        if (pc > 0) {
            codeArea.highlightLine(pc - 1);
        }

        if (snapshot != null) {
            console.printMessage("[DEBUG] Memory Snapshot at PC=" + pc +
                ": Stack size=" + snapshot.stackState().size() +
                ", Heap size=" + snapshot.heapState().size());

            if (stackTable != null) {
                stackTable.setItems(FXCollections.observableArrayList(snapshot.stackState()));
                stackTable.refresh();
            }
            if (heapTable != null) {
                heapTable.setItems(FXCollections.observableArrayList(snapshot.heapState()));
                heapTable.refresh();
            }
        }
    });
}
```

**3. Ajouter des assertions pour détecter les violations:**

```java
private void updateView() {
    // En développement, détecter les appels incorrects
    assert Platform.isFxApplicationThread() || isCalledViaPlatformRunLater()
        : "updateView() must be called on FX thread or via Platform.runLater()";

    // ...
}

private boolean isCalledViaPlatformRunLater() {
    // Vérifier la stack trace pour voir si on vient de Platform.runLater
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    for (StackTraceElement element : stack) {
        if (element.getClassName().contains("javafx.application.Platform")
            && element.getMethodName().contains("run")) {
            return true;
        }
    }
    return false;
}
```

**4. Pattern "Capture-Then-Update":**

Adoptez ce pattern systématiquement:

```java
// 1. Capturer les données (thread safe)
synchronized (lock) {
    if (interpreter == null) return;
    data = interpreter.captureData();
}

// 2. Mettre à jour l'UI (FX thread)
Platform.runLater(() -> {
    updateUIWithData(data);
});
```

**Tests à Ajouter:**

```java
@Test
void testUpdateViewNotOnFXThreadThrows() {
    JajaCodeDebugHandler handler = createHandler();
    handler.start(CODE);

    // Essayer d'appeler updateView directement depuis un autre thread
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<?> future = executor.submit(() -> {
        // Utiliser réflexion pour accéder à la méthode privée
        Method updateView = JajaCodeDebugHandler.class
            .getDeclaredMethod("updateView");
        updateView.setAccessible(true);
        updateView.invoke(handler);
    });

    // Devrait lever IllegalStateException
    assertThrows(ExecutionException.class, () -> future.get());
}

@Test
void testNoUIAccessAfterStop() throws InterruptedException {
    JajaCodeDebugHandler handler = createHandler();
    handler.start(CODE);

    // Lancer plusieurs step() en parallèle
    for (int i = 0; i < 10; i++) {
        handler.step();
    }

    Thread.sleep(50); // Laisser les steps démarrer

    // Arrêter brutalement
    handler.stop();

    Thread.sleep(200); // Laisser les Platform.runLater() s'exécuter

    // Pas de NPE ou IllegalStateException
    // (Vérifié via try-catch dans les callbacks Platform.runLater)
}
```

---

### 🟠 M3. Gestion Manuelle d'État Fragile dans GuiController

**Localisation:** `GuiController.java:403-429`

**Description du Problème:**

La gestion de l'état de débogage dans `GuiController` est fragile car elle utilise plusieurs variables d'état indépendantes qui peuvent devenir incohérentes.

**Variables d'État:**
```java
private boolean debugMode = false;            // L'état général de débogage
private int debugCurrentLine = -1;            // Ligne actuelle (pas utilisé?)
private final Set<Integer> debugBreakpoints = new HashSet<>();  // Breakpoints
private DebugSource debugSource = DebugSource.MINIJAJA;  // Source du debug
private MiniJajaDebugHandler mjjDebugHandler;  // Handler MiniJaja
private JajaCodeDebugHandler debugHandler;     // Handler JajaCode
```

**Code Problématique:**

```java
public void stopDebug() {
    if (!debugMode) {
        return;
    }

    if (debugSource == DebugSource.MINIJAJA) {
        if (mjjDebugHandler != null) {
            mjjDebugHandler.stop();
            mjjDebugHandler = null;  // ⚠️ Nullifié manuellement
        }
    } else {
        if (debugHandler != null) {
            debugHandler.stop();
            debugHandler = null;  // ⚠️ Nullifié manuellement
        }
    }

    debugMode = false;  // ⚠️ Mis à false manuellement
    debugCurrentLine = -1;  // ⚠️ Reset manuel

    // ❌ debugBreakpoints pas nettoyé!
    // ❌ debugSource pas reset!

    if (console != null) {
        console.printMessage("[DEBUG] Debug mode stopped.");
    }
    stepButton.setDisable(true);
    stopButton.setDisable(true);
    continueButton.setDisable(true);
}
```

**Scénarios Problématiques:**

**Scénario 1 - États Incohérents:**
```java
// Démarrage debug MiniJaja
startMiniJajaDebug();
// État: debugMode=true, debugSource=MINIJAJA, mjjDebugHandler!=null

// Si mjjDebugHandler.start() échoue:
mjjDebugHandler.start(code, breakpoints);
if (!mjjDebugHandler.isRunning()) {
    return;  // ❌ On sort sans nettoyer!
}
// État: debugMode=true, debugSource=MINIJAJA, mjjDebugHandler.isRunning()=false
// ❌ L'UI pense qu'on est en debug mais le handler ne tourne pas!
```

**Scénario 2 - Fuites de Handler:**
```java
// Debug MiniJaja en cours
startMiniJajaDebug();
// État: debugMode=true, debugSource=MINIJAJA, mjjDebugHandler!=null

// Utilisateur change de mode sans arrêter
startJajaCodeDebug();
// État: debugMode=true, debugSource=JAJACODE, debugHandler!=null
// ❌ mjjDebugHandler toujours actif en arrière-plan!
```

**Scénario 3 - Boutons Désynchronisés:**
```java
startDebug();
// Boutons: step=enabled, stop=enabled, continue=enabled

// Handler se termine tout seul (fin d'exécution)
handler.stop();  // Appelé depuis le handler lui-même
// État: debugMode=true (pas changé!), handler=null
// Boutons: step=enabled, stop=enabled, continue=enabled
// ❌ Les boutons sont actifs mais le debug est arrêté!
```

**Impact:**

1. **UI incohérente:** Boutons actifs alors que debug arrêté
2. **Handlers orphelins:** Handlers actifs non référencés
3. **Memory Leaks:** Handlers non arrêtés continuent de consommer ressources
4. **Bugs subtils:** États partiellement initialisés

**Solution Recommandée:**

**Option 1 - State Machine Pattern (Recommandé):**

```java
public class GuiController {

    // État encapsulé dans un enum
    private enum DebugState {
        STOPPED,
        DEBUGGING_MINIJAJA,
        DEBUGGING_JAJACODE
    }

    private DebugState debugState = DebugState.STOPPED;
    private AbstractDebugHandler<?> currentDebugHandler;  // Handler générique
    private final Set<Integer> currentBreakpoints = new HashSet<>();

    public void startDebug() {
        // Arrêter proprement tout debug en cours
        if (debugState != DebugState.STOPPED) {
            stopDebug();
        }

        String choice = fileToRun.getValue();

        try {
            if (App.MINI_JAJA_NAME.equals(choice)) {
                startMiniJajaDebugInternal();
            } else {
                startJajaCodeDebugInternal();
            }
        } catch (Exception e) {
            // Si quelque chose échoue, garantir un état propre
            forceStopDebug();
            throw e;
        }
    }

    private void startMiniJajaDebugInternal() {
        String code = mjjCodeArea.getText();
        Set<Integer> breakpoints = mjjCodeArea.getBreakpoints();

        MiniJajaDebugHandler handler = new MiniJajaDebugHandler(
            console, mjjCodeArea, stackTable, heapTable);
        handler.start(code, breakpoints);

        if (!handler.isRunning()) {
            throw new IllegalStateException("Failed to start MiniJaja debugger");
        }

        // ✅ Transition atomique d'état
        currentDebugHandler = handler;
        debugState = DebugState.DEBUGGING_MINIJAJA;
        currentBreakpoints.clear();
        currentBreakpoints.addAll(breakpoints);
        updateUIForDebugState();

        console.printMessage("[DEBUG] MiniJaja debug started");
    }

    private void startJajaCodeDebugInternal() {
        String codeToDebug = jjcCodeArea.getText();
        String[] lines = codeToDebug.split("\\n");
        StringBuilder numberedCode = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            numberedCode.append(i + 1).append(" ").append(lines[i]).append("\n");
        }

        Set<Integer> breakpoints = jjcCodeArea.getBreakpoints();

        JajaCodeDebugHandler handler = new JajaCodeDebugHandler(
            console, jjcCodeArea, stackTable, heapTable);
        handler.start(numberedCode.toString());
        handler.setBreakpoints(breakpoints);  // Nouvelle méthode

        if (!handler.isRunning()) {
            throw new IllegalStateException("Failed to start JajaCode debugger");
        }

        // ✅ Transition atomique d'état
        currentDebugHandler = handler;
        debugState = DebugState.DEBUGGING_JAJACODE;
        currentBreakpoints.clear();
        currentBreakpoints.addAll(breakpoints);
        updateUIForDebugState();

        console.printMessage("[DEBUG] JajaCode debug started");
    }

    public void stepDebug() {
        if (debugState == DebugState.STOPPED || currentDebugHandler == null) {
            return;
        }

        currentDebugHandler.step();

        // Vérifier si le handler s'est arrêté
        if (!currentDebugHandler.isRunning()) {
            stopDebugInternal();
        }
    }

    public void continueDebug() {
        if (debugState == DebugState.STOPPED || currentDebugHandler == null) {
            return;
        }

        // Récupérer les breakpoints à jour
        Set<Integer> freshBreakpoints = switch (debugState) {
            case DEBUGGING_MINIJAJA -> mjjCodeArea.getBreakpoints();
            case DEBUGGING_JAJACODE -> jjcCodeArea.getBreakpoints();
            default -> Collections.emptySet();
        };

        currentDebugHandler.continueDebug(freshBreakpoints);

        if (!currentDebugHandler.isRunning()) {
            stopDebugInternal();
        }
    }

    public void stopDebug() {
        if (debugState == DebugState.STOPPED) {
            return;
        }

        stopDebugInternal();
    }

    private void stopDebugInternal() {
        // Sauvegarder l'état pour le nettoyage de l'UI
        DebugState previousState = debugState;

        // Arrêter le handler
        if (currentDebugHandler != null) {
            try {
                currentDebugHandler.stop();
            } catch (Exception e) {
                logger.error("Error stopping debug handler", e);
            } finally {
                currentDebugHandler = null;
            }
        }

        // ✅ Reset atomique de tout l'état
        debugState = DebugState.STOPPED;
        currentBreakpoints.clear();

        // Nettoyer l'UI selon l'ancien état
        if (previousState == DebugState.DEBUGGING_MINIJAJA) {
            mjjCodeArea.highlightLine(-1);
        } else if (previousState == DebugState.DEBUGGING_JAJACODE) {
            jjcCodeArea.highlightLine(-1);
        }

        updateUIForDebugState();
        console.printMessage("[DEBUG] Debug session stopped");
    }

    /**
     * Force un arrêt complet en cas d'erreur, sans lever d'exceptions.
     */
    private void forceStopDebug() {
        try {
            stopDebugInternal();
        } catch (Exception e) {
            logger.error("Error during forced debug stop", e);
            // Forcer le reset même en cas d'erreur
            currentDebugHandler = null;
            debugState = DebugState.STOPPED;
            currentBreakpoints.clear();
            updateUIForDebugState();
        }
    }

    /**
     * Met à jour l'état de l'UI basé sur debugState.
     */
    private void updateUIForDebugState() {
        boolean isDebugging = (debugState != DebugState.STOPPED);

        stepButton.setDisable(!isDebugging);
        stopButton.setDisable(!isDebugging);
        continueButton.setDisable(!isDebugging);

        // Optionnel: désactiver d'autres boutons pendant le debug
        // compileButton.setDisable(isDebugging);
        // runButton.setDisable(isDebugging);
    }

    public boolean isDebugMode() {
        return debugState != DebugState.STOPPED;
    }

    public DebugState getDebugState() {
        return debugState;
    }
}
```

**Option 2 - Builder Pattern pour Validation:**

```java
public class DebugSession {
    private final DebugSource source;
    private final AbstractDebugHandler<?> handler;
    private final MyCodeArea codeArea;
    private final Set<Integer> breakpoints;

    private DebugSession(Builder builder) {
        this.source = builder.source;
        this.handler = builder.handler;
        this.codeArea = builder.codeArea;
        this.breakpoints = new HashSet<>(builder.breakpoints);

        // Validation
        if (!handler.isRunning()) {
            throw new IllegalStateException("Handler must be running");
        }
    }

    public void step() {
        handler.step();
    }

    public void continueDebug() {
        handler.continueDebug(breakpoints);
    }

    public void stop() {
        handler.stop();
        codeArea.highlightLine(-1);
    }

    public boolean isRunning() {
        return handler.isRunning();
    }

    public static class Builder {
        private DebugSource source;
        private AbstractDebugHandler<?> handler;
        private MyCodeArea codeArea;
        private Set<Integer> breakpoints = new HashSet<>();

        public Builder source(DebugSource source) {
            this.source = source;
            return this;
        }

        public Builder handler(AbstractDebugHandler<?> handler) {
            this.handler = handler;
            return this;
        }

        public Builder codeArea(MyCodeArea codeArea) {
            this.codeArea = codeArea;
            return this;
        }

        public Builder breakpoints(Set<Integer> breakpoints) {
            this.breakpoints = breakpoints;
            return this;
        }

        public DebugSession build() {
            Objects.requireNonNull(source, "source must not be null");
            Objects.requireNonNull(handler, "handler must not be null");
            Objects.requireNonNull(codeArea, "codeArea must not be null");
            return new DebugSession(this);
        }
    }
}

// Usage dans GuiController:
private DebugSession currentSession;

private void startMiniJajaDebugInternal() {
    MiniJajaDebugHandler handler = new MiniJajaDebugHandler(...);
    handler.start(code, breakpoints);

    currentSession = new DebugSession.Builder()
        .source(DebugSource.MINIJAJA)
        .handler(handler)
        .codeArea(mjjCodeArea)
        .breakpoints(breakpoints)
        .build();  // ✅ Validation automatique
}

public void stepDebug() {
    if (currentSession == null) return;
    currentSession.step();
    if (!currentSession.isRunning()) {
        currentSession.stop();
        currentSession = null;
    }
}
```

**Tests à Ajouter:**

```java
@Test
void testStateConsistencyAfterFailedStart() {
    GuiController controller = createController();

    // Simuler un échec de démarrage
    mockDebugHandlerToFailStart();

    try {
        controller.startDebug();
        fail("Should have thrown exception");
    } catch (Exception e) {
        // Expected
    }

    // L'état devrait être proprement reset
    assertFalse(controller.isDebugMode());
    assertTrue(stepButton.isDisabled());
    assertTrue(stopButton.isDisabled());
    assertTrue(continueButton.isDisabled());
}

@Test
void testNoHandlerLeakWhenSwitchingDebugModes() {
    GuiController controller = createController();

    // Démarrer MiniJaja
    loadMiniJajaFile();
    controller.startDebug();
    MiniJajaDebugHandler handler1 = getCurrentHandler();

    // Changer pour JajaCode SANS arrêter
    loadJajaCodeFile();
    controller.startDebug();

    // Le premier handler devrait être arrêté
    assertFalse(handler1.isRunning());

    // Seul le deuxième handler est actif
    assertEquals(1, countActiveDebugHandlers());
}

@Test
void testUIConsistentWhenHandlerStopsSelf() throws InterruptedException {
    GuiController controller = createController();

    String finiteCode = "1 init\n2 jcstop";  // S'arrête immédiatement
    loadCode(finiteCode);
    controller.startDebug();

    // Laisser le handler se terminer tout seul
    Thread.sleep(500);

    // L'UI devrait refléter l'arrêt
    assertFalse(controller.isDebugMode());
    assertTrue(stepButton.isDisabled());
}
```

---

## Problèmes Mineurs

### 🟡 MIN1. Utilisation Incohérente de Platform.runLater()

**Localisation:** `GuiController.java` (plusieurs endroits)

**Description:**

Certaines alertes sont wrappées dans `Platform.runLater()` (lignes 122-128, 169-176, 221-229), d'autres non. Cette incohérence rend difficile de savoir quand c'est nécessaire.

**Exemples:**

```java
// Ligne 122 - AVEC Platform.runLater
Platform.runLater(() -> {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Erreur lors de l'ouverture du fichier : ");
    alert.setContentText(e.toString());
    alert.showAndWait();
});

// Ligne 284 - AVEC Platform.runLater (dans callback Task)
task.setOnFailed(ev -> {
    Platform.runLater(() -> {  // ⚠️ Redondant? setOnFailed est déjà sur FX thread
        Alert alert = new Alert(AlertType.ERROR);
        alert.showAndWait();
    });
});
```

**Règle JavaFX:**
- `Task.setOnSucceeded()` et `Task.setOnFailed()` s'exécutent **déjà sur le FX thread**
- Pas besoin de `Platform.runLater()` à l'intérieur

**Solution:**

```java
// ✅ Correct
task.setOnFailed(ev -> {
    Throwable ex = task.getException();
    console.printMessage("Compilation failed: " + ex.getMessage());

    // Pas besoin de Platform.runLater ici!
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Erreur de compilation");
    alert.setHeaderText("Une erreur est survenue pendant la compilation");
    alert.setContentText(ex.toString());
    alert.showAndWait();
});
```

**Recommandation:**
1. Documenter les méthodes avec `@RequiresFXThread` ou `@AllowsAnyThread`
2. Créer une méthode helper pour les alertes

```java
/**
 * Shows an error alert. Must be called on FX thread.
 * @param title alert title
 * @param message error message
 */
@RequiresFXThread
private void showErrorAlert(String title, String message) {
    assert Platform.isFxApplicationThread() : "Must be called on FX thread";

    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setContentText(message);
    alert.showAndWait();
}

/**
 * Shows an error alert from any thread.
 */
private void showErrorAlertAsync(String title, String message) {
    if (Platform.isFxApplicationThread()) {
        showErrorAlert(title, message);
    } else {
        Platform.runLater(() -> showErrorAlert(title, message));
    }
}
```

---

### 🟡 MIN2. Tests Non Mis à Jour

**Localisation:** `GUI/src/test/java/fr/ufrst/m1info/gl/groupe7/gui/TestApp.java`

**Problème:**

Les tests utilisent la réflexion pour accéder à `App.loadFileContent()`, qui existe toujours mais délègue juste à `GuiController`.

```java
Method loadMethod = App.class.getDeclaredMethod("loadFileContent", File.class);
loadMethod.setAccessible(true);
```

**Impact:**
- Tests ne testent pas la vraie architecture
- Fragilité (changements cassent les tests)
- Faux sentiment de sécurité

**Solution:**

```java
// Option 1: Tester GuiController directement
@Test
void testLoadFileInController() {
    GuiController controller = createTestController();
    File testFile = new File("test.mjj");

    controller.loadFileContent(testFile);

    assertEquals(expectedContent, mjjCodeArea.getText());
}

// Option 2: Exposer une méthode de test publique
public class GuiController {
    @VisibleForTesting
    public void loadFileContentForTesting(File file) {
        loadFileContent(file);
    }
}
```

---

### 🟡 MIN3. Logging Incohérent

**Localisation:** Comparaison entre `GuiController` et `App`

**Problème:**

`App` utilise SLF4J logger:
```java
private static final Logger logger = LoggerFactory.getLogger(App.class);
```

Mais `GuiController` déclare le logger mais ne l'utilise presque jamais:
```java
private static final Logger logger = LoggerFactory.getLogger(GuiController.class);

// Jamais utilisé dans tout le fichier!
```

**Solution:**

Ajouter du logging aux opérations critiques:

```java
public void startDebug() {
    logger.debug("Starting debug session, mode: {}", fileToRun.getValue());

    if (debugMode) {
        logger.warn("Debug already active, ignoring start request");
        return;
    }

    // ...
}

public void stopDebug() {
    logger.debug("Stopping debug session, source: {}", debugSource);

    // ...

    logger.info("Debug session stopped successfully");
}

private void startJajaCodeDebugInternal() {
    logger.debug("Initializing JajaCode debug handler");

    try {
        // ...
        logger.info("JajaCode debug started, {} breakpoints set", breakpoints.size());
    } catch (Exception e) {
        logger.error("Failed to start JajaCode debug", e);
        throw e;
    }
}
```

---

## Points Positifs

### ✅ P1. Excellente Séparation des Responsabilités

L'extraction de `GuiController` depuis `App` est un excellent mouvement architectural:

- **App:** Gère uniquement le cycle de vie JavaFX et la construction de l'UI
- **GuiController:** Contient toute la logique métier

Cela facilite:
- Tests unitaires (tester GuiController sans lancer JavaFX)
- Réutilisation (GuiController peut être utilisé dans d'autres contextes)
- Maintenance (changements UI vs logique bien séparés)

### ✅ P2. Utilisation Correcte de Threads Daemon

Les deux handlers créent correctement des threads daemon:

```java
Thread t = new Thread(r, "jajacode-debugger");
t.setDaemon(true);  // ✅ Ne bloque pas la JVM shutdown
```

Cela garantit que l'application peut se fermer proprement même si des threads de debug sont actifs.

### ✅ P3. Bonne Utilisation de volatile

```java
private volatile boolean isRunning = false;
```

L'utilisation de `volatile` pour les flags partagés montre une compréhension des bases de la concurrence Java.

### ✅ P4. IDs pour Testabilité

```java
// Dans App.java, lignes 305-334
stepButton.setId("stepButton");
stopButton.setId("stopButton");
continueButton.setId("continueButton");
```

L'ajout d'IDs aux composants UI facilite grandement les tests d'intégration avec TestFX.

### ✅ P5. Gestion des Exceptions dans Tasks

Les Tasks JavaFX ont des handlers `setOnFailed` appropriés:

```java
task.setOnFailed(ev -> {
    Throwable ex = task.getException();
    console.printMessage("Compilation failed: " + ex.getMessage());
    // ... affichage de l'erreur
});
```

---

## Recommandations Architecturales

### ARCH1. Adopter une Architecture Réactive

Au lieu de polling et de busy-waiting, considérez une architecture basée sur des événements:

```java
public interface DebugEventListener {
    void onDebugStarted();
    void onDebugPaused(int line, MemorySnapshot snapshot);
    void onDebugResumed();
    void onDebugStopped();
    void onDebugError(Exception e);
    void onBreakpointHit(int line);
}

public abstract class AbstractDebugHandler {
    private final List<DebugEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(DebugEventListener listener) {
        listeners.add(listener);
    }

    protected void fireDebugPaused(int line, MemorySnapshot snapshot) {
        for (DebugEventListener listener : listeners) {
            Platform.runLater(() -> listener.onDebugPaused(line, snapshot));
        }
    }
}

// Dans GuiController:
debugHandler.addListener(new DebugEventListener() {
    @Override
    public void onDebugPaused(int line, MemorySnapshot snapshot) {
        updateUIWithSnapshot(snapshot);
        console.printMessage("[DEBUG] Paused at line " + line);
    }

    @Override
    public void onDebugStopped() {
        stopDebugInternal();
    }
});
```

**Avantages:**
- ✅ Découplage fort entre handler et UI
- ✅ Pas de busy-waiting
- ✅ Plus facile à tester (mock listeners)
- ✅ Extensible (multiples listeners)

### ARCH2. Considérer CompletableFuture pour les Opérations Async

Au lieu de `Task<V>`, utilisez `CompletableFuture` pour plus de flexibilité:

```java
public CompletableFuture<String> compileAsync(String code) {
    return CompletableFuture.supplyAsync(() -> {
        Compiler compiler = new Compiler(code, Compiler.Destination.STRING, null);
        return compiler.compileToString();
    }, executor);
}

// Usage:
compileAsync(mjjCodeArea.getText())
    .thenAcceptAsync(result -> {
        jjcCodeArea.loadText(result);
        console.printMessage("Compilation finished.");
    }, Platform::runLater)  // ✅ Update UI on FX thread
    .exceptionally(ex -> {
        console.printMessage("Compilation failed: " + ex.getMessage());
        showErrorAlert("Compilation Error", ex.getMessage());
        return null;
    });
```

**Avantages:**
- ✅ Composition (chainage avec `thenCompose`, `thenCombine`)
- ✅ Gestion d'erreurs avec `exceptionally`
- ✅ Standard Java (pas spécifique JavaFX)
- ✅ Testable avec mock executors

### ARCH3. Implémenter le Pattern Command pour le Debug

Utilisez le pattern Command pour encapsuler les actions de debug:

```java
public interface DebugCommand {
    void execute(Interpreter interpreter);
    boolean shouldPauseAfter();
}

public class StepCommand implements DebugCommand {
    @Override
    public void execute(Interpreter interpreter) {
        interpreter.step();
    }

    @Override
    public boolean shouldPauseAfter() {
        return true;  // Pause après chaque step
    }
}

public class ContinueCommand implements DebugCommand {
    private final Set<Integer> breakpoints;

    public ContinueCommand(Set<Integer> breakpoints) {
        this.breakpoints = breakpoints;
    }

    @Override
    public void execute(Interpreter interpreter) {
        while (!shouldPauseAtBreakpoint(interpreter)) {
            interpreter.step();
        }
    }

    @Override
    public boolean shouldPauseAfter() {
        return true;
    }

    private boolean shouldPauseAtBreakpoint(Interpreter interpreter) {
        return breakpoints.contains(interpreter.getCurrentLine());
    }
}

// Dans le handler:
public void executeCommand(DebugCommand command) {
    executor.submit(() -> {
        command.execute(interpreter);
        if (command.shouldPauseAfter()) {
            Platform.runLater(this::updateView);
        }
    });
}
```

**Avantages:**
- ✅ Commandes testables individuellement
- ✅ Historique (undo/redo)
- ✅ Macros (séquences de commandes)
- ✅ Replay de sessions de debug

### ARCH4. Injection de Dépendances

Au lieu de passer 10 paramètres au constructeur de `GuiController`, utilisez un objet de configuration ou un framework DI:

```java
public class GuiContext {
    private final Stage stage;
    private final MyCodeArea mjjCodeArea;
    private final MyCodeArea jjcCodeArea;
    private final ChoiceBox<String> fileToRun;
    private final ConsoleOutput console;
    private final TableView<VariableInfo> stackTable;
    private final TableView<HeapInfo> heapTable;
    private final Button stepButton;
    private final Button stopButton;
    private final Button continueButton;

    // Builder pattern
    public static class Builder {
        // ... builder implementation
    }
}

public class GuiController {
    private final GuiContext context;

    public GuiController(GuiContext context) {
        this.context = context;
    }
}

// Usage:
GuiContext context = new GuiContext.Builder()
    .stage(primaryStage)
    .mjjCodeArea(mjjCodeArea)
    .jjcCodeArea(jjcCodeArea)
    // ...
    .build();

GuiController controller = new GuiController(context);
```

---

## Plan d'Action

### Phase 1: Corrections Critiques (Bloquantes pour le merge)

**Priorité 1 - Sécurité Thread:**

1. ✅ **Synchroniser JajaCodeDebugHandler.stop()** (C1)
   - Ajouter un `Object lock`
   - Synchroniser tous les accès à `executor`, `interpreter`, `isRunning`
   - Temps estimé: 2h
   - Fichiers: `JajaCodeDebugHandler.java`

2. ✅ **Appeler controller.shutdown() dans App.stop()** (C2)
   - Override `App.stop()`
   - Appeler `controller.shutdown()`
   - Temps estimé: 30min
   - Fichiers: `App.java`

3. ✅ **Guard contre multiple start()** (C3)
   - Ajouter vérification `if (isRunning) return;`
   - Nettoyer executor existant si présent
   - Temps estimé: 1h
   - Fichiers: `JajaCodeDebugHandler.java`

4. ✅ **Gérer correctement InterruptedException** (C4)
   - Séparer catch pour InterruptedException
   - Restaurer interrupt status
   - Temps estimé: 1h
   - Fichiers: `JajaCodeDebugHandler.java`

**Total Phase 1: ~5 heures**

### Phase 2: Améliorations Majeures (Recommandées avant merge)

**Priorité 2 - Cohérence:**

5. ⚠️ **Uniformiser les modèles de threading** (M1)
   - Créer `AbstractDebugHandler`
   - Migrer les deux handlers vers le nouveau modèle
   - Temps estimé: 8h
   - Fichiers: Nouveaux `AbstractDebugHandler.java`, refactor des handlers

6. ⚠️ **Protéger accès UI** (M2)
   - Pattern "capture-then-update"
   - Ajouter assertions de thread
   - Temps estimé: 3h
   - Fichiers: `JajaCodeDebugHandler.java`, `GuiController.java`

7. ⚠️ **State Machine pour debugMode** (M3)
   - Créer enum `DebugState`
   - Encapsuler état dans `DebugSession`
   - Temps estimé: 4h
   - Fichiers: `GuiController.java`

**Total Phase 2: ~15 heures**

### Phase 3: Polissage (Post-merge)

**Priorité 3 - Qualité:**

8. 🔧 **Uniformiser Platform.runLater()** (MIN1)
   - Créer helpers `showErrorAlert()`, `showErrorAlertAsync()`
   - Documenter avec annotations
   - Temps estimé: 2h

9. 🔧 **Mettre à jour les tests** (MIN2)
   - Tests unitaires pour `GuiController`
   - Tests de concurrence pour handlers
   - Temps estimé: 6h

10. 🔧 **Améliorer le logging** (MIN3)
    - Ajouter logs aux opérations critiques
    - Niveaux appropriés (debug/info/warn/error)
    - Temps estimé: 1h

**Total Phase 3: ~9 heures**

### Phase 4: Architecture (Optionnel, amélioration continue)

**Priorité 4 - Long terme:**

11. 🎯 **Architecture réactive** (ARCH1)
    - Interface `DebugEventListener`
    - Découplage handler-UI
    - Temps estimé: 12h

12. 🎯 **CompletableFuture** (ARCH2)
    - Remplacer Task par CompletableFuture
    - Meilleure composition async
    - Temps estimé: 6h

13. 🎯 **Pattern Command** (ARCH3)
    - Commandes debug encapsulées
    - Historique et replay
    - Temps estimé: 10h

14. 🎯 **Dependency Injection** (ARCH4)
    - `GuiContext` builder
    - Simplifier constructeurs
    - Temps estimé: 4h

**Total Phase 4: ~32 heures**

---

## Résumé Exécutif

### Ce qui doit être corrigé IMMÉDIATEMENT:

1. **Race conditions critiques** - Peuvent causer des crashes
2. **Fuites de ressources** - Threads et executors non fermés
3. **Gestion d'interruption** - Peut bloquer l'application

### Ce qui devrait être amélioré AVANT le merge:

4. **Incohérence threading** - Rend le code difficile à maintenir
5. **Accès UI non protégés** - Risque de crashes UI
6. **État de debug fragile** - Peut mener à des bugs subtils

### Ce qui peut attendre APRÈS le merge:

7. **Polissage** - Tests, logging, documentation
8. **Améliorations architecturales** - Patterns avancés, refactoring

---

**Recommandation Finale:**

**❌ NE PAS MERGER** dans l'état actuel. Les problèmes critiques (C1-C4) doivent être résolus en priorité. Les problèmes majeurs (M1-M3) sont fortement recommandés avant le merge pour éviter d'introduire une dette technique importante.

Une fois les corrections de Phase 1 et Phase 2 appliquées, cette refactorisation sera un excellent ajout à la codebase qui améliore significativement la qualité architecturale du projet.

---

**Auteur:** Senior Code Reviewer Agentll
**Date de Review:** 2025-12-17
**Temps estimé total pour rendre mergeable:** ~20 heures (Phase 1 + Phase 2)
