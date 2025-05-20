package com.levita.levita_monitoring.enums;

import java.util.HashMap;
import java.util.Map;

public enum Kpi {
    LOCATION_INCOME("ACTUAL_INCOME", true),
    PLAN("LOCATION_PLAN",true),
    MAX_REVENUE("MAX_DAILY_REVENUE", true),
    PLAN_COMPLETION("PLAN_COMPLETION_PERCENT", true),
    REMAINING("REMAINING_TO_PLAN", true),
    DAILY_FIGURE("DAILY_FIGURE", true),
    AVG_REVENUE_PER_DAY("AVG_REVENUE_PER_DAY", true),

    CONVERSION("CONVERSION_RATE", false),
    MAIN_SALARY_PART("MAIN_SALARY_PART", false),
    USER_REVENUE("PERSONAL_REVENUE", false),
    USER_INCOME("CURRENT_INCOME", false),
    BONUSES("DAY_BONUSES", false);

    private final String topic;
    private final boolean isLocationCategory;

    // Map для быстрого поиска enum по его topic
    private static final Map<String, Kpi> BY_TOPIC = new HashMap<>();
    static {
        for (var e : values()) {
            BY_TOPIC.put(e.topic, e);
        }
    }

    Kpi(String topic, boolean isLocationCategory) {
        this.topic = topic;
        this.isLocationCategory = isLocationCategory;
    }

    /** возвращает enum по строке, или null если не найден */
    public static Kpi fromTopic(String topic) {
        return BY_TOPIC.get(topic);
    }

    /** нужно ли считать этот KPI «location» */
    public boolean isLocationCategory() {
        return isLocationCategory;
    }

    /** возвращает исходную строку */
    public String getTopic() {
        return topic;
    }
}