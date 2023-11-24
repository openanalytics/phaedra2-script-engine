# Generated file: do NOT change
FROM registry.openanalytics.eu/proxy/library/debian:buster as builder

# Set the working directory
WORKDIR /build

# Stage 1: Install JDK 17
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://download.java.net/java/GA/jdk17/0d483333a00540d886896bac774ff48b/35/GPL/openjdk-17_linux-x64_bin.tar.gz && \
    tar -xvf openjdk-17_linux-x64_bin.tar.gz

# FINAL IMAGE
FROM registry.openanalytics.eu/openanalytics/phaedra-r-base:latest

LABEL maintainer="Saša Berberović <sasa.berberovic@openanalytics.eu>"

ENV PHAEDRA_USER phaedra

RUN useradd -c 'phaedra user' -m -d /home/$PHAEDRA_USER -s /bin/nologin $PHAEDRA_USER
COPY --from=builder /build/jdk-17 /opt/jdk-17

ENV JAVA_HOME=/opt/jdk-17
ENV PATH=${PATH}:${JAVA_HOME}/bin

ARG JAR_FILE
ADD $JAR_FILE /opt/phaedra2/phaedra2-scriptengine-worker.jar

WORKDIR /opt/phaedra2
USER $PHAEDRA_USER

# ADD application.yml /opt/phaedra2/application.yml

CMD ["java", "-jar", "/opt/phaedra2/phaedra2-scriptengine-worker.jar", "--spring.jmx.enabled=false"]
