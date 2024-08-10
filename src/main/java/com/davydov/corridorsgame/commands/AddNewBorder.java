package com.davydov.corridorsgame.commands;

import javafx.geometry.HPos;
import javafx.geometry.VPos;

import java.io.Serializable;

public class AddNewBorder implements Serializable {
    public HPos hPos;
    public VPos vPos;

    public Integer columnIndex;
    public Integer rowIndex;

    public String playerMove;

    public AddNewBorder(HPos hPos, VPos vPos, Integer columnIndex, Integer rowIndex, String playerMove) {
        this.hPos = hPos;
        this.vPos = vPos;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.playerMove = playerMove;
    }
}
