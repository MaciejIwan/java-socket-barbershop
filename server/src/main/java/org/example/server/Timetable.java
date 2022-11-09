package org.example.server;

import org.example.server.exception.TimetableException;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;


public class Timetable implements Serializable {

    public static final String FREE_HOUR_STRING = "free";
    private final Map<String, String> timetable;

    public Timetable() {
        timetable = new TreeMap<>();

        timetable.put("10", FREE_HOUR_STRING);
        timetable.put("11", FREE_HOUR_STRING);
        timetable.put("12", FREE_HOUR_STRING);
        timetable.put("13", FREE_HOUR_STRING);
        timetable.put("14", FREE_HOUR_STRING);
        timetable.put("15", FREE_HOUR_STRING);
        timetable.put("16", FREE_HOUR_STRING);
        timetable.put("17", FREE_HOUR_STRING);
    }

    private boolean isHourFree(String hour) {
        return timetable.containsKey(hour) && timetable.get(hour).equals(FREE_HOUR_STRING);
    }

    public void addVisit(String hour, String nickname) throws TimetableException {
        if (!isHourFree(hour)) {
            throw new TimetableException("Hour is already taken");
        }
        timetable.put(hour, nickname);
    }

    public void cancelReservation(String hour, String username) throws TimetableException {
        timetable.computeIfPresent(hour, (h, name) -> name.equals(username) ? FREE_HOUR_STRING : username);
        if(!timetable.get(hour).equals(FREE_HOUR_STRING))
            throw new TimetableException("You don't have permissions to cancel this visit");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        timetable.forEach((hour, client) -> stringBuilder.append(getColorForRecord(hour, client)));
        return stringBuilder.toString();
    }

    private String getColorForRecord(String hour, String clientName) {
        StringBuilder stringBuilder = new StringBuilder();
        Color textColor = clientName == FREE_HOUR_STRING ? Color.GREEN_BOLD : Color.RED_BOLD;
        return stringBuilder
                .append(textColor)
                .append(hour)
                .append(" - ")
                .append(clientName)
                .append(Color.RESET)
                .append("\n")
                .toString();
    }

}
