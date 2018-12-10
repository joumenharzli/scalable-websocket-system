@echo off

echo [!] Compiling and publishing notifications dispatcher
cmd /C "cd notifications-dispatcher && sbt compile && sbt docker:publishLocal"

echo [!] Compiling and publishing notifications service
cmd /C "cd notifications-service && sbt compile && sbt docker:publishLocal"

echo [!] Running docker compose
docker-compose up -d --build