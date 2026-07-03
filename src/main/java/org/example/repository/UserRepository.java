package org.example.repository;

import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Boolean existsUserByEmail(String email);

    Boolean existsUserByUsername(String username);

    // Containing/IgnoreCase 파생 쿼리는 H2에서 ESCAPE 절 처리 중 오류가 나서 직접 쿼리로 작성
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByUsernameOrDisplayName(@Param("keyword") String keyword);

}
