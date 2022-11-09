package org.example.server.message;

import java.io.Serializable;

public enum MessageType implements Serializable {
    CONNECT, COMMAND, NOTIFICATION, USERNAME_TAKEN, TIMETABLE, ERROR
}
