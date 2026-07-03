package org.example.dto.poke;

import java.time.LocalDateTime;

public record PokeResponse(
        Long senderId,
        String senderUsername,
        String senderDisplayName,
        LocalDateTime createdAt
) {}
