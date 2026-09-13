package io.github.yaforster.trails.adapter.db.user;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "service.api.security.oauth2.enabled", havingValue = "true")
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

}
