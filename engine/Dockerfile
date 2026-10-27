# ---- build stage: JDK + Maven, big, thrown away afterwards ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src ./src
RUN ./mvnw -q -B -DskipTests package

# ---- run stage: only a JRE and the jar, small ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50", "-XX:+UseSerialGC", "-jar", "app.jar"]