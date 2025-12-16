package com.github.bogdanovmn.badmrating.web.user.groups;

import com.github.bogdanovmn.badmrating.web.infrastructure.config.security.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/groups")
@RequiredArgsConstructor
@Slf4j
class UserGroupsController {
    private final UserGroupsService userGroupsService;

    @GetMapping("{id}")
    UserGroupBrief get(@PathVariable("id") UUID id, @CurrentUserId UUID userId) {
        return userGroupsService.get(id, userId);
    }
    @GetMapping
    List<UserGroupBrief> briefList(
        @RequestParam(name = "playerId", required = false) UUID playerId,
        @CurrentUserId UUID userId
    ) {
        return playerId == null
            ? userGroupsService.userGroupsBriefList(userId)
            : userGroupsService.userGroupsBriefListForPlayer(userId, playerId);
    }

    @PostMapping
    UserGroupBrief create(@Valid @RequestBody NewGroupRequest request, @CurrentUserId UUID userId) {
        return userGroupsService.create(request.getName(), userId);
    }

    @DeleteMapping("{id}")
    void delete(@PathVariable("id") UUID id, @CurrentUserId UUID userId) {
        userGroupsService.delete(id, userId);
    }

    @PostMapping("{id}/players")
    void addPlayer(
        @Valid @RequestBody NewPlayerRequest request,
        @PathVariable("id") UUID groupId,
        @CurrentUserId UUID userId
    ) {
        userGroupsService.addPlayer(groupId, request.getPlayerId(), userId);
    }

    @GetMapping("{id}/players")
    List<UUID> groupPlayers(
        @PathVariable("id") UUID groupId,
        @CurrentUserId UUID userId
    ) {
        return userGroupsService.groupPlayers(groupId, userId);
    }

    @DeleteMapping("{id}/players/{playerId}")
    void removePlayer(
        @PathVariable("id") UUID groupId,
        @PathVariable("playerId") UUID playerId,
        @CurrentUserId UUID userId
    ) {
        userGroupsService.removePlayer(groupId, playerId, userId);
    }
}
