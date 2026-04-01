package com.se2026.ims.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;
    private static Locale currentLocale;

    static {
        // 기본 로케일을 시스템 설정에 맞추거나, 명시적으로 설정 가능
        // 한글 윈도우/맥인 경우 KO가 기본일 것임.
        setLocale(Locale.getDefault());
    }

    public static void setLocale(Locale locale) {
        try {
            currentLocale = locale;
            bundle = ResourceBundle.getBundle("i18n.messages", locale);
        } catch (Exception e) {
            System.err.println("Could not load resource bundle for locale " + locale + ": " + e.getMessage());
            try {
                currentLocale = Locale.ENGLISH;
                bundle = ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH);
            } catch (Exception e2) {
                System.err.println("FATAL: Could not even load default ENGLISH resource bundle.");
            }
        }
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static String get(String key) {
        if (bundle == null) return "[" + key + "]";
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static String get(String key, Object... args) {
        String value = get(key);
        if (value.startsWith("!")) return value;
        return MessageFormat.format(value, args);
    }
}
