package com.davydov.corridorsgame.commands;

import java.io.Serializable;

public class AddNewSymbol implements Serializable {
    public String playerSymbol;
    public Integer columnIndex;
    public Integer rowIndex;

    public AddNewSymbol(String playerSymbol, Integer columnIndex, Integer rowIndex) {
        this.playerSymbol = playerSymbol;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
    }
}
