FROM ${docker.from.eclipse-temurin.21.jdk} as builder
WORKDIR application
ARG JAR_FILE=artifacts/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted && ls -la extracted
RUN for layer in dependencies spring-boot-loader snapshot-dependencies application; do mkdir -p "extracted/$layer" && touch "extracted/$layer/.keep"; done


FROM ${docker.from.eclipse-temurin.21}


VOLUME /tmp
WORKDIR application

#RUN set -eux \
#    && adduser --uid=1000 --home=/application --gecos "" --shell=/bin/bash worker \
#    && chown -RL worker:worker . \
#    && chown -RL worker:worker /tmp

RUN set -eux \
    && adduser --uid=1000 --home=/application --disabled-password --gecos "" --shell=/bin/bash worker \
    && chown -RL worker:worker . \
    && chown -RL worker:worker /tmp

USER worker

COPY --from=builder application/extracted/dependencies/ ./
COPY --from=builder application/extracted/spring-boot-loader/ ./
COPY --from=builder application/extracted/snapshot-dependencies/ ./
COPY --from=builder application/extracted/application/ ./

########################################################################################################################

RUN set -eux \
    && mkdir profiler \
    && wget -qO- https://github.com/Granulate/async-profiler/releases/download/v2.0g1/async-profiler-2.0-linux-x64.tar.gz | tar xvz -C profiler

COPY --chown=worker:worker assets/profiler profiler/

########################################################################################################################

EXPOSE 8080
ENTRYPOINT ["sh" ,"-c", "exec java ${JAVA_OPTS} -Dspring.profiles.active=docker org.springframework.boot.loader.launch.JarLauncher"]

########################################################################################################################

LABEL git.branch=${git.branch} \
git.closest.tag.name=${git.closest.tag.fixed} \
git.commit.id=${git.commit.id} \
git.dirty=${git.dirty} \
mvn.project.artifactId=${project.artifactId} \
mvn.project.groupId=${project.groupId} \
mvn.project.version=${project.version}
