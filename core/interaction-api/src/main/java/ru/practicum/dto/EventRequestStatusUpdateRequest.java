package ru.practicum.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;

    private String status;
}
