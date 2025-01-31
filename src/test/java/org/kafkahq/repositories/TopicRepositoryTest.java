package org.kafkahq.repositories;

import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kafkahq.AbstractTest;
import org.kafkahq.KafkaClusterExtension;
import org.kafkahq.KafkaTestCluster;
import org.kafkahq.models.Config;
import org.kafkahq.models.Partition;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(KafkaClusterExtension.class)
public class TopicRepositoryTest extends AbstractTest {
    @Inject
    protected TopicRepository topicRepository;

    @Inject
    protected ConfigRepository configRepository;

    @Test
    public void list() throws ExecutionException, InterruptedException {
        assertEquals(14, topicRepository.list(KafkaTestCluster.CLUSTER_ID, TopicRepository.TopicListView.ALL, Optional.empty()).size());
    }

    @Test
    public void listNoInternal() throws ExecutionException, InterruptedException {
        assertEquals(9, topicRepository.list(KafkaTestCluster.CLUSTER_ID, TopicRepository.TopicListView.HIDE_INTERNAL, Optional.empty()).size());
    }

    @Test
    public void listNoInternalStream() throws ExecutionException, InterruptedException {
        assertEquals(7, topicRepository.list(KafkaTestCluster.CLUSTER_ID, TopicRepository.TopicListView.HIDE_INTERNAL_STREAM, Optional.empty()).size());
    }

    @Test
    public void listNoStream() throws ExecutionException, InterruptedException {
        assertEquals(12, topicRepository.list(KafkaTestCluster.CLUSTER_ID, TopicRepository.TopicListView.HIDE_STREAM, Optional.empty()).size());
    }

    @Test
    public void search() throws ExecutionException, InterruptedException {
        assertEquals(1, topicRepository.list(KafkaTestCluster.CLUSTER_ID, TopicRepository.TopicListView.ALL, Optional.of("ra do")).size());
    }

    @Test
    public void create() throws ExecutionException, InterruptedException {
        topicRepository.create(KafkaTestCluster.CLUSTER_ID, "create", 8, (short) 1, Collections.singletonList(
            new Config(TopicConfig.SEGMENT_MS_CONFIG, "1000")
        ));

        Optional<String> option = configRepository.findByTopic(KafkaTestCluster.CLUSTER_ID, "create")
            .stream()
            .filter(r -> r.getName().equals(TopicConfig.SEGMENT_MS_CONFIG))
            .findFirst()
            .map(Config::getValue);

        assertEquals(8, topicRepository.findByName(KafkaTestCluster.CLUSTER_ID, "create").getPartitions().size());
        assertEquals("1000", option.get());

        topicRepository.delete(KafkaTestCluster.CLUSTER_ID, "create");
    }

    @Test
    public void offset() throws ExecutionException, InterruptedException {
        Optional<Partition> compacted = topicRepository
            .findByName(KafkaTestCluster.CLUSTER_ID, KafkaTestCluster.TOPIC_COMPACTED)
            .getPartitions()
            .stream()
            .filter(partition -> partition.getId() == 0)
            .findFirst();

        assertTrue(compacted.isPresent());
        assertEquals(0, compacted.get().getFirstOffset());
        assertEquals(100, compacted.get().getLastOffset());
    }

    @Test
    public void partition() throws ExecutionException, InterruptedException {
        assertEquals(3, topicRepository.findByName(KafkaTestCluster.CLUSTER_ID, KafkaTestCluster.TOPIC_COMPACTED).getPartitions().size());
    }
}