FROM registry.access.redhat.com/ubi8/ubi-minimal:8.3

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# Install java and the run-java script
# Also set up permissions for user `1001`
RUN microdnf install curl ca-certificates ${JAVA_PACKAGE} \
    && microdnf update \
    && microdnf clean all \
    && mkdir /app \
    && chown 1001 /app \
    && chmod "g+rwX" /app \
    && chown 1001:root /app \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/lib/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Built dependencies
COPY target/lib/* /app/lib/
# Runner
COPY target/*-runner.jar /app/app.jar

# Http port
EXPOSE 8080
# Language Server Tcp Port
EXPOSE 8090

USER 1001

ENTRYPOINT [ "java","-cp", "/app/app.jar:/app/lib/*:/app/lib-ext/*","iti.kukumo.server.Runner" ]

