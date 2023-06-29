package org.edu_sharing.notification.repository;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<NotificationEvent> findAll(String receiverId, List<Status> statusList, Pageable paging) {
        Query query = new Query().with(paging);
        if (StringUtils.isNoneBlank(receiverId)) {
            query.addCriteria(Criteria.where("receiverId").is(receiverId));
        }

        if(statusList != null) {
            Criteria statusCriteria = new Criteria();
            statusCriteria.orOperator(statusList.stream()
                    .distinct()
                    .map(x -> Criteria.where("status").is(x)).collect(Collectors.toList()));
            query.addCriteria(statusCriteria);
        }

        List<NotificationEvent> results = mongoTemplate.find(query, NotificationEvent.class);

        return PageableExecutionUtils.getPage(results, Pageable.unpaged(),
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), NotificationEvent.class));
    }

    @Override
    public UpdateResult updateStatusByReceiverId(String receiverId, List<Status> oldStatus, Status newStatus) {
        return mongoTemplate.update(NotificationEvent.class)
                .matching(Query.query(Criteria.where("receiverId").is(receiverId).and("status").in(oldStatus)))
                .apply(Update.update("status", newStatus))
                .all();
    }


}
