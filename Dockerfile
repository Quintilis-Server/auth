# Multi-stage build for Auth service (with React frontend)
# Stage 1: Build stage with Node.js for frontend
FROM maven:3.9-eclipse-temurin-25-noble AS build

WORKDIR /workspace

# Install Node.js (required for frontend build)
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy common module first for better layer caching
COPY common ./common
COPY auth/pom.xml ./auth/
COPY auth/.mvn ./auth/.mvn
COPY auth/mvnw ./auth/

# Copy auth source and frontend
COPY auth/src ./auth/src

# Build the application (this will also build the React frontend via maven plugins)
WORKDIR /workspace/auth
RUN mvn clean package -DskipTests


# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre-noble

WORKDIR /app

# Create a non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built jar from build stage
COPY --from=build /workspace/auth/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
