package ru.practicum.event.dto;

import ru.practicum.request.dto.RequestDto;

import java.util.List;

public record EventRequestStatusUpdateResult(

        List<RequestDto> confirmedRequests,

         List<RequestDto> rejectedRequests
) {

}
