package org.example;

import org.example.server.BarberShopServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainServer {
    private static final Logger logger = LoggerFactory.getLogger(MainServer.class);
    public static final String APP_UNEXPECTED_ERROR_MSG = "Unexpected application error. App will be close. \n For more details please check clientLogs.log file\n";
    public static void main(String[] args) {
        try {
            BarberShopServer.getInstance().run();
        } catch (Exception e) {
            logger.error(APP_UNEXPECTED_ERROR_MSG);
            System.out.println(APP_UNEXPECTED_ERROR_MSG);
        }
    }
}
