package com.hospital.queue.dto.response;

import com.hospital.queue.enums.TokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueuePositionResponse {

    private String tokenNumber;
    private TokenStatus status;

    /** 1-based position. 0 if currently IN_PROGRESS. -1 if COMPLETED. */
    private int position;

    /** Total patients still waiting ahead. */
    private int totalWaiting;

    private String doctorName;
    private String departmentName;
}
