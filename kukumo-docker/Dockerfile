FROM openjdk:11
LABEL version="1.2.0"

ENV KUKUMO_REMOTE_REPOSITORIES https://repo.maven.apache.org/maven2/

ENV USER_HOME /home/kukumo
ENV KUKUMO_HOME /opt/kukumo
ENV KUKUMO_REPOSITORY $USER_HOME/.local/share/kukumo/repository
#ENV MAVEN_LOCAL_REPOSITORY /repository
#ENV JACOCO_VERSION 0.8.5
#ENV JACOCO_HOME /opt/jacoco

WORKDIR /kukumo

COPY target/assembly/staging $KUKUMO_HOME/
COPY src $KUKUMO_HOME/
COPY src/kukumo $KUKUMO_HOME
COPY src/log4j2.xml $KUKUMNO_HOME
COPY src/entrypoint.sh /
#COPY target/assembly/repo $MAVEN_LOCAL_REPOSITORY/

RUN apt-get update \
    && apt-get install sudo

RUN adduser --disabled-password --gecos '' kukumo \
    && adduser kukumo sudo \
    && echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

#RUN curl -f https://repo1.maven.org/maven2/org/jacoco/jacoco/$JACOCO_VERSION/jacoco-$JACOCO_VERSION.zip -o jacoco.zip && \
#    mkdir $JACOCO_HOME && \
#    unzip jacoco.zip -d $JACOCO_HOME && \
#    rm jacoco.zip

RUN mkdir -p $KUKUMO_REPOSITORY

RUN chmod +x /entrypoint.sh \
    && chmod +x $KUKUMO_HOME/kukumo \
    && echo $KUKUMO_HOME >> ~/.profile \
    && ln -s $KUKUMO_HOME/kukumo /usr/local/bin/kukumo \
    && chmod +x /usr/local/bin/kukumo \
    && chown -R kukumo:kukumo $USER_HOME \
    && chown -R kukumo:kukumo $KUKUMO_HOME \
#    && chown -R kukumo:kukumo $MAVEN_LOCAL_REPOSITORY \
    && chown -R kukumo:kukumo /kukumo

USER kukumo

ENTRYPOINT ["/entrypoint.sh"]
