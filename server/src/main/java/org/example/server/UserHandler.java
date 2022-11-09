package org.example.server;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.example.server.exception.TimetableException;
import org.example.server.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.server.exception.UsernameIsAlreadyTakenException;
import org.example.server.message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;


public class UserHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    private final Socket socket;
    private final BarberShopServer server;

    private ObjectInputStream objectInputStream;

    private ObjectOutputStream objectOutputStream;

    @Getter
    @Setter
    private String username;

    public UserHandler(Socket newUserSocket) {

        this.socket = newUserSocket;
        this.server = BarberShopServer.getInstance();
    }

    @SneakyThrows
    @Override
    public void run() {
        initializeConnectionsStreams();

        try {
            registerUser();
            sendWelcomeMessage();
            while (socket.isConnected()) {
                Message command = getIncomingMessage();
                handleUserCommand(command);
            }
        } catch (UsernameIsAlreadyTakenException e) {
            sendMessageToUser(new Message(MessageType.ERROR, "Username is already taken"));
        } catch (SocketException e) {
            logger.error(e.getMessage());
        } finally {
            closeConnectionStreams();
        }

    }

    synchronized private void registerUser() throws IOException, ClassNotFoundException, UsernameIsAlreadyTakenException {
        Message firstMessage = getIncomingMessage();

        setUsername(firstMessage.getContent());
        server.checkIfUserAlreadyExists(getUsername());
        BarberShopServer.userHandlers.put(getUsername(), this);
    }

    private void sendWelcomeMessage() throws IOException {
        Message welcomeMessage = new Message(MessageType.TIMETABLE, server.getTimetable().toString());
        sendMessageToUser(welcomeMessage);
    }

    public void sendMessageToUser(Message notify) throws IOException {
        objectOutputStream.writeObject(notify);
    }

    private void handleUserCommand(Message command) throws IOException {
        String[] orderHourName = command.getContent().split(" ");

        try {
            switch (orderHourName[0]) {
                case "ADD" -> server.addVisit(orderHourName[1], getUsername());
                case "CANCEL" -> server.cancelVisit(orderHourName[1], getUsername());
                case "EXIT" -> closeConnectionStreams();
                default -> sendMessageToUser(new Message(MessageType.ERROR, "Command is not valid"));
            }
        } catch (TimetableException e) {
            sendMessageToUser(new Message(MessageType.ERROR, "Visit modification error"));
        }
    }


    private void initializeConnectionsStreams() throws IOException {
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }


    private Message getIncomingMessage() throws IOException, ClassNotFoundException {
        Message message = (Message) objectInputStream.readObject();
        logger.info("Received message from: " + (getUsername() != null ? getUsername() : "first message"));
        return message;
    }


    synchronized public void closeConnectionStreams() throws IOException {
        logger.debug("closeConnections() method Enter - " + getUsername());
        logger.info("HashMap names:" + BarberShopServer.userHandlers.size());

        if (objectInputStream != null) {
            objectInputStream.close();
        }

        if (objectOutputStream != null) {
            objectOutputStream.close();
        }

        if (socket != null) {
            socket.close();
        }

        BarberShopServer.userHandlers.remove(getUsername());

        logger.info("HashMap names:" + BarberShopServer.userHandlers.size());
        logger.debug("closeConnections() method Exit - user: " + getUsername());

    }


}
