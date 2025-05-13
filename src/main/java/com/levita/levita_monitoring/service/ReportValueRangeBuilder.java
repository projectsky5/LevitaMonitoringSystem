package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;

import java.io.IOException;

public interface ReportValueRangeBuilder {
    ValueRange buildShiftValueRange(
            FullReportDto.ShiftReportDto shift,
            User user,
            String reportDate
    ) throws IOException;
    ValueRange buildTrialValueRange(
            FullReportDto.TrialReportDto trial,
            User user,
            String reportDate
    ) throws IOException;
    ValueRange buildCurrentValueRange(
            FullReportDto.CurrentReportDto current,
            User user,
            String reportDate
    ) throws IOException;
    ValueRange buildClearShiftValueRange(
            User user,
            String reportDate
    ) throws IOException;
    ValueRange buildClearTrialValueRange(
            User user,
            String reportDate
    ) throws IOException;
    ValueRange buildClearCurrentValueRange(
            User user,
            String reportDate
    ) throws IOException;

    void updateDefaultValues(
            ValueRange range
    ) throws IOException;

}
