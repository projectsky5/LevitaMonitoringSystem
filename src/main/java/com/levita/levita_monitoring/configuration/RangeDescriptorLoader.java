package com.levita.levita_monitoring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.integration.model.dto.ExplicitRange;
import com.levita.levita_monitoring.integration.model.dto.RangeGroup;
import com.levita.levita_monitoring.integration.model.dto.SheetsYamlConfig;
import com.levita.levita_monitoring.integration.model.dto.SpreadsheetYamlEntry;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Configuration
public class RangeDescriptorLoader {

    @Bean
    public List<SpreadsheetConfig> spreadsheetConfigFromYaml() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        SheetsYamlConfig yamlConfig = mapper.readValue(
                new ClassPathResource("config/sheets-ranges.yml").getInputStream(),
                SheetsYamlConfig.class
        );

        List<SpreadsheetConfig> result = new ArrayList<>();

        for (SpreadsheetYamlEntry spreadsheet : yamlConfig.spreadsheets){
            List<RangeDescriptor> descriptors = new ArrayList<>();

            for(RangeGroup rangeGroup : spreadsheet.ranges){
                if(rangeGroup.template != null && rangeGroup.count != null) {
                    IntStream.rangeClosed(1, rangeGroup.count).forEach(i ->
                            descriptors.add(new RangeDescriptor(
                                    i,
                                    String.format(rangeGroup.template, i),
                                    rangeGroup.category,
                                    null
                            ))
                    );
                }

                if(rangeGroup.explicit != null) {
                    for (ExplicitRange explicitRange : rangeGroup.explicit){
                        descriptors.add(new RangeDescriptor(
                                explicitRange.id,
                                explicitRange.range,
                                rangeGroup.category,
                                explicitRange.label
                                ));
                    }
                }
            }
            result.add(new SpreadsheetConfig(spreadsheet.id, descriptors));
        }

        return result;
    }
}
