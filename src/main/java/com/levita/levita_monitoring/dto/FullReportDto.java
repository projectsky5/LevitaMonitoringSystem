package com.levita.levita_monitoring.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record FullReportDto(
        @Valid @NotNull ShiftReportDto shift,
        @Valid @NotNull TrialReportDto trial,
        @Valid @NotNull CurrentReportDto current,
        @Valid List<@Valid OperationDto> operations
) {
    public record ShiftReportDto(
            @NotNull @PositiveOrZero BigDecimal shiftStart,
            @NotNull @PositiveOrZero BigDecimal shiftEnd
    ) {}

    public record TrialReportDto(
            @Min(0) int trialCame,

            @Min(0) int trialBought,
            @NotNull @PositiveOrZero BigDecimal trialBoughtAmount,

            @Min(0) int trialPaid,
            @NotNull @PositiveOrZero BigDecimal trialPaidAmount,

            @Min(0) int prepayment,
            @NotNull @PositiveOrZero BigDecimal prepaymentAmount,

            @Min(0) int surcharge,
            @NotNull @PositiveOrZero BigDecimal surchargeAmount
    ) {}

    public record CurrentReportDto(
            @Min(0) int finished,

            @Min(0) int extended,
            @NotNull @PositiveOrZero BigDecimal extendedAmount,

            @Min(0) int upgrades,
            @NotNull @PositiveOrZero BigDecimal upgradeAmount,

            @Min(0) int returned,
            @NotNull @PositiveOrZero BigDecimal returnedAmount,

            @Min(0) int prepayment,
            @NotNull @PositiveOrZero BigDecimal prepaymentAmount,

            @Min(0) int surcharge,
            @NotNull @PositiveOrZero BigDecimal surchargeAmount,

            @Min(0) int individual,
            @NotNull @PositiveOrZero BigDecimal individualAmount,

            @Min(0) int singleVisits,
            @NotNull @PositiveOrZero BigDecimal singleVisitAmount
    ) {}

    public record OperationDto(
            @NotBlank String type,
            @NotNull BigDecimal amount,
            @NotBlank String cashType,
            @NotBlank String category,
            String comment
    ) {}
}
