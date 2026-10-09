FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY PrismAI/pom.xml ./pom.xml
COPY PrismAI/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

RUN apt-get update \
  && apt-get install -y --no-install-recommends libgomp1 \
  && rm -rf /var/lib/apt/lists/* \
  && useradd --system --uid 10001 --create-home prism

WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/PrismAI-*.jar /app/app.jar

USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]