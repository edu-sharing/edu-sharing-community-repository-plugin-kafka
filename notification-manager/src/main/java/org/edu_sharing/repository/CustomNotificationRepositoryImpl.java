package org.edu_sharing.repository;

import lombok.RequiredArgsConstructor;
import org.edu_sharing.model.NotificationEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@RequiredArgsConstructor
public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public void removeUserName(String userId){
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("creator.id").is(userId)),
                new Update().unset("creator.displayName"),
                NotificationEvent.class);

        mongoTemplate.updateMulti(
                Query.query(Criteria.where("receiver.id").is(userId)),
                new Update().unset("receiver.displayName"),
                NotificationEvent.class);
    }
}