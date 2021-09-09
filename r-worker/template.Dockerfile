# Template file: can be changed
FROM debian:buster as builder

RUN mkdir /opt/phaedra2
ARG JAR_FILE
ADD $JAR_FILE /opt/phaedra2/phaedra2-scriptengine-worker.jar

RUN apt-get update -y && \
    apt-get install unzip && \
    unzip -p  /opt/phaedra2/phaedra2-scriptengine-worker.jar META-INF/MANIFEST.MF | sed -En 's/Implementation-Version: (.*)\r/\1/gp' > /opt/phaedra2/VERSION

# FINAL IMAGE
FROM openanalytics/r-base

#<<<INSTALL_JAVA>>>#

LABEL maintainer="Tobia De Koninck <tdekoninck@openanalytics.eu>"

ENV PHAEDRA_USER phaedra

RUN useradd -c 'phaedra user' -m -d /home/$PHAEDRA_USER -s /bin/nologin $PHAEDRA_USER
COPY --from=builder --chown=$PHAEDRA_USER:$PHAEDRA_USER /opt/phaedra2 /opt/phaedra2

RUN R -e "install.packages('rjson')"

WORKDIR /opt/phaedra2
USER $PHAEDRA_USER

# ADD application.yml /opt/phaedra2/application.yml

CMD ["java", "-jar", "/opt/phaedra2/phaedra2-scriptengine-worker.jar", "--spring.jmx.enabled=false"]
