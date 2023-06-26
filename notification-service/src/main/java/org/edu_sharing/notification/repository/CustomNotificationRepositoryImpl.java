package org.edu_sharing.notification.repository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.notification.model.NotificationEvent;
import org.edu_sharing.service.notification.events.data.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Arrays;
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
}
