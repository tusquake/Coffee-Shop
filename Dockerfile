# ----------------------------------
# Stage 1: Build Stage (The Builder)
# This stage compiles the code and generates the executable JAR.
# ----------------------------------
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files (mvnw, .mvn) and pom.xml first to optimize Docker caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy the rest of the source code
COPY src src

# Grant execution permission to the Maven wrapper
RUN chmod +x mvnw

# Run the Maven package command to build the JAR.
# -DskipTests is used here to ensure the build completes quickly without running tests.
RUN ./mvnw clean package -DskipTests


# ----------------------------------
# Stage 2: Run Stage (The Runner)
# This stage creates the final lightweight image containing only the JRE and the JAR.
# ----------------------------------
# Use a lightweight JRE base image (less than half the size of the full JDK image)
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the 'build' stage into the 'run' stage
# The JAR file is typically named your-artifact-version.jar, so we use *.jar as a wildcard
COPY --from=build /app/target/*.jar app.jar

# Define the port the container will expose (Spring Boot's default)
EXPOSE 8080

# Define the command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]