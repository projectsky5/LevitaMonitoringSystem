package com.levita.levita_monitoring.configuration.sheet_reports;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

@Component
public class YamlConfigLoader {

    private SheetNamesDescriptor sheetNamesDescriptor;
    private ShiftColumnsDescriptor shiftColumnsDescriptor;
    private DateColumnsDescriptor dateColumnsDescriptor;
    private CurrentColumnsDescriptor currentColumnsDescriptor;
    private TrialColumnsDescriptor trialColumnsDescriptor;

    @PostConstruct
    public void loadAll() throws Exception {
        this.sheetNamesDescriptor     = load("config/sheets-names.yml",    SheetNamesDescriptor.class);
        this.shiftColumnsDescriptor   = load("config/shift-columns.yml",   ShiftColumnsDescriptor.class);
        this.dateColumnsDescriptor    = load("config/date-columns.yml",    DateColumnsDescriptor.class);
        this.currentColumnsDescriptor = load("config/current-columns.yml", CurrentColumnsDescriptor.class);
        this.trialColumnsDescriptor   = load("config/trial-columns.yml",   TrialColumnsDescriptor.class);
    }

    private <T> T load(String path, Class<T> type) throws Exception {
        // 1) Опции загрузчика YAML
        LoaderOptions options = new LoaderOptions();
        // 2) SnakeYAML-конструктор, который умеет создавать ваш JavaBean
        Constructor yamlCtor = new Constructor(type, options);
        // 3) Создаём Yaml-инстанс с этим конструктором
        Yaml yaml = new Yaml(yamlCtor);

        // 4) Читаем файл и подставляем в класс
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return yaml.<T>loadAs(in, type);
        }
    }

    // Геттеры для доступа из других бинов
    public SheetNamesDescriptor getSheetNamesDescriptor()       { return sheetNamesDescriptor; }
    public ShiftColumnsDescriptor getShiftColumnsDescriptor()   { return shiftColumnsDescriptor; }
    public DateColumnsDescriptor getDateColumnsDescriptor()     { return dateColumnsDescriptor; }
    public CurrentColumnsDescriptor getCurrentColumnsDescriptor() { return currentColumnsDescriptor; }
    public TrialColumnsDescriptor getTrialColumnsDescriptor()   { return trialColumnsDescriptor; }
}