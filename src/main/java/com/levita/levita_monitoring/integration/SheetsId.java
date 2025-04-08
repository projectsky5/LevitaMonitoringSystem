package com.levita.levita_monitoring.integration;

public enum SheetsId {

    FIRST_TABLE("1Hgzq2qdmAV3oyOKnhgZIWLjxopbGs8jvNMM4kFT7MsY"),
    SECOND_TABLE("1y-nCwUkKVQPg9LSgtbnneT5bCWj_D7v54yD17SABVAw");

    private String propertyKey;

    SheetsId(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

}
