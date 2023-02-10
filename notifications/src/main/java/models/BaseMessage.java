package models;

import java.util.Date;

abstract class BaseMessage {
    String id;
    Date timestamp;
    UserInfo creator;
    UserInfo receiver;

    static class UserInfo {
        String id;
        String displayName;
    }
}