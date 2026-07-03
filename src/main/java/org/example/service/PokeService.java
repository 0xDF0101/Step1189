package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.poke.PokeResponse;
import org.example.entity.Poke;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.repository.PokeRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PokeService {

    private final PokeRepository pokeRepository;
    private final UserRepository userRepository;
    private final FollowService followService;

    @Transactional
    public void poke(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalStateException("자기 자신을 찌를 수 없습니다.");
        }

        User sender = userRepository.getReferenceById(userId);
        User receiver = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("찌르려는 유저를 찾을 수 없습니다."));

        if (!followService.isMutualFollow(userId, targetUserId)) {
            throw new IllegalStateException("맞팔로우 상태에서만 콕 찌르기가 가능합니다.");
        }

        pokeRepository.save(new Poke(sender, receiver));
    }

    public List<PokeResponse> getReceivedPokes(Long userId) {
        User user = userRepository.getReferenceById(userId);
        return pokeRepository.findByReceiverOrderByCreatedAtDesc(user).stream()
                .map(p -> new PokeResponse(
                        p.getSender().getId(),
                        p.getSender().getUsername(),
                        p.getSender().getDisplayName(),
                        p.getCreatedAt()))
                .toList();
    }
}
