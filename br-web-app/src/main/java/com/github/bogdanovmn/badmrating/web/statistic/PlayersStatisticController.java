package com.github.bogdanovmn.badmrating.web.statistic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players/statistic")
@RequiredArgsConstructor
@Slf4j
class PlayersStatisticController {
    private final PlayersStatisticService playersStatisticService;

    @GetMapping("top")
    List<TopStatistic> getPlayersTop() {
        return playersStatisticService.getPlayersTop();
    }

    @GetMapping("actual-top")
    List<TopStatistic> getPlayersActualTop() {
        return playersStatisticService.getPlayersActualTop();
    }
}
