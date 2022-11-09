package org.example.server.message;

import lombok.Data;
import org.example.server.Color;
import org.example.server.Timetable;

import java.io.Serializable;

@Data
public class Message implements Serializable {

    private final MessageType type;
    private String content;

    private Timetable timetable;

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }

    public Message(String content) {
        this(MessageType.NOTIFICATION, content);
    }

    public String getContent() {
        if (type == MessageType.ERROR)
            return Color.RED_BOLD + content + Color.RESET;
        else
            return content;
    }
}