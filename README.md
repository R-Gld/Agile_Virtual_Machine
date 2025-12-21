[![Coverage](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=coverage&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)
[![Duplicated Lines (%)](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=duplicated_lines_density&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)
[![Lines of Code](https://disc.univ-fcomte.fr/cr700-sonarqube/api/project_badges/measure?project=avm-2025-groupe-7&metric=ncloc&token=sqb_2b2e9e6502855e5ad0c68935e4ad671cb02372d5)](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)

# AVM 2025 Project - Group 7

## Project Description

The **AVM 2025 (Agile Virtual Machine)** project is part of the *Agile Development of Virtual Machines* module in the Master's program in Computer Science at the University of Franche-Comté.
The main objective is to **design, compile, and interpret a minimalist programming language** called **MiniJaja**, along with its compiled version **JajaCode**.

The project follows an **Agile** approach (*Scrum*-style): iterative development, 2-week sprints, continuous integration, and regular validation through testing and code reviews.

---

## Learning Objectives

- **Master advanced programming and compilation concepts**:
    - Lexical and syntactic analysis;
    - Construction of Abstract Syntax Trees (AST);
    - Type checking and interpretive semantics;
    - MiniJaja → JajaCode compilation;
    - Interpretation of both languages.

- **Apply Software Engineering principles**:
    - Modular software architecture (parser, memory, interpreter, GUI...);
    - Validation through unit, integration, and acceptance testing;
    - Version control with Git and continuous integration with GitLab CI/CD;
    - Quality monitoring with **SonarQube** and dependency management with **Maven/Nexus**.

- **Work in an agile team**:
    - Product backlog breakdown;
    - Sprint planning with clear objectives;
    - Pair programming, code reviews, and retrospectives;
    - Using Jira for task tracking and release management.

---

## Tools and Environment

| Tool             | Role                                                          |
|------------------|---------------------------------------------------------------|
| **Java 21**      | Main project language                                         |
| **Maven**        | Project build and dependency management                       |
| **Git / GitLab** | Version control and continuous integration                    |
| **SonarQube**    | Code quality analysis and test coverage                       |
| **Nexus**        | Maven dependency and artifact repository                      |
| **JavaFX**       | Graphical user interface                                      |
| **JUnit**        | Unit and integration testing                                  |
| **ANTLR**        | Automatic generation of lexical and syntactic analyzers       |


---

## Architecture and Modules

The MiniJAJA project is structured around **7 Maven modules**:

| Module | Role | Key Technologies |
|--------|------|------------------|
| **LexerParser** | Lexical/syntactic analysis, AST and interpretation | ANTLR 4, JUnit 5, Cucumber |
| **Memoire** | Runtime memory management and symbol tables | Native Java |
| **CLI** | Command-line interface for compilation/execution | Apache Commons CLI |
| **GUI** | Interactive graphical interface with JavaFX | JavaFX 23, TestFX |
| **ASTExporter** | Syntax tree visualization and export | ANTLR 4 |
| **CoverageReport** | Code coverage report aggregation | JaCoCo |
| **Rapport** | LaTeX documentation generation | pdflatex |

### Module Dependencies

```
LexerParser (core module)
  └─ Memoire
CLI → LexerParser → Memoire
GUI → LexerParser + CLI → Memoire
ASTExporter → LexerParser
CoverageReport (aggregator of all modules)
Rapport (independent documentation)
```

### Module Responsibilities

- **LexerParser**: Core of the project. Implements MiniJaja → JajaCode compilation and interpretation of both languages.
- **Memoire**: Abstractions for runtime state management (variables, scopes, symbols).
- **CLI**: Entry point for command-line usage (compilation, execution, debugging).
- **GUI**: JavaFX IDE with editor, console, and step-by-step execution visualization.
- **ASTExporter**: Utility for generating Abstract Syntax Tree diagrams.
- **CoverageReport**: Aggregation module for JaCoCo coverage reports.
- **Rapport**: Generation of the project summary document in PDF via LaTeX.

---

## Installation and Setup

### Prerequisites

- **Java 21** or higher
- **Maven 3.9.9** or higher
- **Git** (for repository cloning)
- **pdflatex** (optional, for generating the Report)

### Installation Steps

#### 1. Clone the repository

```bash
git clone https://github.com/R-Gld/Agile_Virtual_Machine.git
cd Agile_Virtual_Machine
```

#### 2. Build the project

**Complete build:**
```bash
mvn clean install
```

**Quick build (main modules only):**
```bash
mvn clean compile -pl '!Rapport'
```

**Install base modules (without tests):**
```bash
mvn install -pl 'LexerParser,CLI' -am -DskipTests
```

#### 3. Run the application

**CLI interface (compiler):**
```bash
# After build with maven package
java -jar CLI/target/CLI-2.1-SNAPSHOT-jar-with-dependencies.jar [options] <file.mj>
```

**GUI interface (interactive IDE):**
```bash
# Launch directly via Maven
mvn javafx:run -pl GUI

# Or after build with maven package
java -jar GUI/target/GUI-2.1-SNAPSHOT-jar-with-dependencies.jar
```

**AST Exporter (tree visualization):**
```bash
# After build with maven package
java -jar ASTExporter/target/ASTExporter-2.1-SNAPSHOT-shaded.jar <file.mj> <output.png>
```

#### 4. IDE Configuration

**IntelliJ IDEA / Eclipse:**
1. Open the project as a Maven project
2. The IDE automatically detects the 7 modules
3. Maven generates ANTLR files on first compilation
4. Configure JDK 21 in project settings

---

## Testing and Validation

### Running Tests

**Complete tests (main modules):**
```bash
mvn test -pl '!GUI,!CoverageReport,!Rapport'
```

**Specific module tests:**
```bash
mvn test -pl LexerParser
mvn test -pl CLI
mvn test -pl Memoire
```

**GUI tests (requires prior dependency installation):**
```bash
mvn install -pl 'LexerParser,CLI' -am -DskipTests
mvn test -pl GUI
```

### Code Coverage (JaCoCo)

**Generate coverage reports:**
```bash
mvn jacoco:prepare-agent test jacoco:report
```

**Aggregated report (all modules):**
```bash
mvn clean test
mvn jacoco:report-aggregate -pl CoverageReport
```

Coverage reports are generated in:
- Per module: `<module>/target/site/jacoco/index.html`
- Aggregated: `CoverageReport/target/site/jacoco-aggregate/index.html`

### Test Frameworks Used

| Framework | Usage |
|-----------|-------|
| **JUnit 5** | Unit and integration testing |
| **Cucumber** | Behavior-driven testing (BDD) with `.feature` files |
| **Mockito** | Object mocking for isolated tests |
| **TestFX** | JavaFX graphical interface testing |
| **JaCoCo** | Code coverage metrics |

### Headless Testing (Servers Without Graphical Interface)

The GUI module requires a display for testing. On systems without X11 interface:

```bash
# Install Xvfb (Linux/Ubuntu)
sudo apt-get install xvfb

# Run GUI tests in headless mode
xvfb-run -a mvn test -pl GUI
```

### Quality Analysis

Quality metrics (coverage, duplication, bugs) are available on the SonarQube dashboard:
[SonarQube Dashboard - AVM 2025 Group 7](https://disc.univ-fcomte.fr/cr700-sonarqube/dashboard?id=avm-2025-groupe-7)

---

## Work Methodology

1. **Project initialization**: definition of product backlog and team roles.
2. **Bi-weekly sprints**:
    - Planning (user stories, estimates, priorities);
    - Development with pair programming;
    - Continuous integration and code review;
    - Sprint review and retrospective.
3. **Tracking via Jira**: backlog management, sprints, and task traceability.
4. **GitLab CI/CD**:
    - Automatic Maven build launch;
    - Test execution;
    - SonarQube analysis;
    - Artifact deployment.

---

## Releases

- **Release 1**: first functional version delivered in early November.
    - Partial MiniJaja → JajaCode compilation;
    - Simple program interpretation;
    - Initial unit tests and configured CI.

- **Release 2**: final version delivered in mid-December.
    - Complete compilation and step-by-step execution;
    - Integrated JavaFX graphical interface;
    - Complete test suites and compliant quality metrics.

---

## Evaluation and Deliverables

- **Final report** (December 15 – Moodle submission):
    - Software architecture;
    - Description of technical choices and tests;
    - Results on the provided test suite;
    - Agile organization report;
    - Project retrospective.

- **Presentation** (December 18–19):
    - 20 min technical presentation;
    - 15 min software demonstration;
    - 15 min Q&A.

---

## Team – Group 7

- **Romain GALLAND** ([@R-Gld](https://github.com/R-Gld) on GitHub)
- **Lucas LAURET** ([@llauret](https://github.com/llauret) on GitHub)
- **Léo MAUGERI** ([@PlsJustDoIt](https://github.com/PlsJustDoIt) on GitHub)
- **Félix RIAT**
- **Théo VALFREY**
- **Ahmed DJEMAOUI** ([@djemaouiahmed](https://github.com/djemaouiahmed) on GitHub)
- **Javad AFSHAR**
