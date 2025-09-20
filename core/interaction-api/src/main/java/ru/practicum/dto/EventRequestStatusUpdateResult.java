package ru.practicum.dto;

import java.util.List;

public record EventRequestStatusUpdateResult(

        List<RequestDto> confirmedRequests,

         List<RequestDto> rejectedRequests
) {

}
