package org.edu_sharing.notification.data;

public enum Status {
    /**
     *  was created
     */
    NEW,
    /**
     * waits to get send
     */
    PENDING,
    /**
     * notification was sent
     */
    SENT,
    /**
     * was read
     */
    READ,

    /**
     * notification was disabled
     */
    IGNORED,
}
