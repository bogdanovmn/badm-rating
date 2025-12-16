package com.github.bogdanovmn.badmrating.web.top;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import com.github.bogdanovmn.badmrating.web.common.domain.TopType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/top")
@RequiredArgsConstructor
@Slf4j
class TopPlayersController {
    private final TopPlayersService topPlayersService;

    @GetMapping("{topType}")
    List<PlayerTopPosition> getPlayersTop(
        @PathVariable("topType") TopType topType,
        @RequestParam("source") Source source,
        @RequestParam("playType") PlayType playType
    ) {
        return topPlayersService.playersTop(source, playType, topType);
    }

    @GetMapping("{topType}/context/{playerId}")
    List<PlayerTopPosition> getPlayersTopContext(
        @PathVariable("topType") TopType topType,
        @PathVariable("playerId") UUID playerId,
        @RequestParam("source") Source source,
        @RequestParam("playType") PlayType playType
    ) {
        return topPlayersService.playerTopContext(playerId, source, playType, topType);
    }

    @GetMapping("{topType}/position-history")
    Map<LocalDate, Integer> getPlayerTopPositionHistory(
        @PathVariable("topType") TopType topType,
        @RequestParam("playerId") UUID playerId,
        @RequestParam("source") Source source,
        @RequestParam("playType") PlayType playType
    ) {
        return topPlayersService.playerTopPositionHistory(playerId, source, playType, topType);
    }
}
