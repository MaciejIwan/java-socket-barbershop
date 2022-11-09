package org.example.clientApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.server.message.Message;
import org.example.server.message.MessageType;

import java.io.IOException;
import java.util.Scanner;

import static java.lang.System.exit;

public class ClientController implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private static ClientController clientController = null;
    private static Client client = null;
    private final Scanner scanner;

    ClientController() {
        clientController = this;
        client = Client.getInstance();
        scanner = new Scanner(System.in);
    }

    public static ClientController getInstance() {
        if (clientController == null) {
            clientController = new ClientController();
        }
        return clientController;
    }

    @Override
    public void run() {
        try {
            String username = loadPhoneNumberFromUser();

            client.connectToServerAs(username);
            client.listenForIncomingMessage();
            handleUserInput();

        } catch (Exception e) {
            client.closeEverything();
            logger.error("Unexpected error", e);
            throw new RuntimeException(e);
        }
    }

    private void handleUserInput() throws IOException {
        while (true) {
            String input = scanner.nextLine();
            Message command = new Message(MessageType.COMMAND, input);
            client.sendMessage(command);
        }
    }

    private String loadPhoneNumberFromUser() {
        System.out.print("Enter your username for the group chat: ");
        return scanner.nextLine();
    }

    public void handleMessages(Message message) {
        System.out.println(message.getContent());
        System.out.print("Send command to server: ADD|CANCEL|EXIT HOUR(int) format HH)\n");
    }

    public void connectionLost() {
        System.out.printf("Connection has been lost");
        client.closeEverything();
        exit(1);
    }
}
