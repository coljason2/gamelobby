package com.blackjackgame.blackjackgame.model;


import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class BetResult implements Serializable {
    private String user;
    private List<Set<Integer>> numbers;
    private int matchCount;

}
