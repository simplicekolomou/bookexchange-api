# Ceci est l'api REST (probablement pas full) de Bookexchange
## Lancement en dev
### 1. Lancer la bd de dev
```bash
cd dev-db
docker compose up
```
### 2. Mettre les variables d'environment
Se référer au fichier .env.example pour les variables de dev.
```bash
set DB_DDL_AUTO=true
```
Ou si vous utilisez intelliJ,
vous pouvez modifier les variables d'environement dans la configuration de lancement de la tâche bootRun.
### 3. Lancer l'api
```bash
./gradlew bootRun
ou (sur windows)
.\gradlew.bat bootRun
```
## Build d'une image docker
### 1. Créer le fichier .jar
```bash
gradle bootJar
```
### 2. Créer l'image
Modifiez le tag si nécessaire.
```aiignore
build -t be-api:0.0.1 --build-arg JAR_FILE=build/libs/api-0.0.1-SNAPSHOT.jar .
```