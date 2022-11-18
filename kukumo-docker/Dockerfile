FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine

WORKDIR /kukumo

ENV KUKUMO_HOME /opt/kukumo
ENV KUKUMO_REPOSITORY /root/.local/share/kukumo/repository

RUN apk add --no-cache bash

COPY src/entrypoint.sh /

# launcher executables are including in the staging directory (from kukumo-launcher)

COPY target/assembly/staging $KUKUMO_HOME/


RUN mkdir -p $KUKUMO_REPOSITORY \
    && chmod +x /entrypoint.sh \
    && chmod +x $KUKUMO_HOME/kukumo \
    && echo $KUKUMO_HOME >> ~/.profile \
    && ln -s $KUKUMO_HOME/kukumo /usr/local/bin/kukumo \
    && chmod +x /usr/local/bin/kukumo

ENTRYPOINT ["/entrypoint.sh"]
