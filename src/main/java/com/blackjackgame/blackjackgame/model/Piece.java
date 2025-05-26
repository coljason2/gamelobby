package com.blackjackgame.blackjackgame.model;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class Piece implements Serializable {

    private static final long serialVersionUID = 5555566441L;

    private String name;      // 將, 士, 象, 車, 馬, 炮, 卒
    private String color;     // "red" or "black"
    private boolean revealed; // 是否已翻開
    private int x, y;         // 在棋盤的位置
}
