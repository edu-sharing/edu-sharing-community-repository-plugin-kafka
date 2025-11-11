package org.edu_sharing.kafka.notification.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.edu_sharing.kafka.notification.data.NodeDataDTO;
import org.edu_sharing.kafka.notification.data.StatusDTO;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RatingEventDTO extends NodeBaseEventDTO {
    public RatingEventDTO(String id, Date timestamp, String creatorId, String receiverId, StatusDTO status, NodeDataDTO node, String ratingMode, double newRating, double ratingSum, long ratingCount) {
        super(id, timestamp, creatorId, receiverId, status, node);
        this.newRating = newRating;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
        this.ratingMode = ratingMode;
    }

    /**
     * the new rating that was given
     */
    private double newRating;
    /**
     * the new rating sum
     */
    private double ratingSum;
    /**
     * the count of ratings in total
     * (To get the avg use ratingSum / ratingCount)
     */
    private long ratingCount;

    /**
     * Represents the mode associated with a rating event.
     * This can be used to differentiate between various contexts or types of ratings. (stars, likes)
     */
    private String ratingMode;
}
