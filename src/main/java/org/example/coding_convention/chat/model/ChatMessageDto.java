package org.example.coding_convention.chat.model;

import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class ChatMessageDto {
    private Integer id;
    private String message;
    private String username;
    private String time;

    public void setUsernameTime(String nickname) {
        username = nickname;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = formatter.format(new Date());
    }
}
