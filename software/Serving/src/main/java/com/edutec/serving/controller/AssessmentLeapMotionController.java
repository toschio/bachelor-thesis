package com.edutec.serving.controller;

import com.edutec.serving.models.AssessmentLeapMotionStatData;
import com.edutec.serving.models.dtos.AssessmentLeapMotionStatDataDto;
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
public class AssessmentLeapMotionController {


    private final String baseUrl = "leap_motion_assessment";
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private final Resources.StoreNameProvider storeNameProvider;


    @GetMapping(value = baseUrl + "/stat")
    public ResponseEntity<List<AssessmentLeapMotionStatDataDto>> getStat() {
        ReadOnlyKeyValueStore<String, AssessmentLeapMotionStatData> store = streamsBuilderFactoryBean
                .getKafkaStreams().store(storeNameProvider.getAssessment_analytics_store(), QueryableStoreTypes.keyValueStore());
        KeyValueIterator<String, AssessmentLeapMotionStatData> all = store.all();
        List<AssessmentLeapMotionStatDataDto> allPosts = new ArrayList<>();
        while (all.hasNext()) {
            KeyValue<String, AssessmentLeapMotionStatData> next = all.next();
            allPosts.add(AssessmentLeapMotionStatDataDto.of(next.key, next.value));
        }
        return ResponseEntity.ok(allPosts);
    }

    @GetMapping(value = baseUrl + "/stat/{userid}")
    public ResponseEntity<AssessmentLeapMotionStatDataDto> getStatForUser(@PathVariable("userid") String userId) {
        ReadOnlyKeyValueStore<String, AssessmentLeapMotionStatData> store = streamsBuilderFactoryBean
                .getKafkaStreams().store(storeNameProvider.getAssessment_analytics_store(), QueryableStoreTypes.keyValueStore());
        final String userIdMapped = userId.replace("_", " ");
        final AssessmentLeapMotionStatData assessmentLeapMotionStatData = store.get(userIdMapped);
        return ResponseEntity.ok(AssessmentLeapMotionStatDataDto.of(userIdMapped, assessmentLeapMotionStatData));
    }
}
