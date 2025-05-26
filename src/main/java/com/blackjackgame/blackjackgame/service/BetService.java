package com.blackjackgame.blackjackgame.service;

import com.blackjackgame.blackjackgame.model.Bet;
import com.blackjackgame.blackjackgame.model.BetResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BetService {
    // 儲存每一期的所有下注紀錄：期數 -> List<下注>
    private final Map<Integer, List<Bet>> betsByIssue = new ConcurrentHashMap<>();


    public void placeBet(int issue, Bet bet) {
        log.info("[BetService] 期數:{} 下注；:{}", issue, bet);
        betsByIssue.computeIfAbsent(issue, k -> new ArrayList<>()).add(bet);
    }

    public List<BetResult> settleBets(int issue, List<Integer> winningNumbers) {
        List<Bet> currentBets = this.getBetsForIssue(issue);  // 正確取得當期下注

        List<BetResult> results = new ArrayList<>();

        if (Objects.nonNull(currentBets)) {
            for (Bet bet : currentBets) {
                long matchCount = bet.getBets().stream()
                        .filter(winningNumbers::contains)
                        .count();

                BetResult result = new BetResult();
                result.setUser(bet.getUser());
                result.setNumbers(bet.getBets());
                result.setMatchCount((int) matchCount);

                results.add(result);
            }
        }

        this.clearBetsForIssue(issue); // 清除已結算的資料
        return results;
    }

    public List<Bet> getBetsForIssue(int issue) {
        return betsByIssue.getOrDefault(issue, Collections.emptyList());
    }

    public void clearBetsForIssue(int issue) {
        betsByIssue.remove(issue);
    }
}
