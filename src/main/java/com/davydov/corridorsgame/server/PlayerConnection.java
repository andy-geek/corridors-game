package com.davydov.corridorsgame.server;

import com.davydov.corridorsgame.commands.AddNewBorder;

import java.io.*;
import java.net.Socket;

public class PlayerConnection extends Thread {
    GameServer gameServer;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    Socket playerSocket;

    public PlayerConnection(GameServer server, Socket socket, String playerSymbol) {
        try {
            gameServer = server;
            playerSocket = socket;

            objectInputStream = new ObjectInputStream(playerSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(playerSocket.getOutputStream());

            objectOutputStream.writeUTF(playerSymbol);

            this.start();
        } catch (IOException e) {
            System.out.println("GameConnection: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                AddNewBorder newBorder = (AddNewBorder) objectInputStream.readObject();
                boolean check = gameServer.checkNewBorder(newBorder);
                if (check) {
                    gameServer.addNewBorder(newBorder);
                }
            }
        } catch (IOException e) {
            System.out.println("Readline: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }
}
