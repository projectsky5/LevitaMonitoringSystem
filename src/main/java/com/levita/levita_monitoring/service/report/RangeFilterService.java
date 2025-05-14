package com.levita.levita_monitoring.service.report;

import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;

import java.util.List;

public interface RangeFilterService {
    List<RangeDescriptor> filterByCategory(SpreadsheetConfig config, String category);
    List<RangeDescriptor> filterExcluding(SpreadsheetConfig config, List<String> excludedCategories);
}
