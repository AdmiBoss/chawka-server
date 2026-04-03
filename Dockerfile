# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17.0.13_11-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom first for layer caching
COPY mvnw.cmd mvnw pom.xml ./
COPY .mvn .mvn

# Make wrapper executable (Linux inside container)
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -q || \
    (apk add --no-cache maven && mvn dependency:go-offline -q)

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -q || \
    mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17.0.13_11-jre-alpine

# Patch OS-level vulnerabilities
RUN apk update && apk upgrade --no-cache && rm -rf /var/cache/apk/*

WORKDIR /app

# Create non-root user for security
RUN addgroup -S chawka && adduser -S chawka -G chawka

COPY --from=builder /build/target/chawka-server-*.jar app.jar

RUN chown chawka:chawka app.jar
USER chawka

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO /dev/null http://localhost:8080/api/dictionary || exit 1

# Pass env vars at runtime: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, APP_S3_BUCKET, etc.
ENTRYPOINT ["java", "-jar", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "app.jar"]
