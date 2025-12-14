#!/bin/bash

echo "Starting Our Social Networks Backend in LOCAL mode..."
echo "Backend URL: http://localhost:8080"
echo "Frontend URL: http://localhost:4200"
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=local