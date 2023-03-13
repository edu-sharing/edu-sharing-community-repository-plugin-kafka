package org.edu_sharing.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Notification")
@TypeAlias("RatingMessage")
@JsonTypeName("RatingMessage")
public class RatingMessage extends NodeBaseMessage {
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
}