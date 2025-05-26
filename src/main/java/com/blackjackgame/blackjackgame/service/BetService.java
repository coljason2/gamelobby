package com.blackjackgame.blackjackgame.service;

import com.blackjackgame.blackjackgame.model.Bet;
import com.blackjackgame.blackjackgame.model.BetResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BetService {
    // 儲存每一期的所有下注紀錄：期數 -> List<下注>
    private final Map<Integer, List<Bet>> betsByIssue = new ConcurrentHashMap<>();


    public void placeBet(int issue, Bet bet) {
        log.info("[BetService] 期數:{} 下注；:{}", issue, bet);
        bet.setIssueNumber(issue);
        betsByIssue.computeIfAbsent(issue, k -> new ArrayList<>()).add(bet);
    }

    public List<BetResult> settleBets(int issue, List<Integer> winningNumbers) {
        List<Bet> currentBets = this.getBetsForIssue(issue);  // 正確取得當期下注
        List<BetResult> results = new ArrayList<>();

        if (Objects.nonNull(currentBets)) {
            for (Bet bet : currentBets) {
                boolean isWin = bet.getBets().stream()
                        .anyMatch(group -> winningNumbers.containsAll(group));
                bet.setWin(isWin);  // 更新中獎狀態

                BetResult result = new BetResult();
                result.setUser(bet.getUser());
                result.setNumbers(bet.getBets());
                result.setMatchCount(isWin ? 3 : 0); // 這裡 matchCount 是任意一組全中就給 3，可調整

                results.add(result);
            }
        }

        return results;
    }

    public List<Bet> getBetsForIssue(int issue) {
        return betsByIssue.getOrDefault(issue, Collections.emptyList());
    }

    public List<Bet> getBetsFromUser(String user) {
        return betsByIssue.values().stream()
                .flatMap(List::stream)
                .filter(bet -> user.equals(bet.getUser()))
                .collect(Collectors.toList());
    }
}
