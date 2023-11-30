# FINAL IMAGE
FROM registry.openanalytics.eu/openanalytics/phaedra-r-base

RUN apt-get update && \
    apt-get install -y openjdk-17-jdk curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Download and install ASM 9.1
RUN curl -O https://repo1.maven.org/maven2/org/ow2/asm/asm/9.1/asm-9.1.jar && \
    mv asm-9.1.jar /usr/share/java/asm.jar

# Download and install Groovy 3.0.8 using SDKMAN
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install groovy 3.0.8"

# Set environment variables for JDK 17
ENV JAVA_HOME=/usr/lib/jvm/java-18-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Set ASM and Groovy-related environment variables
ENV CLASSPATH=$CLASSPATH:/usr/share/java/asm.jar
ENV GROOVY_HOME=/root/.sdkman/candidates/groovy/3.0.8
ENV PATH=$PATH:$GROOVY_HOME/bin


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

CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-jar", "/opt/phaedra2/phaedra2-scriptengine-worker.jar"]
