package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.group.*;
import org.example.dto.progress.DailyProgressDto;
import org.example.entity.*;
import org.example.exception.EntityNotFoundException;
import org.example.model.GroupRole;
import org.example.repository.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupGoalRepository groupGoalRepository;
    private final UserRepository userRepository;
    private final DailyProgressRepository dailyProgressRepository;

    @Transactional
    public Long createGroup(Long userId, GroupCreateRequest request) {
        User user = userRepository.getReferenceById(userId);
        String inviteCode = generateUniqueInviteCode();

        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .inviteCode(inviteCode)
                .maxMembers(request.maxMembers())
                .createdBy(user)
                .build();

        groupRepository.save(group);
        groupMemberRepository.save(new GroupMember(group, user, GroupRole.LEADER));

        return group.getId();
    }

    @Transactional
    public Long joinGroup(Long userId, GroupJoinRequest request) {
        Group group = groupRepository.findByInviteCode(request.inviteCode().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 초대 코드입니다."));

        User user = userRepository.getReferenceById(userId);

        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("이미 참여 중인 그룹입니다.");
        }

        int currentCount = groupMemberRepository.countByGroup(group);
        if (currentCount >= group.getMaxMembers()) {
            throw new IllegalStateException("그룹 인원이 꽉 찼습니다.");
        }

        groupMemberRepository.save(new GroupMember(group, user, GroupRole.MEMBER));
        return group.getId();
    }

    public List<GroupSummaryResponse> getMyGroups(Long userId) {
        User user = userRepository.getReferenceById(userId);
        return groupMemberRepository.findByUser(user).stream()
                .map(gm -> new GroupSummaryResponse(
                        gm.getGroup().getId(),
                        gm.getGroup().getName(),
                        gm.getGroup().getDescription(),
                        gm.getGroup().getInviteCode(),
                        groupMemberRepository.countByGroup(gm.getGroup()),
                        gm.getGroup().getMaxMembers(),
                        gm.getRole().name()
                ))
                .toList();
    }

    public GroupDetailResponse getGroupDetail(Long groupId, Long currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));

        GroupMember currentMember = groupMemberRepository
                .findByGroupAndUserId(group, currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("그룹 멤버가 아닙니다."));

        List<GroupMember> members = groupMemberRepository.findByGroup(group);

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate today = LocalDate.now();

        List<MemberRankingDto> totalRankings = members.stream()
                .map(m -> new MemberRankingDto(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        dailyProgressRepository.sumAllChaptersByUserId(m.getUser().getId())
                ))
                .sorted(Comparator.comparingInt(MemberRankingDto::value).reversed())
                .toList();

        List<MemberRankingDto> weeklyRankings = members.stream()
                .map(m -> new MemberRankingDto(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        dailyProgressRepository.sumChaptersInPeriod(m.getUser().getId(), weekStart, today)
                ))
                .sorted(Comparator.comparingInt(MemberRankingDto::value).reversed())
                .toList();

        List<MemberRankingDto> streakRankings = members.stream()
                .map(m -> new MemberRankingDto(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        calculateStreak(m.getUser().getId())
                ))
                .sorted(Comparator.comparingInt(MemberRankingDto::value).reversed())
                .toList();

        GroupGoalDto goalDto = groupGoalRepository.findByGroup(group)
                .map(goal -> {
                    List<MemberGoalProgressDto> progress = members.stream()
                            .map(m -> {
                                int read = dailyProgressRepository.sumChaptersInPeriod(
                                        m.getUser().getId(), goal.getStartDate(), goal.getEndDate());
                                int target = goal.getTargetChaptersPerPerson();
                                double pct = target > 0 ? Math.min((double) read / target * 100, 100.0) : 0.0;
                                return new MemberGoalProgressDto(
                                        m.getUser().getId(), m.getUser().getUsername(), read, target, pct);
                            })
                            .sorted(Comparator.comparingDouble(MemberGoalProgressDto::percent).reversed())
                            .toList();
                    return new GroupGoalDto(goal.getStartDate(), goal.getEndDate(),
                            goal.getTargetChaptersPerPerson(), progress);
                })
                .orElse(null);

        List<GroupMemberDto> memberDtos = members.stream()
                .map(m -> new GroupMemberDto(
                        m.getUser().getId(), m.getUser().getUsername(),
                        m.getRole().name(), m.getJoinedAt()))
                .toList();

        return new GroupDetailResponse(
                group.getId(), group.getName(), group.getDescription(),
                group.getInviteCode(), members.size(), group.getMaxMembers(),
                currentMember.getRole() == GroupRole.LEADER,
                totalRankings, weeklyRankings, streakRankings, goalDto, memberDtos
        );
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));

        GroupMember member = groupMemberRepository.findByGroupAndUserId(group, userId)
                .orElseThrow(() -> new EntityNotFoundException("그룹 멤버가 아닙니다."));

        int memberCount = groupMemberRepository.countByGroup(group);

        if (member.getRole() == GroupRole.LEADER) {
            if (memberCount == 1) {
                groupGoalRepository.deleteByGroup(group);
                groupMemberRepository.deleteByGroup(group);
                groupRepository.delete(group);
                return;
            }
            // 리더가 나가면 가장 먼저 가입한 다른 멤버에게 리더 이양
            groupMemberRepository.findByGroupAndRoleNot(group, GroupRole.LEADER)
                    .stream()
                    .min(Comparator.comparing(GroupMember::getJoinedAt))
                    .ifPresent(GroupMember::promoteToLeader);
        }

        groupMemberRepository.delete(member);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));

        groupMemberRepository.findByGroupAndUserId(group, userId)
                .filter(m -> m.getRole() == GroupRole.LEADER)
                .orElseThrow(() -> new IllegalStateException("그룹 리더만 삭제할 수 있습니다."));

        groupGoalRepository.deleteByGroup(group);
        groupMemberRepository.deleteByGroup(group);
        groupRepository.delete(group);
    }

    @Transactional
    public void setGroupGoal(Long groupId, Long userId, GroupGoalRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalStateException("종료일은 시작일 이후여야 합니다.");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));

        groupMemberRepository.findByGroupAndUserId(group, userId)
                .filter(m -> m.getRole() == GroupRole.LEADER)
                .orElseThrow(() -> new IllegalStateException("그룹 리더만 목표를 설정할 수 있습니다."));

        groupGoalRepository.findByGroup(group)
                .ifPresentOrElse(
                        goal -> goal.update(request.targetChaptersPerPerson(), request.startDate(), request.endDate()),
                        () -> groupGoalRepository.save(new GroupGoal(group,
                                request.targetChaptersPerPerson(), request.startDate(), request.endDate()))
                );
    }

    @Transactional
    public void handleUserDeletion(Long userId) {
        User user = userRepository.getReferenceById(userId);
        groupMemberRepository.deleteByUser(user);
    }

    private int calculateStreak(Long userId) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> readDates = dailyProgressRepository
                .getDailyReadingStats(userId, today.minusYears(1))
                .stream()
                .map(DailyProgressDto::readDate)
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate check = readDates.contains(today) ? today : today.minusDays(1);
        while (readDates.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        return streak;
    }

    private String generateUniqueInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
            code = sb.toString();
        } while (groupRepository.existsByInviteCode(code));
        return code;
    }
}
