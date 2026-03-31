package com.dduru.gildongmu.user.repository;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthIdAndOauthType(String oauthId, OauthType oauthType);
    boolean existsByOauthIdAndOauthType(String oauthId, OauthType oauthType);
    boolean existsByEmail(String email);

    @Query(
            value = "SELECT u FROM User u LEFT JOIN FETCH u.profile",
            countQuery = "SELECT COUNT(u) FROM User u"
    )
    Page<User> findAllWithProfile(Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findWithProfileById(@Param("id") Long id);

    default User getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(UserNotFoundException::new);
    }
}
