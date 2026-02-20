package com.typearena.model;
import java.util.List;

public class GameMessage {
    private MessageType type;
    private String sender;
    private String roomCode;
    private double progress;
    private int wpm;
    private String content;
    private String color;
    private List<PlayerInfo> playerList;

    // CHANGE_COLOR eklendi
    public enum MessageType { JOIN, START, UPDATE_PROGRESS, LEAVE, ROOM_STATE, CHANGE_COLOR }

    public static class PlayerInfo {
        private String name;
        private String color;
        private double progress;
        private int wpm;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public double getProgress() { return progress; }
        public void setProgress(double progress) { this.progress = progress; }
        public int getWpm() { return wpm; }
        public void setWpm(int wpm) { this.wpm = wpm; }
    }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
    public int getWpm() { return wpm; }
    public void setWpm(int wpm) { this.wpm = wpm; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public List<PlayerInfo> getPlayerList() { return playerList; }
    public void setPlayerList(List<PlayerInfo> playerList) { this.playerList = playerList; }
}