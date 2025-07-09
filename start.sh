#!/bin/bash

# Kill all running Java processes before starting services
pkill -f java || true
  sleep 5

# Stop any running Docker containers and remove them
docker stop $(docker ps -q) || true
docker rm $(docker ps -aq) || true
# Stop any running docker-compose services
docker-compose down || true

echo "========================================="
echo "Building microservices Docker images..."
echo "========================================="

# Build all the Docker images using Maven package goal
mvn clean package

# Check if the Maven build was successful
if [ $? -ne 0 ]; then
  echo "Maven build failed. Aborting Docker Compose startup."
  exit 1
fi

echo "========================================="
echo "Starting Docker Compose environment..."
echo "========================================="

# Start the Docker Compose environment
docker-compose up -d

echo "========================================="
echo "Services are starting up..."
echo "========================================="
echo "You can check the status with: docker-compose ps"
echo "View logs with: docker-compose logs -f [service_name]"
