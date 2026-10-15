# ============================================================
# Online Exam System - production image
#
# Render has no native Java runtime, so the app ships as a
# container. Build and run commands live here - leave Render's
# "Build Command" and "Start Command" fields EMPTY, otherwise
# Render uses its native build environment and fails with
# "JAVA_HOME is not set".
# ============================================================

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

# Tests are skipped: they would need a live database.
RUN mvn -B -ntp clean package -DskipTests


# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

# Informational - the real port is supplied at runtime via $PORT,
# which application.properties reads as server.port=${PORT:8080}
EXPOSE 8080

# MaxRAMPercentage keeps the heap inside Render's free 512 MB container.
# ExitOnOutOfMemoryError makes a memory fault fail fast and visibly,
# rather than leaving a half-dead container behind a health check.
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=70", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]
