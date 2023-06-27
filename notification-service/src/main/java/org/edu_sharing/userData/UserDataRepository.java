package org.edu_sharing.userData;

import org.apache.catalina.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface UserDataRepository extends MongoRepository<UserData, String> {
    List<UserData> findByIdIn(List<String> ids);

    default Map<String, UserData> findByIdInAsMap(List<String> ids){
        return findByIdIn(ids).stream().collect(Collectors.toMap(UserData::getId, x->x));
    }
}
