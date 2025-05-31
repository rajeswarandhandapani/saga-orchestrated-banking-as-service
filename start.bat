@echo off
echo =========================================
echo Building microservices Docker images...
echo =========================================

REM Build all the Docker images using Maven
call mvn clean spring-boot:build-image

REM Check if the Maven build was successful
if %ERRORLEVEL% NEQ 0 (
  echo Maven build failed. Aborting Docker Compose startup.
  exit /b %ERRORLEVEL%
)

echo =========================================
echo Starting Docker Compose environment...
echo =========================================

REM Start the Docker Compose environment
docker-compose up -d

echo =========================================
echo Services are starting up...
echo =========================================
echo You can check the status with: docker-compose ps
echo View logs with: docker-compose logs -f [service_name]
