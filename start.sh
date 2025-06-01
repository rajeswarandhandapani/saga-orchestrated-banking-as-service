#!/bin/bash

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
