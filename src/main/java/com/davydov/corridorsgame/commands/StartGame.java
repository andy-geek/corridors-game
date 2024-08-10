package com.davydov.corridorsgame.commands;

import java.io.Serializable;

public class StartGame implements Serializable {
    public boolean gameStatus;

    public String playerMove;

    public StartGame(boolean gameStatus, String playerMove) {
        this.gameStatus = gameStatus;
        this.playerMove = playerMove;
    }
}
