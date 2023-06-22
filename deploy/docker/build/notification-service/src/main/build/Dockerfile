FROM ${docker.from.openjdk.17} as builder
WORKDIR application
ARG JAR_FILE=artifacts/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract && ls -la
RUN if [[ -z "$(ls -A ./snapshot-dependencies)" ]] ; then touch ./snapshot-dependencies/.keep; fi


FROM ${docker.from.eclipse-temurin.17}


VOLUME /tmp
WORKDIR application

#RUN set -eux \
#    && adduser --uid=1000 --home=/application --gecos "" --shell=/bin/bash worker \
#    && chown -RL worker:worker . \
#    && chown -RL worker:worker /tmp

RUN set -eux \
    && adduser -D -u 1000 -h /application -s bin/bash worker \
    && chown -RL worker:worker . \
    && chown -RL worker:worker /tmp

USER worker

COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

########################################################################################################################

RUN set -eux \
    && mkdir profiler \
    && wget -qO- https://github.com/Granulate/async-profiler/releases/download/v2.0g1/async-profiler-2.0-linux-x64.tar.gz | tar xvz -C profiler

COPY --chown=worker:worker assets/profiler profiler/

########################################################################################################################

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher", "${JAVA_OPTS:-}"]

########################################################################################################################

LABEL git.branch=${git.branch}
LABEL git.closest.tag.name=${git.closest.tag.fixed}
LABEL git.commit.id=${git.commit.id}
LABEL git.dirty=${git.dirty}
LABEL mvn.project.artifactId=${project.artifactId}
LABEL mvn.project.groupId=${project.groupId}
LABEL mvn.project.version=${project.version}
