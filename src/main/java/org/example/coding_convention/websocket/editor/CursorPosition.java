package org.example.coding_convention.websocket.editor;

public class CursorPosition {
    private String senderId;
    private int line;
    private String column;

    public CursorPosition() {}

    public CursorPosition(String senderId, int line, String column) {
        this.senderId = senderId;
        this.line = line;
        this.column = column;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
