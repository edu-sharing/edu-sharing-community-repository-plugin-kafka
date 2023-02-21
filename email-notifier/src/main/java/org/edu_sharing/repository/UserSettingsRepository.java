package org.edu_sharing.repository;

import org.edu_sharing.domain.UserSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingsRepository extends MongoRepository<UserSettings, String> {
}
