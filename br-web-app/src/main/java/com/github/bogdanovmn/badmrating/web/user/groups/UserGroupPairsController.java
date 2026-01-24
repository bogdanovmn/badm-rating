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
@RequestMapping("/groups/{groupId}/pairs")
@RequiredArgsConstructor
@Slf4j
class UserGroupPairsController {
    private final UserGroupsService userGroupsService;

    @GetMapping
    List<UUID[]> get(@PathVariable("groupId") UUID id, @CurrentUserId UUID userId) {
        return userGroupsService.groupPairs(id, userId);
    }

    @PostMapping
    void addPair(
        @PathVariable("groupId") UUID id,
        @Valid @RequestBody PairRequest request,
        @CurrentUserId UUID userId
    ) {
        userGroupsService.addPair(id, request.getPair());
    }

    @DeleteMapping
    void removePair(
        @PathVariable("groupId") UUID id,
        @RequestParam("playerId") UUID playerId,
        @CurrentUserId UUID userId
    ) {
        userGroupsService.removePair(id, playerId);
    }
}
