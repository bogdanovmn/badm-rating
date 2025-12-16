package com.github.bogdanovmn.badmrating.web.user.groups;

import jakarta.validation.constraints.NotBlank;
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
    List<UserGroupBrief> userGroupsBriefList(UUID userId) {
        return userGroupsRepository.userGroupsBriefList(userId);
    }

    @Transactional(readOnly = true)
    List<UserGroupBrief> userGroupsBriefListForPlayer(UUID userId, UUID playerId) {
        return userGroupsRepository.userGroupsBriefListForPlayer(userId, playerId);
    }

    @Transactional
    UserGroupBrief create(@NotBlank String name, UUID userId) {
        return UserGroupBrief.builder()
            .id(userGroupsRepository.create(name, userId))
            .name(name)
            .playersCount(0)
        .build();
    }

    @Transactional
    void delete(UUID groupId, UUID userId) {
        validateOrFail(userId, groupId);
        userGroupsRepository.delete(groupId);
    }

    @Transactional
    void addPlayer(UUID groupId, @NotNull UUID playerId, UUID userId) {
        validateOrFail(userId, groupId);
        userGroupsRepository.addPlayer(groupId, playerId);
    }

    @Transactional
    void removePlayer(UUID groupId, UUID playerId, UUID userId) {
        validateOrFail(userId, groupId);
        userGroupsRepository.removePlayer(groupId, playerId);
    }

    @Transactional(readOnly = true)
    List<UUID> groupPlayers(UUID groupId, UUID userId) {
        validateOrFail(userId, groupId);
        return userGroupsRepository.groupPlayers(groupId);
    }

    private void validateOrFail(UUID userId, UUID groupId) {
        if (!userGroupsRepository.belongsToUser(groupId, userId)) {
            throw new AccessDeniedException("Group not belongs to user");
        }
    }

    UserGroupBrief get(UUID groupId, UUID userId) {
        validateOrFail(userId, groupId);
        return userGroupsRepository.briefById(groupId);
    }
}
