package org.example.user.dto;

import org.example.entity.Progress;
import org.example.entity.User;
import org.example.model.Role;

import java.time.LocalDateTime;
import java.util.List;

public record UserInfo(
        String nickname,
        List<Progress> progressList,
        String email,
        Role role,
        Boolean publicStatus,
        String statusMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserInfo(User user) {
        this(
                user.getNickname(),
                user.getProgressList(),
                user.getEmail(),
                user.getRole(),
                user.getPublicStatus(),
                user.getStatusMessage(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
