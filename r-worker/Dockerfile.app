FROM registry.openanalytics.eu/openanalytics/phaedra-r-base:latest

ARG JAR_FILE
ADD $JAR_FILE /opt/phaedra2/phaedra2-scriptengine-worker.jar

RUN chmod a+rwx -R /opt/phaedra2/

LABEL maintainer="Saša Berberović <sasa.berberovic@openanalytics.eu>"

ENV PHAEDRA_USER phaedra

RUN useradd -c 'phaedra user' -m -d /home/$PHAEDRA_USER -s /bin/nologin $PHAEDRA_USER

WORKDIR /opt/phaedra2
USER $PHAEDRA_USER

CMD ["java", "-jar", "/opt/phaedra2/phaedra2-scriptengine-worker.jar"]
