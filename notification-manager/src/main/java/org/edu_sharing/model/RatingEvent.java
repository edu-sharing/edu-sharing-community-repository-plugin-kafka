package org.edu_sharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("RatingMessage")
@Document(collation = "notification")
public class RatingEvent extends NodeBaseEvent {
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