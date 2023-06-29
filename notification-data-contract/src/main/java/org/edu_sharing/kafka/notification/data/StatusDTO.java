package org.edu_sharing.kafka.notification.data;

public enum StatusDTO {
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
