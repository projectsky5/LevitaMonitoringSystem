package com.levita.levita_monitoring.dto.reportDto;

import java.math.BigDecimal;
import java.util.List;

public record FullReportDto(
        ShiftReportDto shift,
        TrialReportDto trial,
        CurrentReportDto current,
        List<OperationDto> operations
) {
    public record ShiftReportDto(
            BigDecimal shiftStart,
            BigDecimal shiftEnd
    ) {}

    public record TrialReportDto(
            int trialCame,

            int trialBought,
            BigDecimal trialBoughtAmount,

            int trialPaid,
            BigDecimal trialPaidAmount,

            int prepayment,
            BigDecimal prepaymentAmount,

            int surcharge,
            BigDecimal surchargeAmount
    ) {}

    public record CurrentReportDto(
            int finished,

            int extended,
            BigDecimal extendedAmount,

            int upgrades,
            BigDecimal upgradeAmount,

            int returned,
            BigDecimal returnedAmount,

            int prepayment,
            BigDecimal prepaymentAmount,

            int surcharge,
            BigDecimal surchargeAmount,

            int individual,
            BigDecimal individualAmount,

            int singleVisits,
            BigDecimal singleVisitAmount
    ) {}

    public record OperationDto(
            String type,
            BigDecimal amount,
            String cashType,
            String category,
            String comment
    ) {}
}
