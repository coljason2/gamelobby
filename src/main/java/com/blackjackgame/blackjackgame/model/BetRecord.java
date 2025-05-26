package com.blackjackgame.blackjackgame.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BetRecord implements Serializable {
    private String user;
    private List<Integer> numbers;
}
