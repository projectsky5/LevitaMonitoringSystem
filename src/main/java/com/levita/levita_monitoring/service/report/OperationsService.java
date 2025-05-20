package com.levita.levita_monitoring.service.report;

import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;


public interface OperationsService {
    void saveOperations(
            List<FullReportDto.OperationDto> operations,
            User user,
            String reportDate
    ) throws IOException;

    void rollbackOperations(
            User user,
            String reportDate
    ) throws IOException;

    int findFirstEmptyRow(
            String sheet,
            String column,
            int startRow
    ) throws IOException;

    List<Integer> findRowsByDate(
            String sheet,
            String column,
            int startRow,
            String date
    ) throws IOException;

    String buildCellRange(
            String sheet,
            String col,
            int row
    ) throws IOException;

    List<Object> buildOperationRow(String reportDate,
                                   Object income,
                                   Object expense,
                                   String cashType,
                                   String category,
                                   String admin,
                                   String comment
    );

    Object formatAmount(BigDecimal amt);
}
