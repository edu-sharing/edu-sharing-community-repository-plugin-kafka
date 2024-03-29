package org.edu_sharing.repository;

import lombok.RequiredArgsConstructor;
import org.edu_sharing.messages.BaseMessage;
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
                new Query().addCriteria(Criteria.where("creator.id").is(userId)),
                new Update().unset("creator.displayName"),
                BaseMessage.class);

        mongoTemplate.updateMulti(
                new Query().addCriteria(Criteria.where("receiver.id").is(userId)),
                new Update().unset("receiver.displayName"),
                BaseMessage.class);
    }
}
