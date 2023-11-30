# FINAL IMAGE
FROM registry.openanalytics.eu/openanalytics/phaedra-r-base:latest

RUN apt-get update && apt-get install -y apt-transport-https
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y openjdk-17-jdk curl && \
    apt-get clean

# Set environment variables for JDK 17
ENV JAVA_HOME=/usr/lib/jvm/java-18-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

ARG JAR_FILE
ADD $JAR_FILE /opt/phaedra2/phaedra2-scriptengine-worker.jar

RUN chmod a+rwx -R /opt/phaedra2/

LABEL maintainer="Saša Berberović <sasa.berberovic@openanalytics.eu>"

ENV PHAEDRA_USER phaedra

RUN useradd -c 'phaedra user' -m -d /home/$PHAEDRA_USER -s /bin/nologin $PHAEDRA_USER
#COPY --from=builder --chown=$PHAEDRA_USER:$PHAEDRA_USER /opt/phaedra2 /opt/phaedra2

WORKDIR /opt/phaedra2
USER $PHAEDRA_USER

# ADD application.yml /opt/phaedra2/application.yml

CMD ["java", "-jar", "/opt/phaedra2/phaedra2-scriptengine-worker.jar"]
