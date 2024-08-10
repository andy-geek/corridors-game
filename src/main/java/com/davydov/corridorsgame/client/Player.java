package com.davydov.corridorsgame.client;

import com.davydov.corridorsgame.commands.AddNewBorder;
import com.davydov.corridorsgame.commands.AddNewSymbol;
import com.davydov.corridorsgame.commands.StartGame;

import com.davydov.corridorsgame.commands.StopGame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Player extends Application {
    String serverHost = "localhost";
    Integer serverPort = 8080;

    Boolean gameStatus;
    String playerSymbol;
    String playerMove;
    String winningPlayer;
    String drawSymbol = "D";

    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    PlayerController playerController;

    public void connectToGameServer() {
        new Thread(() -> {
            try {
                Socket playerSocket = new Socket(serverHost, serverPort);
                objectOutputStream = new ObjectOutputStream(playerSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(playerSocket.getInputStream());

                playerSymbol = objectInputStream.readUTF();

                StartGame startGame = (StartGame) objectInputStream.readObject();
                gameStatus = startGame.gameStatus;
                playerMove = startGame.playerMove;

                Platform.runLater(this::updateLabels);
                while (gameStatus) {
                    Object command = receiveCommand();
                    if (command instanceof AddNewBorder newBorder) {
                        Platform.runLater(() -> {
                            addNewBorder(newBorder.hPos, newBorder.vPos, newBorder.columnIndex, newBorder.rowIndex);
                        });
                        playerMove = newBorder.playerMove;
                    } else if (command instanceof AddNewSymbol newSymbol) {
                        Platform.runLater(() -> {
                            addNewSymbol(newSymbol.playerSymbol, newSymbol.columnIndex, newSymbol.rowIndex);
                        });
                        playerMove = newSymbol.playerSymbol;
                    } else if (command instanceof StopGame stopGame) {
                        gameStatus = stopGame.gameStatus;
                        winningPlayer = stopGame.winningPlayer;
                        playerMove = null;
                    }
                    Platform.runLater(this::updateLabels);
                }
                playerSocket.close();
            } catch (UnknownHostException e) {
                System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Readline: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFound: " + e.getMessage());
            }
        }).start();
    }

    public void addNewBorder(HPos hPos, VPos vPos, int columnIndex, int rowIndex) {
        Line border = new Line();
        border.setStrokeWidth(playerController.STROKE_WIDTH);
        border.setStartX(0);
        border.setStartY(0);

        if (vPos == VPos.TOP || vPos == VPos.BOTTOM) {
            border.setEndX(playerController.cellWidth - playerController.BORDER_SIZE);
            border.setEndY(0);
        } else {
            border.setEndX(0);
            border.setEndY(playerController.cellHeight - playerController.BORDER_SIZE);
        }

        GridPane.setHalignment(border, hPos);
        GridPane.setValignment(border, vPos);
        playerController.gamePanel.add(border, columnIndex, rowIndex);
    }

    public void addNewSymbol(String playerSymbol, int columnIndex, int rowIndex) {
        Label symbol = new Label(playerSymbol);
        symbol.setFont(Font.font(playerController.TEXT_FONT));

        GridPane.setHalignment(symbol, HPos.CENTER);
        playerController.gamePanel.add(symbol, columnIndex, rowIndex);
    }

    public void sendCommandAddNewBorder(HPos hPos, VPos vPos, Integer columnIndex, Integer rowIndex) {
        try {
            if (playerMove.equals(playerSymbol)) {
                objectOutputStream.writeObject(new AddNewBorder(hPos, vPos, columnIndex, rowIndex, playerSymbol));
            }
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        } catch (NullPointerException ignored) {
        }
    }

    public Object receiveCommand() {
        Object command = null;
        try {
            command = objectInputStream.readObject();
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }

        return command;
    }

    public void updateLabels() {
        if (playerController.labelPlayerSymbol.getText().isEmpty()) {
            playerController.labelPlayerSymbol.setText("You play: " + playerSymbol);
        }

        if (gameStatus) {
            if (playerMove.equals(playerSymbol)) {
                playerController.labelPlayerMove.setText("Your move!");
            } else  {
                playerController.labelPlayerMove.setText("The opponent's move!");
            }
        } else {
            if (winningPlayer.equals(playerSymbol)) {
                playerController.labelPlayerMove.setText("You have won!");
            } else if (winningPlayer.equals(drawSymbol)){
                playerController.labelPlayerMove.setText("A draw!");
            } else {
                playerController.labelPlayerMove.setText("You have lost!");
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Player.class.getResource("GameWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 430, 515);
        stage.setTitle("Corridors Game");
        stage.setScene(scene);
        stage.show();

        playerController = fxmlLoader.getController();
        playerController.player = this;
    }

    public static void main(String[] args) {
        launch();
    }
}
