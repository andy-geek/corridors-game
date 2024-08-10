package com.davydov.corridorsgame.commands;

import java.io.Serializable;

public class StopGame implements Serializable {
    public boolean gameStatus;

    public String winningPlayer;

    public StopGame(boolean gameStatus, String winningPlayer) {
        this.gameStatus = gameStatus;
        this.winningPlayer = winningPlayer;
    }
}
