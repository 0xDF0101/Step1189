package org.example.repository;

import org.example.entity.Group;
import org.example.entity.GroupGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupGoalRepository extends JpaRepository<GroupGoal, Long> {
    Optional<GroupGoal> findByGroup(Group group);
    void deleteByGroup(Group group);
}
