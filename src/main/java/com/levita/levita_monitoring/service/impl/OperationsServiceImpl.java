package com.levita.levita_monitoring.service.impl;

import com.levita.levita_monitoring.configuration.sheet_reports.YamlConfigLoader;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.service.OperationsService;
import com.levita.levita_monitoring.service.SheetsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationsServiceImpl implements OperationsService {

    private static final Logger log = LoggerFactory.getLogger(OperationsServiceImpl.class);

    private final YamlConfigLoader yamlLoader;
    private final SheetsClient sheetsClient;

    public OperationsServiceImpl(YamlConfigLoader yamlLoader, SheetsClient sheetsClient) {
        this.yamlLoader = yamlLoader;
        this.sheetsClient = sheetsClient;
    }

    @Override
    public void saveOperations(List<FullReportDto.OperationDto> operations, User user, String reportDate) throws IOException {
        String location = user.getLocation().getName();
        String sheetName = yamlLoader.getSheetNamesDescriptor().getOperations().get(location);
        if (sheetName == null) {
            throw new IllegalArgumentException(
                    "Не найден лист операций для локации: " + location);
        }
        String dateCol = yamlLoader.getDateColumnsDescriptor().getDateColumns().get(sheetName);
        if (dateCol == null && sheetName.startsWith("Журнал операций")) {
            dateCol = "B";
        }
        int startRow = 2;
        int insertRow = findFirstEmptyRow(sheetName, dateCol, startRow);
        if (operations == null || operations.isEmpty()) {
            sheetsClient.appendRow(buildCellRange(sheetName, dateCol, insertRow), List.of(reportDate));
            return;
        }

        for (FullReportDto.OperationDto dto : operations) {
            Object income;
            Object expense;
            if("Приход".equalsIgnoreCase(dto.type())){
                income = formatAmount(dto.amount());
                expense = "";
            } else {
                income = "";
                expense = formatAmount(dto.amount());
            }

            List<Object> row = buildOperationRow(reportDate,
                    income,
                    expense,
                    dto.cashType(),
                    dto.category(),
                    user.getName(),
                    dto.comment()
            );
            sheetsClient.appendRow(buildCellRange(sheetName, dateCol, insertRow++), row);
        }
    }

    @Override
    public void rollbackOperations(User user, String reportDate) throws IOException {
        String location  = user.getLocation().getName();
        String sheetName = yamlLoader.getSheetNamesDescriptor()
                .getOperations()
                .get(location);
        if (sheetName == null) {
            throw new IllegalArgumentException("Не найден лист операций для локации: " + location);
        }

        // попытка получить колонку из date-columns.yml
        String dateCol = yamlLoader.getDateColumnsDescriptor()
                .getDateColumns()
                .get(sheetName);
        // если её нет и это лист операций — ставим «B»
        if (dateCol == null && sheetName.startsWith("Журнал операций")) {
            dateCol = "B";
        }

        int startRow = 2;
        List<Integer> rows = findRowsByDate(sheetName, dateCol, startRow, reportDate);
        if (rows.isEmpty()) {
            log.info("Откат операций: строк за дату [{}] не найдено в листе [{}]", reportDate, sheetName);
            return;
        }

        sheetsClient.deleteRows(sheetName, rows);
    }

    @Override
    public List<Integer> findRowsByDate(String sheet, String column, int startRow, String date) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheet, column, startRow, column);
        List<List<Object>> rows = sheetsClient.getValues(range);
        List<Integer> result = new ArrayList<>();
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (!rows.get(i).isEmpty() && date.equals(rows.get(i).getFirst().toString())) {
                    result.add(startRow + i);
                }
            }
        }
        return result;
    }

    @Override
    public String buildCellRange(String sheet, String col, int row) {
        return String.format("'%s'!%s%d", sheet, col, row);
    }

    @Override
    public Object formatAmount(BigDecimal amt) {
        if (amt == null) return null;
        BigDecimal clean = amt.stripTrailingZeros();
        return clean.scale() <= 0 ? clean.longValue() : clean.doubleValue();
    }

    @Override
    public int findFirstEmptyRow(String sheet, String column, int startRow) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheet, column, startRow, column);
        List<List<Object>> rows = sheetsClient.getValues(range);

        // если API вернул null — значит нет ни одной заполненной строки в столбце,
        // первая свободная — startRow
        if (rows == null || rows.isEmpty()) {
            return startRow;
        }

        // ищем первый «пропуск» внутри rows
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).isEmpty()) {
                // rowIndex = startRow + i
                return startRow + i;
            }
        }

        // если до конца списка пустых не встретили — следующая за последней непустой
        return startRow + rows.size();
    }

    @Override
    public List<Object> buildOperationRow(String reportDate,
                                           Object income,
                                           Object expense,
                                           String cashType,
                                           String category,
                                           String admin,
                                           String comment
    ) {
        List<Object> row = new ArrayList<>(7);
        row.add(reportDate);
        row.add(income);
        row.add(expense);
        row.add(cashType);
        row.add(category);
        row.add(admin);
        row.add(comment);
        return row;
    }

}
