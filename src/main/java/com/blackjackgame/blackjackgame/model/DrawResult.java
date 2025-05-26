package com.blackjackgame.blackjackgame.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class DrawResult implements Serializable {
    private int issueNumber; // 期數（新增）
    private Set<Integer> winningNumbers;
    private LocalDateTime drawTime;
    private List<String> winners; // 符合中獎條件的用戶名單
    private List<Integer> numbers;
}
