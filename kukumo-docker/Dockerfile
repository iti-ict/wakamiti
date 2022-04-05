FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine

RUN apk add --no-cache bash

ENV KUKUMO_REMOTE_REPOSITORIES maven-central=https://repo.maven.apache.org/maven2/
ENV KUKUMO_HOME /opt/kukumo
ENV KUKUMO_REPOSITORY /root/.local/share/kukumo/repository

WORKDIR /kukumo

COPY target/assembly/staging $KUKUMO_HOME/
COPY src $KUKUMO_HOME/
COPY src/kukumo $KUKUMO_HOME
COPY src/log4j2.xml $KUKUMO_HOME
COPY src/entrypoint.sh /

RUN mkdir -p $KUKUMO_REPOSITORY

RUN chmod +x /entrypoint.sh \
    && chmod +x $KUKUMO_HOME/kukumo \
    && echo $KUKUMO_HOME >> ~/.profile \
    && ln -s $KUKUMO_HOME/kukumo /usr/local/bin/kukumo \
    && chmod +x /usr/local/bin/kukumo

ENTRYPOINT ["/entrypoint.sh"]
