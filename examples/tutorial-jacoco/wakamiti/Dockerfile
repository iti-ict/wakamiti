FROM openjdk:17-jdk-slim AS builder
ENV JACOCO_VERSION=0.8.12
RUN apt-get update && apt-get install -y curl \
    && mkdir -p /opt/jacoco \
    && curl -L https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/${JACOCO_VERSION}/org.jacoco.cli-${JACOCO_VERSION}-nodeps.jar \
       -o /opt/jacoco/jacococli.jar

FROM wakamiti/wakamiti:2.7.2
COPY --from=builder /opt/jacoco/jacococli.jar /opt/jacoco/jacococli.jar
COPY --chmod=755 jacoco /usr/local/bin/

