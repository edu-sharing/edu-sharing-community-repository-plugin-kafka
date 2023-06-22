package org.edu_sharing.kafka.notification.events.data;

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
}
