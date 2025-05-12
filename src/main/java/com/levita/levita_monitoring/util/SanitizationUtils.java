package com.levita.levita_monitoring.util;

public class SanitizationUtils {

    private SanitizationUtils() {}

    /**
     * Убирает из строки нецифровые символы, заменяет неразрывные пробелы,
     * точки с запятой и запятые на точку, удаляет знак '%' и валютные символы.
     *
     * @param value входная строка, может содержать цифры, пробелы, разделители и символы валют.
     * @return нормализованная строка, готовая к преобразованию в число.
     */
    public static String sanitizeNumeric(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace(",", ".")
                .replace("%", "")
                .replaceAll("[₽$€¥£]", "")
                .trim();
    }

    /**
     * Убирает все символы кроме букв, цифр и пробелов.
     * Полезно для очистки имён и локаций от лишних знаков.
     *
     * @param value входная строка
     * @return строка, содержащая только буквы, цифры и пробелы
     */
    public static String sanitizeAlphaNumeric(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^\\p{Alnum} ]+", "").trim();
    }
}
