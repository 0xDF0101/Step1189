package org.example.repository;

import org.example.entity.Follow;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowing(User following);

    int countByFollower(User follower);

    int countByFollowing(User following);

    void deleteByFollowerAndFollowing(User follower, User following);

    void deleteByFollower(User user);

    void deleteByFollowing(User user);
}
