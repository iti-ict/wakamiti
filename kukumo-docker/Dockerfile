FROM openjdk:11

ENV KUKUMO_REMOTE_REPOSITORIES https://repo.maven.apache.org/maven2/

ENV KUKUMO_HOME /opt/kukumo
ENV KUKUMO_REPOSITORY /root/.local/share/kukumo/repository
ENV MAVEN_LOCAL_REPOSITORY /repository

WORKDIR /kukumo

COPY target/assembly/staging $KUKUMO_HOME/
COPY src $KUKUMO_HOME/
COPY src/kukumo $KUKUMO_HOME
COPY src/log4j2.xml $KUKUMNO_HOME
COPY src/entrypoint.sh /
COPY target/assembly/repo $MAVEN_LOCAL_REPOSITORY/

RUN groupadd --gid 1024 kukumo \
    && useradd --gid 1024 kukumo \
    && usermod -aG kukumo kukumo \
    && chmod +x /entrypoint.sh \
    && chmod +x $KUKUMO_HOME/kukumo \
    && echo $KUKUMO_HOME >> ~/.profile \
    && ln -s $KUKUMO_HOME/kukumo /usr/local/bin/kukumo \
    && chmod +x /usr/local/bin/kukumo \
    && chown -R kukumo:kukumo $KUKUMO_HOME \
    && chown -R kukumo:kukumo $MAVEN_LOCAL_REPOSITORY \
    && chown -R kukumo:kukumo /kukumo

#USER kukumo

RUN mkdir -p $KUKUMO_REPOSITORY

ENTRYPOINT ["/entrypoint.sh"]
