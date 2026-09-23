FROM alpine:3.20 AS builder
ENV JACOCO_VERSION=0.8.12

RUN wget -O /jacocoagent.jar \
    "https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"


FROM springcommunity/spring-petclinic-rest:2.1.5
COPY --from=builder /jacocoagent.jar /opt/jacoco/jacocoagent.jar

EXPOSE 6300

ENTRYPOINT ["java", \
    "-javaagent:/opt/jacoco/jacocoagent.jar=output=tcpserver,address=*,port=6300", \
    "-cp", "/app/resources:/app/classes:/app/libs/*", \
    "org.springframework.samples.petclinic.PetClinicApplication" \
]
