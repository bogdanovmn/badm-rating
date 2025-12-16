package com.github.bogdanovmn.badmrating.web.user.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserGroupsRepository {
    private final NamedParameterJdbcTemplate jdbc;

    List<UserGroupBrief> userGroupsBriefList(UUID userId) {
        return jdbc.query("""
            SELECT g.id, g.name, COUNT(gp.*) players_count
            FROM user_group g
            LEFT JOIN user_group_player gp ON gp.group_id = g.id
            WHERE g.user_id = :userId
            GROUP BY g.id
            ORDER BY g.name
        """,
            Map.of("userId", userId),
            (rs, rowNum) -> UserGroupBrief.builder()
                .id(UUID.fromString(rs.getString("id")))
                .name(rs.getString("name"))
                .playersCount(rs.getInt("players_count"))
            .build()
        );
    }

    List<UserGroupBrief> userGroupsBriefListForPlayer(UUID userId, UUID playerId) {
        return jdbc.query("""
            WITH available_groups AS (
                SELECT g.id, g.name
                FROM user_group g
                WHERE g.user_id = :userId
            ),
            player_groups AS (
                SELECT gp.group_id
                FROM user_group_player gp
                WHERE gp.player_id = :playerId
            )
            SELECT
                ag.id,
                ag.name,
                COUNT(ugp.player_id) AS players_count
            FROM available_groups ag
            LEFT JOIN player_groups pg ON pg.group_id = ag.id
            LEFT JOIN user_group_player ugp ON ugp.group_id = ag.id
            WHERE pg.group_id IS NULL
            GROUP BY ag.id, ag.name
            ORDER BY ag.name;
        """,
            Map.of(
                "userId", userId,
                "playerId", playerId
            ),
            (rs, rowNum) -> UserGroupBrief.builder()
                .id(UUID.fromString(rs.getString("id")))
                .name(rs.getString("name"))
                .playersCount(rs.getInt("players_count"))
            .build()
        );
    }

    List<UUID> groupPlayers(UUID groupId) {
        return jdbc.queryForList("""
            SELECT player_id
            FROM user_group_player
            WHERE group_id = :groupId
        """,
            Map.of("groupId", groupId),
            UUID.class
        );
    }

    UUID create(@NotBlank String name, UUID userId) {
        return jdbc.queryForObject(
            """
                INSERT INTO user_group (name, user_id)
                VALUES (:name, :userId)
                RETURNING id
            """,
            Map.of(
                "name", name,
                "userId", userId
            ),
            UUID.class
        );
    }

    void delete(UUID groupId) {
        jdbc.update(
            "DELETE FROM user_group WHERE id = :groupId",
            Map.of("groupId", groupId)
        );
    }

    void addPlayer(UUID groupId, @NotNull UUID playerId) {
        jdbc.update(
            "INSERT INTO user_group_player (group_id, player_id) VALUES (:groupId, :playerId)",
            Map.of(
                "groupId", groupId,
                "playerId", playerId
            )
        );
    }

    void removePlayer(UUID groupId, UUID playerId) {
        jdbc.update(
            "DELETE FROM user_group_player WHERE group_id = :groupId AND player_id = :playerId",
            Map.of("groupId", groupId, "playerId", playerId)
        );
    }

    boolean belongsToUser(UUID groupId, UUID userId) {
        Integer count = jdbc.queryForObject(
            """
                SELECT COUNT(*) FROM user_group
                WHERE id = :groupId AND user_id = :userId
            """,
            Map.of(
                "groupId", groupId,
                "userId", userId
            ),
            Integer.class
        );

        return count != null && count > 0;
    }

    UserGroupBrief briefById(UUID groupId) {
        return jdbc.queryForObject("""
            SELECT id, name, COUNT(gp.*) players_count
            FROM user_group
            LEFT JOIN user_group_player gp ON gp.group_id = user_group.id
            WHERE user_group.id = :groupId
            GROUP BY user_group.id
        """,
            Map.of("groupId", groupId),
            (rs, rowNum) -> UserGroupBrief.builder()
                .id(UUID.fromString(rs.getString("id")))
                .name(rs.getString("name"))
                .playersCount(rs.getInt("players_count"))
            .build()
        );
    }
}
