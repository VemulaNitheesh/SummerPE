# Use a lightweight Java 21 Runtime image
FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot executable JAR into the container
COPY target/product-service-0.0.1-SNAPSHOT.jar app.jar

# Document that the application listens on port 8080
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
