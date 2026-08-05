package com.debbiecyber.QuickBite.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RiderSummaryResponse {
    private Boolean availableForDelivery;
    private long activeDeliveries;
    private long completedDeliveries;
    private BigDecimal totalDeliveryEarnings;
}
