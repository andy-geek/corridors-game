package com.davydov.corridorsgame.server;

import com.davydov.corridorsgame.commands.AddNewBorder;
import com.davydov.corridorsgame.commands.AddNewSymbol;
import com.davydov.corridorsgame.commands.StartGame;
import com.davydov.corridorsgame.commands.StopGame;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer extends Thread {
    final Integer FIELD_SIZE = 10;
    final Integer NUM_SIDES = 4;

    Integer serverPort = 8080;
    ArrayList<PlayerConnection> players = new ArrayList<>();

    String[][] gameField = new String[FIELD_SIZE][FIELD_SIZE];
    String[][][] gameFieldBorders = new String[FIELD_SIZE][FIELD_SIZE][NUM_SIDES];

    public void startGame() {
        try {
            players.get(0).objectOutputStream.writeObject(new StartGame(true, "X"));
            players.get(1).objectOutputStream.writeObject(new StartGame(true, "X"));
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    public void stopGame(String winningPlayer) {
        try {
            players.get(0).objectOutputStream.writeObject(new StopGame(false, winningPlayer));
            players.get(1).objectOutputStream.writeObject(new StopGame(false, winningPlayer));
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    public void checkEndGame() {
        int numX = 0;
        int numO = 0;

        boolean result = true;
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                if (gameField[i][j] != null) {
                    if (gameField[i][j].equals("X")) {
                        numX++;
                    } else if (gameField[i][j].equals("O")) {
                        numO++;
                    }
                } else {
                    result = false;
                    break;
                }
            }
        }

        if (result) {
            if (numX > numO) {
                stopGame("X");
            } else if (numX < numO) {
                stopGame("O");
            } else {
                stopGame("D");
            }
        }
    }

    public boolean checkNewBorder(AddNewBorder newBorder) {
        boolean result = true;

        int rowIndex = newBorder.rowIndex;
        int columnIndex = newBorder.columnIndex;

        if (newBorder.hPos == HPos.LEFT) {
            result = gameFieldBorders[rowIndex][columnIndex][0] == null;
        } else if (newBorder.vPos == VPos.BOTTOM) {
            result = gameFieldBorders[rowIndex][columnIndex][1] == null;
        } else if (newBorder.vPos == VPos.TOP) {
            result = gameFieldBorders[rowIndex][columnIndex][2] == null;
        } else if (newBorder.hPos == HPos.RIGHT) {
            result = gameFieldBorders[rowIndex][columnIndex][3] == null;
        }

        return result;
    }

    public void addNewBorder(AddNewBorder newBorder) {
        String playerMove = newBorder.playerMove;
        newBorder.playerMove = (playerMove.equals("X")) ? "O" : "X";
        try {
            players.get(0).objectOutputStream.writeObject(newBorder);
            players.get(1).objectOutputStream.writeObject(newBorder);

            if (newBorder.hPos == HPos.LEFT) {
                gameFieldBorders[newBorder.rowIndex][newBorder.columnIndex][0] = "LEFT";
                if (newBorder.columnIndex > 0) {
                    addAdditionalNewBorder(HPos.RIGHT, newBorder.vPos,
                            newBorder.columnIndex - 1, newBorder.rowIndex, newBorder.playerMove);
                    gameFieldBorders[newBorder.rowIndex][newBorder.columnIndex - 1][3] = "RIGHT";

                    addNewSymbol(playerMove, newBorder.columnIndex - 1, newBorder.rowIndex);
                }
            } else if (newBorder.vPos == VPos.BOTTOM) {
                gameFieldBorders[newBorder.rowIndex][newBorder.columnIndex][1] = "BOTTOM";
                if (newBorder.rowIndex < FIELD_SIZE - 1) {
                    addAdditionalNewBorder(newBorder.hPos, VPos.TOP,
                            newBorder.columnIndex, newBorder.rowIndex + 1, newBorder.playerMove);
                    gameFieldBorders[newBorder.rowIndex + 1][newBorder.columnIndex][2] = "TOP";

                    addNewSymbol(playerMove, newBorder.columnIndex, newBorder.rowIndex + 1);
                }
            } else if (newBorder.vPos == VPos.TOP) {
                gameFieldBorders[newBorder.rowIndex][newBorder.columnIndex][2] = "TOP";
                if (newBorder.rowIndex > 0) {
                    addAdditionalNewBorder(newBorder.hPos, VPos.BOTTOM,
                            newBorder.columnIndex, newBorder.rowIndex - 1, newBorder.playerMove);
                    gameFieldBorders[newBorder.rowIndex - 1][newBorder.columnIndex][1] = "BOTTOM";

                    addNewSymbol(playerMove, newBorder.columnIndex, newBorder.rowIndex - 1);
                }
            } else if (newBorder.hPos == HPos.RIGHT) {
                gameFieldBorders[newBorder.rowIndex][newBorder.columnIndex][3] = "RIGHT";
                if (newBorder.columnIndex < FIELD_SIZE - 1) {
                    addAdditionalNewBorder(HPos.LEFT, newBorder.vPos,
                            newBorder.columnIndex + 1, newBorder.rowIndex, newBorder.playerMove);
                    gameFieldBorders[newBorder.rowIndex][newBorder.columnIndex + 1][0] = "LEFT";

                    addNewSymbol(playerMove, newBorder.columnIndex + 1, newBorder.rowIndex);
                }
            }

            addNewSymbol(playerMove, newBorder.columnIndex, newBorder.rowIndex);
            checkEndGame();
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    public void addAdditionalNewBorder(HPos hPos, VPos vPos, int columnIndex, int rowIndex, String playerMove) {
        AddNewBorder additionalNewBorder = new AddNewBorder(hPos, vPos, columnIndex, rowIndex, playerMove);
        try {
            players.get(0).objectOutputStream.writeObject(additionalNewBorder);
            players.get(1).objectOutputStream.writeObject(additionalNewBorder);
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    public void addNewSymbol(String playerSymbol, int columnIndex, int rowIndex) {
        boolean result = true;

        for (int i = 0; i < NUM_SIDES; i++) {
            if (gameFieldBorders[rowIndex][columnIndex][i] == null) {
                result = false;
                break;
            }
        }

        try {
            if (result) {
                AddNewSymbol newSymbol = new AddNewSymbol(playerSymbol, columnIndex, rowIndex);

                players.get(0).objectOutputStream.writeObject(newSymbol);
                players.get(1).objectOutputStream.writeObject(newSymbol);

                gameField[rowIndex][columnIndex] = playerSymbol;
            }
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.start();
    }

    @Override
    public void run() {
        try {
            ServerSocket listenSocket = new ServerSocket(serverPort);
            while (players.size() < 2) {
                Socket playerSocket = listenSocket.accept();
                String playerSymbol = (players.isEmpty()) ? "X" : "O";
                PlayerConnection playerConnection = new PlayerConnection(this, playerSocket, playerSymbol);
                players.add(playerConnection);
            }
            startGame();
        } catch (IOException e) {
            System.out.println("Listen socket: " + e.getMessage());
        }
    }
}
