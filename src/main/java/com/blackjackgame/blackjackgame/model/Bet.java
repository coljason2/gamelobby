package com.blackjackgame.blackjackgame.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class Bet implements Serializable {
    private String user;
    private List<Set<Integer>> bets; // 每人最多下注3組，每組3個號碼
    private LocalDateTime time = LocalDateTime.now();
}
