package com.edutec.serving.controller;

import com.edutec.serving.models.DiscussionPostStatData;
import com.edutec.serving.models.dtos.DiscussionPostStatDataDto;
import com.edutec.serving.props.Resources;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiscussionPostStatController {


    private final String baseUrl = "mdl_forum_posts";
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private final Resources.StoreNameProvider storeNameProvider;


    @GetMapping(value = baseUrl + "/stat")
    public ResponseEntity<List<DiscussionPostStatDataDto>> getStat() {
        ReadOnlyKeyValueStore<String, DiscussionPostStatData> store = streamsBuilderFactoryBean
                .getKafkaStreams().store(storeNameProvider.getDiscussion_analytics_store(), QueryableStoreTypes.keyValueStore());
        KeyValueIterator<String, DiscussionPostStatData> all = store.all();
        List<DiscussionPostStatDataDto> allPosts = new ArrayList<>();
        while (all.hasNext()) {
            KeyValue<String, DiscussionPostStatData> next = all.next();
            allPosts.add(DiscussionPostStatDataDto.of(next.key, next.value));
        }
        return ResponseEntity.ok(allPosts);
    }

    @GetMapping("mdl_forum_posts/stat/{userid}")
    public ResponseEntity<DiscussionPostStatData> getStatForUser(@PathVariable("userid") String userId) {
        ReadOnlyKeyValueStore<String, DiscussionPostStatData> store = streamsBuilderFactoryBean
                .getKafkaStreams().store(storeNameProvider.getDiscussion_analytics_store(), QueryableStoreTypes.keyValueStore());
        return ResponseEntity.ok(store.get(userId.replace("_", " ")));
    }
}
