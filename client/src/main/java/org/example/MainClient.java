package org.example;

import org.example.clientApp.ClientController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainClient {
    public static final String APP_UNEXPECTED_ERROR_MSG = "Unexpected application error. App will be close. \n For more details please check clientLogs.log file\n";
    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) {
        try {
            ClientController.getInstance().run();
        } catch (Exception e) {
            logger.error(APP_UNEXPECTED_ERROR_MSG, e);
            System.out.println(APP_UNEXPECTED_ERROR_MSG);
        }
    }
}
