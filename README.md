# Ceci est l'api REST (probablement pas full) de Bookexchange
## Lancement en dev
### 1. Lancer la bd de dev
```bash
cd dev-db
docker compose up
```
### 2. Optionellement mettre des variables d'environment
la variable DB_DDL_AUTO permet d'appliquer le schéma de DB qui provient des entités. Sa valeur par défault est 'false' mais elle peut être mise à 'true'
!!! à ne pas le faire sur la prod.
```bash
set DB_DDL_AUTO=true
```
### 3. Lancer l'api
```bash
./gradlew bootRun
ou (sur windows)
.\gradlew.bat bootRun
```