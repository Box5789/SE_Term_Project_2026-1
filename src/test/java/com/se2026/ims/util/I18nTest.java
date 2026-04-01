package com.se2026.ims.util;

import org.junit.jupiter.api.Test;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

public class I18nTest {
    @Test
    public void testEnglish() {
        I18n.setLocale(Locale.ENGLISH);
        assertEquals("Issue Management System (IMS)", I18n.get("app.title"));
        assertEquals("📊 Dashboard", I18n.get("nav.dashboard"));
    }

    @Test
    public void testKorean() {
        I18n.setLocale(Locale.KOREAN);
        assertEquals("이슈 관리 시스템 (IMS)", I18n.get("app.title"));
        assertEquals("📊 대시보드", I18n.get("nav.dashboard"));
    }

    @Test
    public void testWithArguments() {
        I18n.setLocale(Locale.ENGLISH);
        assertEquals("Welcome, John (ADMIN)", I18n.get("dashboard.welcome", "John", "ADMIN"));
    }

    @Test
    public void testMissingKey() {
        assertTrue(I18n.get("non.existent.key").contains("non.existent.key"));
    }
}
