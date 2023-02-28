FROM ${docker.from.kafka}

RUN mkdir -p /opt/bitnami/kafka/plugins \
    && cd /opt/bitnami/kafka/plugins \
    && curl --remote-name --location --silent https://search.maven.org/remotecontent?filepath=org/mongodb/kafka/mongo-kafka-connect/1.9.1/mongo-kafka-connect-1.9.1-all.jar

COPY assets/config/ /opt/bitnami/kafka/config/
COPY assets/entrypoint.sh bin/


STOPSIGNAL SIGWINCH

ENTRYPOINT ["entrypoint.sh"]
CMD /opt/bitnami/kafka/bin/connect-standalone.sh /opt/bitnami/kafka/config/connect-standalone.properties /opt/bitnami/kafka/config/mongo.properties


########################################################################################################################

LABEL git.branch=${git.branch}
LABEL git.closest.tag.name=${git.closest.tag.fixed}
LABEL git.commit.id=${git.commit.id}
LABEL git.dirty=${git.dirty}
LABEL mvn.project.artifactId=${project.artifactId}
LABEL mvn.project.groupId=${project.groupId}
LABEL mvn.project.version=${project.version}
