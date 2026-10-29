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
    && adduser --uid=1000 --home=/application --disabled-password --gecos "" --shell=/bin/bash worker \
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
ENTRYPOINT ["sh" ,"-c", "exec java ${JAVA_OPTS} --enable-preview -Dspring.profiles.active=docker org.springframework.boot.loader.JarLauncher"]

########################################################################################################################

LABEL git.branch=${git.branch} \
git.closest.tag.name=${git.closest.tag.fixed} \
git.commit.id=${git.commit.id} \
git.dirty=${git.dirty} \
mvn.project.artifactId=${project.artifactId} \
mvn.project.groupId=${project.groupId} \
mvn.project.version=${project.version}

USER root

# security: distrust the emSign (eMudhra) root CAs and the Comodo AAA Services root, which cross-signs the eMudhra
# intermediates with trivially guessable private keys (CVE-2026-114406 system store, CVE-2026-91403 java).
# Remove this step once ca-certificates and the JDK ship the fix.
RUN set -eux \
    && if [ -f /etc/ca-certificates.conf ]; then \
         sed -i -E 's#^mozilla/(emSign|Comodo_AAA_Services_root)#!mozilla/\1#' /etc/ca-certificates.conf \
         && update-ca-certificates --fresh; \
       fi \
    && for ks in $(find / -xdev -type f -name cacerts 2>/dev/null); do \
         kt=$(find / -xdev -type f -name keytool 2>/dev/null | head -1); \
         [ -n "$kt" ] || break; \
         "$kt" -list -keystore "$ks" -storepass changeit 2>/dev/null | grep -iE 'emsign|comodo.?aaa|aaa.?certificate.?services' \
           | sed -E 's/, [A-Z][a-z]{2} [0-9]{1,2}, [0-9]{4}, .*//' \
           | while IFS= read -r alias; do "$kt" -delete -keystore "$ks" -storepass changeit -alias "$alias"; done; \
       done

USER worker
