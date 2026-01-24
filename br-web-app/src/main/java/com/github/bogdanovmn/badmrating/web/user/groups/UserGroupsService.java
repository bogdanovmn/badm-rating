package com.github.bogdanovmn.badmrating.web.user.groups;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class UserGroupsService {
    private final UserGroupsRepository userGroupsRepository;

    @Transactional(readOnly = true)
    public List<UserGroupBrief> userGroupsBriefList(UUID userId) {
        return userGroupsRepository.userGroupsBriefList(userId);
    }

    @Transactional(readOnly = true)
    public List<UserGroupBrief> userGroupsBriefListForPlayer(UUID userId, UUID playerId) {
        return userGroupsRepository.userGroupsBriefListForPlayer(userId, playerId);
    }

    @Transactional
    UserGroupBrief create(String name, UUID userId) {
        return UserGroupBrief.builder()
            .id(userGroupsRepository.create(name, userId))
            .name(name)
            .playersCount(0)
        .build();
    }

    @Transactional
    public void delete(UUID groupId, UUID userId) {
        validateOrFail(userId, groupId);
        userGroupsRepository.delete(groupId);
    }

    @Transactional
    public void addPlayer(UUID groupId, @NotNull UUID playerId, UUID userId) {
        validateOrFail(userId, groupId);
        userGroupsRepository.addPlayer(groupId, playerId);
    }

    @Transactional
    public void removePlayer(UUID groupId, UUID playerId, UUID userId) {
        validateOrFail(userId, groupId);
        userGroupsRepository.removePlayer(groupId, playerId);
    }

    @Transactional(readOnly = true)
    public List<UUID> groupPlayers(UUID groupId, UUID userId) {
        validateOrFail(userId, groupId);
        return userGroupsRepository.groupPlayers(groupId);
    }

    @Transactional(readOnly = true)
    public UserGroupBrief get(UUID groupId, UUID userId) {
        validateOrFail(userId, groupId);
        return userGroupsRepository.briefById(groupId);
    }

    @Transactional(readOnly = true)
    public List<UUID[]> groupPairs(UUID groupId, UUID userId) {
        validateOrFail(userId, groupId);
        return userGroupsRepository.groupPairs(groupId);
    }

    private void validateOrFail(UUID userId, UUID groupId) {
        if (!userGroupsRepository.belongsToUser(groupId, userId)) {
            throw new AccessDeniedException("Group not belongs to user");
        }
    }

    @Transactional
    public void addPair(UUID groupId, List<UUID> pair) {
        for (UUID playerId : pair) {
            if (userGroupsRepository.isPairExistsForPlayer(groupId, playerId)) {
                throw new IllegalArgumentException("Pair already exists for player %s".formatted(playerId));
            }
        }
        userGroupsRepository.addPair(groupId, pair.get(0), pair.get(1));
    }

    @Transactional
    public void removePair(UUID groupId, UUID playerId) {
        if (!userGroupsRepository.isPairExistsForPlayer(groupId, playerId)) {
            throw new IllegalArgumentException("Pair not exists for player %s".formatted(playerId));
        }
        userGroupsRepository.removePair(groupId, playerId);
    }
}
