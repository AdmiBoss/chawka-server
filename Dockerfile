# ── Runtime image ─────────────────────────────────────────────────────────────
# The CI pipeline builds the JAR and uploads it as app.jar.
# This Dockerfile simply packages it into a minimal runtime container.
FROM eclipse-temurin:17.0.13_11-jre-alpine

# Patch OS-level vulnerabilities
RUN apk update && apk upgrade --no-cache && rm -rf /var/cache/apk/*

WORKDIR /app

# Create non-root user for security
RUN addgroup -S chawka && adduser -S chawka -G chawka

COPY app.jar app.jar

RUN chown chawka:chawka app.jar
USER chawka

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO /dev/null http://localhost:8080/api/stats || exit 1

# Pass env vars at runtime: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, APP_S3_BUCKET, etc.
ENTRYPOINT ["java", "-jar", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "app.jar"]
