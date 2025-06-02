#!/bin/bash

# Stop the running Docker Compose environment if it exists
if docker-compose ps > /dev/null 2>&1; then
  echo "Stopping existing Docker Compose environment..."
  docker-compose down
else
  echo "No existing Docker Compose environment found."
fi

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
