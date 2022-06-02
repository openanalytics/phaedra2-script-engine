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
RUN apt-get update -y && \
    apt-get install unzip && \
    apt-get install -y --no-install-recommends build-essential cmake

#<<<INSTALL_JAVA>>>#

LABEL maintainer="Tobia De Koninck <tdekoninck@openanalytics.eu>"

ENV PHAEDRA_USER phaedra

RUN useradd -c 'phaedra user' -m -d /home/$PHAEDRA_USER -s /bin/nologin $PHAEDRA_USER
COPY --from=builder --chown=$PHAEDRA_USER:$PHAEDRA_USER /opt/phaedra2 /opt/phaedra2

ADD user_package_library/glpgPhaedra /opt/phaedra2/user_package_library/glpgPhaedra
ADD user_package_library/receptor2 /opt/phaedra2/user_package_library/receptor2

RUN R -e "install.packages('rjson')"
RUN R -e "install.packages('plyr')"
RUN R -e "install.packages('ape')"
RUN R -e "install.packages('reshape2')"
RUN R -e "install.packages('kSamples')"
RUN R -e "install.packages('nloptr')"
RUN R -e "install.packages('lme4')"
RUN R -e "install.packages('pbkrtest')"
RUN R -e "install.packages('car')"
RUN R -e "install.packages('drc')"
RUN R -e "install.packages('ggplot2')"

RUN R -e "install.packages('/opt/phaedra2/user_package_library/glpgPhaedra',repos=NULL, type='source')"
RUN R -e "install.packages('/opt/phaedra2/user_package_library/receptor2',repos=NULL, type='source')"

WORKDIR /opt/phaedra2
USER $PHAEDRA_USER

# ADD application.yml /opt/phaedra2/application.yml

CMD ["java", "-jar", "/opt/phaedra2/phaedra2-scriptengine-worker.jar", "--spring.jmx.enabled=false"]
