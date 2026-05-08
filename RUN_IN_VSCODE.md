# How to run this JavaFX GUI in VS Code

This version is a Maven JavaFX project, so you do **not** need to manually download JavaFX SDK or write `--module-path` yourself.

## Requirements

1. Install JDK 17 or newer.
2. Install VS Code extensions:
   - Extension Pack for Java
   - Maven for Java
3. Open the folder that contains `pom.xml` directly in VS Code.

## Run

### Option 1: VS Code Run button
Open:

```text
src/main/java/GUI/CODE/HotelApp.java
```

Then press **Run** above the `main` method.

### Option 2: Terminal
Run:

```bash
mvn clean javafx:run
```

The first run downloads JavaFX dependencies. After that it should run normally.

## Demo accounts

Admin:

```text
username: Admin_User
password: adminPass789
```

Receptionist:

```text
username: Staff_A
password: staffPass123
```

Guest:

```text
username: Abdullah
password: pass1234
```

## Important note

If you previously saw hundreds of errors inside controllers and multithreading files, that usually means JavaFX was not linked to the project. This Maven version fixes that by adding JavaFX dependencies in `pom.xml`.
