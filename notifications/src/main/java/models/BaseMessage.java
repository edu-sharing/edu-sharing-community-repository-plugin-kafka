package models;

import java.util.Date;

abstract class BaseMessage {
    String id;
    MessageType messageType;
    Date timestamp;
    UserInfo creator;
    UserInfo receiver;

    static class UserInfo {
        String id;
        String displayName;
    }
    enum MessageType {

    }
}