package com.levita.levita_monitoring.service.report;

import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RangeFilterServiceImpl implements RangeFilterService {
    @Override
    public List<RangeDescriptor> filterByCategory(SpreadsheetConfig config, String category) {
        return config.getRanges().stream()
                .filter(descriptor -> descriptor.category().equalsIgnoreCase(category))
                .toList();
    }

    @Override
    public List<RangeDescriptor> filterExcluding(SpreadsheetConfig config, List<String> excludedCategories) {
        return config.getRanges().stream()
                .filter(descriptor -> !excludedCategories.contains(descriptor.category()))
                .toList();
    }
}
