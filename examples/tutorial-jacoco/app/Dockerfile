FROM openjdk:17-jdk-slim AS builder
ENV JACOCO_VERSION=0.8.12
RUN apt-get update && apt-get install -y curl \
    && mkdir -p /opt/jacoco \
    && curl -L https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar \
       -o /opt/jacoco/jacocoagent.jar

FROM springcommunity/spring-petclinic-rest:2.1.5
COPY --from=builder /opt/jacoco/jacocoagent.jar /opt/jacoco/jacocoagent.jar

EXPOSE 6300

ENTRYPOINT ["java", \
    "-javaagent:/opt/jacoco/jacocoagent.jar=output=tcpserver,address=*,port=6300", \
    "-cp", "/app/resources:/app/classes:/app/libs/*", \
    "org.springframework.samples.petclinic.PetClinicApplication" \
]
