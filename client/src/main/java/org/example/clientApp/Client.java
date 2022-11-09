package org.example.clientApp;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.server.message.Message;
import org.example.server.message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    public static final int SOCKET_PORT = 7777;
    public static final String SOCKET_HOST = "localhost";
    private static Client client = null;
    private static ClientController clientController = null;
    private Socket socket;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    @Getter
    @Setter
    private String username;

    Client() {
        clientController = ClientController.getInstance();
    }

    public static Client getInstance() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    public void connectToServerAs(String username) throws IOException {
        this.setUsername(username);
        openConnectionToServer();

        Message messages = new Message(MessageType.CONNECT, getUsername());
        logger.info("Sending messages to the ServerSocket");
        sendMessage(messages);
    }

    public void sendMessage(Message message) throws IOException {
        objectOutputStream.writeObject(message);
    }

    private void openConnectionToServer() throws IOException {
        socket = new Socket(SOCKET_HOST, SOCKET_PORT);

        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());

        logger.info("Connected!");
    }

    public void listenForIncomingMessage() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    Message message = (Message) objectInputStream.readObject();
                    clientController.handleMessages(message);
                } catch (IOException | ClassNotFoundException e) {
                    closeEverything();
                    clientController.connectionLost();
                    break;
                }
            }
        }).start();
    }


    public void closeEverything() {


        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException e) {
            }
        }

        if (objectInputStream != null) {
            try {
                objectInputStream.close();
            } catch (IOException e) {
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

    }


}