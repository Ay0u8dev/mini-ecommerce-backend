package com.miniecommerce.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent implements Serializable {

    private String eventId;
    private String eventType;  // USER_CREATED, USER_UPDATED, USER_DELETED
    private Long userId;
    private String userName;
    private String email;
    private LocalDateTime timestamp;

    public static UserEvent createUserCreatedEvent(Long userId, String userName, String email) {
        return UserEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("USER_CREATED")
                .userId(userId)
                .userName(userName)
                .email(email)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static UserEvent createUserUpdatedEvent(Long userId, String userName, String email) {
        return UserEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("USER_UPDATED")
                .userId(userId)
                .userName(userName)
                .email(email)
                .timestamp(LocalDateTime.now())
                .build();
    }
}