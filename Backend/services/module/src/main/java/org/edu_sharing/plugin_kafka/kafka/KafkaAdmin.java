package org.edu_sharing.plugin_kafka.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnsupportedVersionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KafkaAdmin implements ApplicationContextAware {
    /**
     * The default close timeout duration as 10 seconds.
     */
    public static final Duration DEFAULT_CLOSE_TIMEOUT = Duration.ofSeconds(10);
    public static final int DEFAULT_OPERATION_TIMEOUT = 30;

    final Map<String, Object> configs;

    @Setter
    private ApplicationContext applicationContext;

    @Setter
    private boolean autoCreate = true;

    @Setter
    private boolean fatalIfBrokerNotAvailable;

    private Duration closeTimeout = DEFAULT_CLOSE_TIMEOUT;

    @Setter
    @Getter
    private long operationTimeout = DEFAULT_OPERATION_TIMEOUT;

    @Setter
    private boolean modifyTopicConfigs;

    private boolean initializingContext;
    private String clusterId;

    /**
     * Set the close timeout in seconds. Defaults to {@link #DEFAULT_CLOSE_TIMEOUT} seconds.
     *
     * @param closeTimeout the timeout.
     */
    public void setCloseTimeout(int closeTimeout) {
        this.closeTimeout = Duration.ofSeconds(closeTimeout);
    }

    @PostConstruct
    public void afterSingletonsInstantiated() {
        this.initializingContext = true;
        if (this.autoCreate) {
            initialize();
        }
    }

    public final boolean initialize() {
        Collection<NewTopic> newTopics = newTopics();

        if (newTopics.size() > 0) {
            AdminClient adminClient = null;
            try {
                adminClient = createAdmin();
            } catch (Exception e) {
                if (!this.initializingContext || this.fatalIfBrokerNotAvailable) {
                    throw new IllegalStateException("Could not create admin", e);
                } else {
                    log.error("Could not create admin", e);
                }
            } finally {
                this.initializingContext = false;
            }
            if (adminClient != null) {
                try {
                    synchronized (this) {
                        this.clusterId = adminClient.describeCluster().clusterId().get(this.operationTimeout,
                                TimeUnit.SECONDS);
                    }
                    addOrModifyTopicsIfNeeded(adminClient, newTopics);
                    return true;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    if (!this.initializingContext || this.fatalIfBrokerNotAvailable) {
                        throw new IllegalStateException("Could not configure topics", ex);
                    } else {
                        log.error("Could not configure topics", ex);
                    }
                } finally {
                    this.initializingContext = false;
                    adminClient.close(this.closeTimeout);
                }
            }
        }

        this.initializingContext = false;
        return false;
    }

    private Collection<NewTopic> newTopics() {
        Map<String, NewTopic> newTopicsMap = new HashMap<>(
                this.applicationContext.getBeansOfType(NewTopic.class, false, false));

        AtomicInteger count = new AtomicInteger();
        Map<String, NewTopics> wrappers = this.applicationContext.getBeansOfType(NewTopics.class, false, false);
        wrappers.forEach((name, newTopics) ->
                newTopics.getNewTopics().forEach(nt -> newTopicsMap.put(name + "#" + count.getAndIncrement(), nt)));

        return new ArrayList<>(newTopicsMap.values());
    }

    private AdminClient createAdmin() {
        return AdminClient.create(new HashMap<>(this.configs));
    }

    private void addOrModifyTopicsIfNeeded(AdminClient adminClient, Collection<NewTopic> topics) {
        if (topics.size() > 0) {
            Map<String, NewTopic> topicNameToTopic = new HashMap<>();
            topics.forEach(t -> topicNameToTopic.compute(t.name(), (k, v) -> t));
            DescribeTopicsResult topicInfo = adminClient
                    .describeTopics(topics.stream()
                            .map(NewTopic::name)
                            .collect(Collectors.toList()));
            List<NewTopic> topicsToAdd = new ArrayList<>();
            Map<String, NewPartitions> topicsWithPartitionMismatches =
                    checkPartitions(topicNameToTopic, topicInfo, topicsToAdd);
            if (topicsToAdd.size() > 0) {
                addTopics(adminClient, topicsToAdd);
            }
            if (topicsWithPartitionMismatches.size() > 0) {
                createMissingPartitions(adminClient, topicsWithPartitionMismatches);
            }
            if (this.modifyTopicConfigs) {
                List<NewTopic> toCheck = new LinkedList<>(topics);
                toCheck.removeAll(topicsToAdd);
                Map<ConfigResource, List<ConfigEntry>> mismatchingConfigs =
                        checkTopicsForConfigMismatches(adminClient, toCheck);
                if (!mismatchingConfigs.isEmpty()) {
                    adjustConfigMismatches(adminClient, topics, mismatchingConfigs);
                }
            }
        }
    }

    private Map<ConfigResource, List<ConfigEntry>> checkTopicsForConfigMismatches(
            AdminClient adminClient, Collection<NewTopic> topics) {

        List<ConfigResource> configResources = topics.stream()
                .map(topic -> new ConfigResource(ConfigResource.Type.TOPIC, topic.name()))
                .collect(Collectors.toList());

        DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(configResources);
        try {
            Map<ConfigResource, Config> topicsConfig = describeConfigsResult.all()
                    .get(this.operationTimeout, TimeUnit.SECONDS);

            Map<ConfigResource, List<ConfigEntry>> configMismatches = new HashMap<>();
            for (Map.Entry<ConfigResource, Config> topicConfig : topicsConfig.entrySet()) {
                Optional<NewTopic> topicOptional = topics.stream()
                        .filter(p -> p.name().equals(topicConfig.getKey().name()))
                        .findFirst();

                List<ConfigEntry> configMismatchesEntries = new ArrayList<>();
                if (topicOptional.isPresent() && topicOptional.get().configs() != null) {
                    for (Map.Entry<String, String> desiredConfigParameter : topicOptional.get().configs().entrySet()) {
                        ConfigEntry actualConfigParameter = topicConfig.getValue().get(desiredConfigParameter.getKey());
                        if (!desiredConfigParameter.getValue().equals(actualConfigParameter.value())) {
                            configMismatchesEntries.add(actualConfigParameter);
                        }
                    }
                    if (configMismatchesEntries.size() > 0) {
                        configMismatches.put(topicConfig.getKey(), configMismatchesEntries);
                    }
                }
            }
            return configMismatches;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Interrupted while getting topic descriptions:" + topics, ie);
        } catch (ExecutionException | TimeoutException ex) {
            throw new KafkaException("Failed to obtain topic descriptions:" + topics, ex);
        }
    }

    private void adjustConfigMismatches(AdminClient adminClient, Collection<NewTopic> topics,
                                        Map<ConfigResource, List<ConfigEntry>> mismatchingConfigs) {
        for (Map.Entry<ConfigResource, List<ConfigEntry>> mismatchingConfigsOfTopic : mismatchingConfigs.entrySet()) {
            ConfigResource topicConfigResource = mismatchingConfigsOfTopic.getKey();

            Optional<NewTopic> topicOptional = topics.stream().filter(p -> p.name().equals(topicConfigResource.name()))
                    .findFirst();
            if (topicOptional.isPresent()) {
                for (ConfigEntry mismatchConfigEntry : mismatchingConfigsOfTopic.getValue()) {
                    List<AlterConfigOp> alterConfigOperations = new ArrayList<>();
                    Map<String, String> desiredConfigs = topicOptional.get().configs();
                    if (desiredConfigs.get(mismatchConfigEntry.name()) != null) {
                        alterConfigOperations.add(
                                new AlterConfigOp(
                                        new ConfigEntry(mismatchConfigEntry.name(),
                                                desiredConfigs.get(mismatchConfigEntry.name())),
                                        AlterConfigOp.OpType.SET));
                    }
                    if (alterConfigOperations.size() > 0) {
                        try {
                            Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>();
                            configs.put(topicConfigResource, alterConfigOperations);

                            AlterConfigsResult alterConfigsResult = adminClient.incrementalAlterConfigs(configs);
                            alterConfigsResult.all().get(this.operationTimeout, TimeUnit.SECONDS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new KafkaException("Interrupted while getting topic descriptions", ie);
                        } catch (ExecutionException | TimeoutException ex) {
                            throw new KafkaException("Failed to obtain topic descriptions", ex);
                        }
                    }
                }
            }

        }
    }

    private Map<String, NewPartitions> checkPartitions(Map<String, NewTopic> topicNameToTopic,
                                                       DescribeTopicsResult topicInfo, List<NewTopic> topicsToAdd) {

        Map<String, NewPartitions> topicsToModify = new HashMap<>();
        topicInfo.topicNameValues().forEach((n, f) -> {
            NewTopic topic = topicNameToTopic.get(n);
            try {
                TopicDescription topicDescription = f.get(this.operationTimeout, TimeUnit.SECONDS);
                if (topic.numPartitions() >= 0 && topic.numPartitions() < topicDescription.partitions().size()) {
                    log.info(String.format(
                            "Topic '%s' exists but has a different partition count: %d not %d", n,
                            topicDescription.partitions().size(), topic.numPartitions()));
                } else if (topic.numPartitions() > topicDescription.partitions().size()) {
                    log.info(String.format(
                            "Topic '%s' exists but has a different partition count: %d not %d, increasing "
                                    + "if the broker supports it", n,
                            topicDescription.partitions().size(), topic.numPartitions()));
                    topicsToModify.put(n, NewPartitions.increaseTo(topic.numPartitions()));
                }
            } catch (@SuppressWarnings("unused") InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (TimeoutException e) {
                throw new KafkaException("Timed out waiting to get existing topics", e);
            } catch (@SuppressWarnings("unused") ExecutionException e) {
                topicsToAdd.add(topic);
            }
        });
        return topicsToModify;
    }

    private void addTopics(AdminClient adminClient, List<NewTopic> topicsToAdd) {
        CreateTopicsResult topicResults = adminClient.createTopics(topicsToAdd);
        try {
            topicResults.all().get(this.operationTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for topic creation results", e);
        } catch (TimeoutException e) {
            throw new KafkaException("Timed out waiting for create topics results", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) { // Possible race with another app instance
                log.debug("Failed to create topics", e.getCause());
            } else {
                log.error("Failed to create topics", e.getCause());
                throw new KafkaException("Failed to create topics", e.getCause()); // NOSONAR
            }
        }
    }

    private void createMissingPartitions(AdminClient adminClient, Map<String, NewPartitions> topicsToModify) {
        CreatePartitionsResult partitionsResult = adminClient.createPartitions(topicsToModify);
        try {
            partitionsResult.all().get(this.operationTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for partition creation results", e);
        } catch (TimeoutException e) {
            throw new KafkaException("Timed out waiting for create partitions results", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InvalidPartitionsException) { // Possible race with another app instance
                log.debug("Failed to create partitions", e.getCause());
            } else {
                log.error("Failed to create partitions", e.getCause());
                if (!(e.getCause() instanceof UnsupportedVersionException)) {
                    throw new KafkaException("Failed to create partitions", e.getCause()); // NOSONAR
                }
            }
        }
    }

    /**
     * Wrapper for a collection of {@link NewTopic} to facilitate declaring multiple
     * topics as a single bean.
     */
    public static class NewTopics {

        private final Collection<NewTopic> newTopics = new ArrayList<>();

        /**
         * Construct an instance with the {@link NewTopic}s.
         *
         * @param newTopics the topics.
         */
        public NewTopics(NewTopic... newTopics) {
            this.newTopics.addAll(Arrays.asList(newTopics));
        }

        Collection<NewTopic> getNewTopics() {
            return this.newTopics;
        }

    }


}
