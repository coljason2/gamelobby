package com.blackjackgame.blackjackgame.controller;

import com.blackjackgame.blackjackgame.model.Bet;
import com.blackjackgame.blackjackgame.model.DrawResult;
import com.blackjackgame.blackjackgame.service.BetService;
import com.blackjackgame.blackjackgame.service.DrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/lotto")
public class LottoController {

    @Autowired
    private BetService betService;

    @Autowired
    private DrawService drawService;

    // 下注 API
    @PostMapping("/bet")
    public String placeBet(@RequestBody Bet bet) {
        int currentIssue = drawService.getCurrentIssue();
        betService.placeBet(currentIssue, bet);
        return "下注成功！目前期數：" + currentIssue;
    }

    // 查詢目前期數與倒數秒數
    @GetMapping("/current-time")
    public Map<String, Object> getCurrentInfo() {
        int issue = drawService.getCurrentIssue();
        long countdown = drawService.getSecondsUntilNextDraw();
        Map<String, Object> map = new HashMap<>();
        map.put("issue", issue);
        map.put("countdown", countdown);
        map.put("lastDrawNumbers", getRecentResults(1).get(0).getNumbers());
        return map;
    }

    // 查詢某一期結果
    @GetMapping("/result/{issue}")
    public DrawResult getResult(@PathVariable int issue) {
        return drawService.getDrawResult(issue);
    }

    // 查詢最近 N 期結果
    @GetMapping("/recent")
    public List<DrawResult> getRecentResults(@RequestParam(defaultValue = "10") int count) {
        return drawService.getRecentResults(count);
    }

    @GetMapping("/user-bets")
    public List<Bet> getUserBets(@RequestParam String user, @RequestParam int issue) {
        return betService.getBetsForIssue(issue).stream()
                .filter(b -> user.equals(b.getUser()))
                .collect(Collectors.toList());
    }

    @GetMapping("/check-win")
    public boolean checkIfUserWon(@RequestParam String user, @RequestParam int issue) {
        DrawResult result = drawService.getDrawResult(issue);
        if (result == null) return false;

        List<Integer> winning = result.getNumbers();
        return betService.getBetsForIssue(issue).stream()
                .filter(b -> user.equals(b.getUser()))
                .anyMatch(b -> winning.containsAll(b.getBets()));
    }

    @GetMapping("/my-bets")
    public ResponseEntity<List<Bet>> getMyBets(@RequestParam String user) {
        log.info("[my-bets] user :{}", user);
        List<Bet> list = betService.getBetsFromUser(user);
        log.info("[my-bets] list :{}", list);
        return ResponseEntity.ok(list);
    }
}
