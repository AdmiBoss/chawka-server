# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom first for layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Make wrapper executable (Linux inside container)
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S chawka && adduser -S chawka -G chawka

COPY --from=builder /build/target/chawka-server-*.jar app.jar

RUN chown chawka:chawka app.jar
USER chawka

EXPOSE 8080

# Pass env vars at runtime: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, APP_S3_BUCKET, etc.
ENTRYPOINT ["java", "-jar", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "app.jar"]
