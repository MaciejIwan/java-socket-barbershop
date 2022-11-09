package org.example.server;

import lombok.Getter;
import org.example.server.exception.TimetableException;
import org.example.server.message.Message;
import org.example.server.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.server.exception.AcceptUserConnectionException;
import org.example.server.exception.UsernameIsAlreadyTakenException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BarberShopServer implements Runnable {
    private static final int PORT = 7777;
    private ServerSocket serverSocket;
    public static final Map<String, UserHandler> userHandlers = new HashMap<>();
    private static BarberShopServer barberShopServer = null;

    @Getter
    private final Timetable timetable;
    private static final Logger logger = LoggerFactory.getLogger(BarberShopServer.class);

    public BarberShopServer(Timetable timetable) {
        this.timetable = timetable;
    }

    public static BarberShopServer getInstance() {
        return getInstance(new Timetable());
    }

    public static BarberShopServer getInstance(Timetable timetable) {
        BarberShopServer result = barberShopServer;
        if (result != null) {
            return result;
        }
        synchronized (BarberShopServer.class) {
            if (barberShopServer == null) {
                barberShopServer = new BarberShopServer(timetable);
            }
            return barberShopServer;
        }
    }

    @Override
    public void run() {
        try {
            startServer();
            while (true) {
                Socket userSocket = acceptNewConnection();
                handleNewConnection(userSocket);
            }
        } catch (IOException e) {
            logger.error("server failed!", e);
        } catch (AcceptUserConnectionException e) {
            logger.error("Cant handle new user", e);
        } finally {
            closeServer();
        }
    }

    private void startServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        logger.info("Server awaiting connections...");
    }


    private Socket acceptNewConnection() throws AcceptUserConnectionException {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            logger.error("Can't open new socket connection", e);
            throw new AcceptUserConnectionException("");
        }
    }

    synchronized public void broadcastToEveryone(Message notify) {
        for (Map.Entry<String, UserHandler> set : userHandlers.entrySet()) {
            try {
                UserHandler handler = set.getValue();
                handler.sendMessageToUser(notify);
            } catch (IOException ioException) {
                System.out.println("can't send update to user");
            }
        }
    }

    synchronized void checkIfUserAlreadyExists(String usernameToCheck) throws UsernameIsAlreadyTakenException {
        UserHandler value = BarberShopServer.userHandlers.get(usernameToCheck);
        boolean isUsernameTaken = value != null || usernameToCheck.equals(Timetable.FREE_HOUR_STRING);
        if (isUsernameTaken) {
            throw new UsernameIsAlreadyTakenException("Username is already taken");
        }
    }

    private void handleNewConnection(Socket newUserSocket) {
        try {
            UserHandler userHandler = new UserHandler(newUserSocket);
            new Thread(userHandler).start();

        } catch (IllegalThreadStateException illegalThreadStateException) {
            logger.error("Can't start new thread", illegalThreadStateException);
        }
    }

    synchronized void addVisit(String hour, String username) throws TimetableException {
        try {
            timetable.addVisit(hour, username);
        } catch (Exception e) {
            throw new TimetableException("Timetable exception");
        }
        broadcastToEveryone(new Message(MessageType.TIMETABLE, timetable.toString()));
    }

    synchronized public void cancelVisit(String hour, String username) throws TimetableException {
        try {
            timetable.cancelReservation(hour, username);
        } catch (Exception e) {
            throw new TimetableException("Timetable exception");
        }
        broadcastToEveryone(new Message(MessageType.TIMETABLE, timetable.toString()));
    }

    private void closeServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
