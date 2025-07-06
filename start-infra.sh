#!/bin/bash
# Script to start Keycloak (from docker-compose), then Kafka (docker run), then all Maven Spring Boot applications with service-discovery first and api-gateway last, 10 sec delay between each

# Kill all running Java processes before starting services
pkill -f java || true
  sleep 5

# Stop any running Docker containers and remove them
docker stop $(docker ps -q) || true
docker rm $(docker ps -aq) || true
# Stop any running docker-compose services
docker-compose down || true

# Set Java 21 as default if available
echo "Starting Apache Kafka container..."
docker run -d --name kafka --rm -p 9092:9092 apache/kafka:latest

# Start MySQL service from docker-compose in detached mode
if [ -f "docker-compose.yml" ]; then
  echo "Starting MySQL service from docker-compose..."
  docker-compose up -d mysql
  echo "Waiting 15 seconds for MySQL to initialize..."
  sleep 5
else
  echo "docker-compose.yml not found! Skipping MySQL startup."
fi

# Start Keycloak service from docker-compose in detached mode
if [ -f "docker-compose.yml" ]; then
  echo "Starting Keycloak service from docker-compose..."
  docker-compose up -d keycloak
  echo "Waiting 15 seconds for Keycloak to initialize..."
  sleep 5
else
  echo "docker-compose.yml not found! Skipping Keycloak startup."
fi


# Build and install common-lib first
if [ -d "common-lib" ]; then
  echo "Building and installing common-lib..."
  (cd common-lib && mvn clean install)
else
  echo "common-lib directory not found! Skipping common-lib build."
fi

# Install parent POM first to resolve dependency issues
if [ -f "pom.xml" ]; then
  echo "Installing parent POM (banking-as-service) into local Maven repository..."
  mvn clean install -N
else
  echo "Root pom.xml not found! Skipping parent POM installation."
fi
