#!/bin/bash
# Script to start Keycloak (from docker-compose), then Kafka (docker run), then all Maven Spring Boot applications with service-discovery first and api-gateway last, 10 sec delay between each

# Kill all running Java processes before starting services
pkill -f java || true

# Set Java 21 as default if available
if [ -x /usr/lib/jvm/java-21-amazon-corretto/bin/java ]; then
  echo "Setting Java 21 as default..."
  sudo alternatives --set java /usr/lib/jvm/java-21-amazon-corretto/bin/java
  sudo alternatives --set javac /usr/lib/jvm/java-21-amazon-corretto/bin/javac
  export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
  export PATH=$JAVA_HOME/bin:$PATH
  echo "JAVA_HOME set to $JAVA_HOME"
fi

# Install parent POM first to resolve dependency issues
if [ -f "pom.xml" ]; then
  echo "Installing parent POM (banking-as-service) into local Maven repository..."
  mvn clean install -N
else
  echo "Root pom.xml not found! Skipping parent POM installation."
fi

# Start Keycloak service from docker-compose in detached mode
if [ -f "docker-compose.yml" ]; then
  echo "Starting Keycloak service from docker-compose..."
  docker-compose up -d keycloak
  echo "Waiting 15 seconds for Keycloak to initialize..."
  sleep 10
else
  echo "docker-compose.yml not found! Skipping Keycloak startup."
fi

echo "Starting Apache Kafka container..."
docker run -d --name kafka --rm -p 9092:9092 apache/kafka:latest
sleep 10

# Build and install common-lib first
# if [ -d "common-lib" ]; then
#   echo "Building and installing common-lib..."
#   (cd common-lib && mvn clean install)
# else
#   echo "common-lib directory not found! Skipping common-lib build."
# fi


modules=(
  "service-discovery"
  "account-service"
  "audit-service"
  "notification-service"
  "payment-service"
  "transaction-service"
  "user-service"
  "api-gateway"
)

for module in "${modules[@]}"; do
  if [ -d "$module" ]; then
    echo "Starting Spring Boot app in $module ..."
    (cd "$module" && mvn spring-boot:run &)
    echo "Waiting 10 seconds before starting the next service..."
    sleep 10
  else
    echo "Directory $module does not exist, skipping."
  fi
done

echo "All listed Spring Boot applications are starting in the background."
