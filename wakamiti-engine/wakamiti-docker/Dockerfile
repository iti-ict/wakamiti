FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
LABEL maintainer="mgalbis@iti.es"

# Wakamiti runtime distribution filename.
ARG BUILD_DATE
ARG BUILD_VERSION

# Labels.
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.name="wakamiti/wakamiti"
LABEL org.label-schema.description="Wakamiti core"
LABEL org.label-schema.url="https://iti-ict.github.io/wakamiti/"
LABEL org.label-schema.version=$BUILD_VERSION

WORKDIR /wakamiti

ENV TZ Europe/Madrid
ENV WAKAMITI_HOME /opt/wakamiti
ENV WAKAMITI_REPOSITORY /root/.local/share/wakamiti/repository

RUN apk add --no-cache bash libxext libxrender libxtst libxi fontconfig ttf-dejavu

COPY src/entrypoint.sh /

# launcher executables are including in the staging directory (from wakamiti-launcher)

COPY target/assembly/staging $WAKAMITI_HOME/

RUN mkdir -p $WAKAMITI_REPOSITORY \
    && ln -s $WAKAMITI_REPOSITORY /repository \
    && chmod +x /repository \
    && chmod +x /entrypoint.sh \
    && chmod +x $WAKAMITI_HOME/wakamiti \
    && echo $WAKAMITI_HOME >> ~/.profile \
    && ln -s $WAKAMITI_HOME/wakamiti /usr/local/bin/wakamiti \
    && chmod +x /usr/local/bin/wakamiti



ENTRYPOINT ["/entrypoint.sh"]
