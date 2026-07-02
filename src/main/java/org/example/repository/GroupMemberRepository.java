package org.example.repository;

import org.example.entity.Group;
import org.example.entity.GroupMember;
import org.example.entity.User;
import org.example.model.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroup(Group group);

    List<GroupMember> findByUser(User user);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.user.id = :userId")
    Optional<GroupMember> findByGroupAndUserId(@Param("group") Group group, @Param("userId") Long userId);

    boolean existsByGroupAndUser(Group group, User user);

    int countByGroup(Group group);

    void deleteByGroup(Group group);

    void deleteByUser(User user);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.role != :role")
    List<GroupMember> findByGroupAndRoleNot(@Param("group") Group group, @Param("role") GroupRole role);
}
