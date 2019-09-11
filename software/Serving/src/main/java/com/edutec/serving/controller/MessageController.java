package com.edutec.serving.controller;

import com.edutec.serving.models.DiscussionPostStatData;
import com.edutec.serving.models.dtos.DiscussionPostStatDataDto;
import com.edutec.serving.props.Resources;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final Log logger = LogFactory.getLog(MessageController.class);
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private final Resources.StoreNameProvider storeNameProvider;

    // todo is this necessary

    @SubscribeMapping("*") // mdl_forum_posts/stat")
    public List<DiscussionPostStatDataDto> getInitialSocketData() {
        logger.info("Initial data sending....");
        ReadOnlyKeyValueStore<String, DiscussionPostStatData> store = streamsBuilderFactoryBean
                .getKafkaStreams().store(storeNameProvider.getDiscussion_analytics_store(), QueryableStoreTypes.keyValueStore());
        KeyValueIterator<String, DiscussionPostStatData> all = store.all();
        List<DiscussionPostStatDataDto> allPosts = new ArrayList<>();
        while (all.hasNext()) {
            KeyValue<String, DiscussionPostStatData> next = all.next();
            allPosts.add(DiscussionPostStatDataDto.of(next.key, next.value));
        }
        return allPosts;
    }
}
