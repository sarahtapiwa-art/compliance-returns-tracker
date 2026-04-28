# Build stage
FROM maven:3.8.4-openjdk-17 AS build

COPY . .
RUN mvn clean install -DskipTests

# Final stage
FROM openjdk:17

# Copy the jar file from the build stage only
COPY --from=build returns-tracking-system.jar .
EXPOSE 18000

CMD ["java", "-jar","returns-tracking-system.jar"]
