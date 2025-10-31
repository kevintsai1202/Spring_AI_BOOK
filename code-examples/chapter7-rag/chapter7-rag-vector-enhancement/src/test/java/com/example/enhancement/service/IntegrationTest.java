package com.example.enhancement.service;

import com.example.enhancement.model.TextCleaningConfig;
import com.example.enhancement.model.LanguageDetectionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成測試 - 驗證核心功能
 */
@DisplayName("核心功能集成測試")
class IntegrationTest {

    @Test
    @DisplayName("測試文本清理服務基本功能")
    void testTextCleaningService() {
        // Given
        TextCleaningService service = new TextCleaningService();
        TextCleaningConfig config = TextCleaningConfig.builder()
                .language("zh")
                .removeExtraWhitespace(true)
                .removeSensitiveInfo(true)
                .normalizeLineBreaks(true)
                .build();

        String dirtyText = "這是測試文本。   包含多餘空格。\n\n\n";

        // When
        String cleanedText = service.cleanText(dirtyText, config);

        // Then
        assertNotNull(cleanedText);
        assertTrue(cleanedText.length() > 0);
        System.out.println("✓ 文本清理服務測試通過");
        System.out.println("  原始文本長度: " + dirtyText.length());
        System.out.println("  清理後長度: " + cleanedText.length());
    }

    @Test
    @DisplayName("測試語言檢測服務基本功能")
    void testLanguageDetectionService() {
        // Given
        LanguageDetectionService service = new LanguageDetectionService();

        // When
        LanguageDetectionResult zhResult = service.detectLanguage("這是中文測試文本");
        LanguageDetectionResult enResult = service.detectLanguage("This is English test text");
        LanguageDetectionResult jaResult = service.detectLanguage("これは日本語です");

        // Then
        assertNotNull(zhResult);
        assertNotNull(enResult);
        assertNotNull(jaResult);

        assertEquals("zh", zhResult.getLanguage());
        assertEquals("en", enResult.getLanguage());
        assertEquals("ja", jaResult.getLanguage());

        System.out.println("✓ 語言檢測服務測試通過");
        System.out.println("  中文檢測: " + zhResult.getLanguage() + " (置信度: " + zhResult.getConfidence() + ")");
        System.out.println("  英文檢測: " + enResult.getLanguage() + " (置信度: " + enResult.getConfidence() + ")");
        System.out.println("  日文檢測: " + jaResult.getLanguage() + " (置信度: " + jaResult.getConfidence() + ")");
    }

    @Test
    @DisplayName("測試文本清理和語言檢測整合")
    void testTextCleaningAndLanguageDetection() {
        // Given
        TextCleaningService cleaningService = new TextCleaningService();
        LanguageDetectionService languageService = new LanguageDetectionService();

        TextCleaningConfig config = TextCleaningConfig.builder()
                .language("zh")
                .removeExtraWhitespace(true)
                .removeSensitiveInfo(true)
                .build();

        String mixedText = "這是包含   多餘空格   和特殊字符的中文文本！";

        // When
        String cleanedText = cleaningService.cleanText(mixedText, config);
        LanguageDetectionResult langResult = languageService.detectLanguage(cleanedText);

        // Then
        assertNotNull(cleanedText);
        assertNotNull(langResult);
        assertEquals("zh", langResult.getLanguage());

        System.out.println("✓ 文本清理和語言檢測整合測試通過");
        System.out.println("  原始文本: " + mixedText);
        System.out.println("  清理後: " + cleanedText);
        System.out.println("  檢測語言: " + langResult.getLanguage());
    }

    @Test
    @DisplayName("測試多語言文本處理")
    void testMultiLanguageProcessing() {
        // Given
        TextCleaningService cleaningService = new TextCleaningService();
        LanguageDetectionService languageService = new LanguageDetectionService();

        String[] testTexts = {
            "這是一段中文測試文本，包含標點符號。",
            "This is an English test text with punctuation.",
            "これは日本語のテストテキストです。"
        };

        // When & Then
        for (String text : testTexts) {
            TextCleaningConfig config = TextCleaningConfig.builder()
                    .removeExtraWhitespace(true)
                    .build();

            String cleaned = cleaningService.cleanText(text, config);
            LanguageDetectionResult result = languageService.detectLanguage(cleaned);

            assertNotNull(cleaned);
            assertNotNull(result);
            assertNotNull(result.getLanguage());
            assertTrue(result.getConfidence() > 0.0);

            System.out.println("✓ 多語言處理: " + result.getLanguage() +
                             " (置信度: " + result.getConfidence() + ")");
        }
    }

    @Test
    @DisplayName("測試邊界條件處理")
    void testEdgeCases() {
        // Given
        TextCleaningService cleaningService = new TextCleaningService();
        LanguageDetectionService languageService = new LanguageDetectionService();

        TextCleaningConfig config = TextCleaningConfig.builder()
                .removeExtraWhitespace(true)
                .build();

        // 測試空字串
        String emptyText = "";
        String cleanedEmpty = cleaningService.cleanText(emptyText, config);
        LanguageDetectionResult emptyResult = languageService.detectLanguage(emptyText);

        assertNotNull(cleanedEmpty);
        assertEquals("", cleanedEmpty);
        assertEquals("unknown", emptyResult.getLanguage());

        System.out.println("✓ 邊界條件測試通過");
        System.out.println("  空字串處理正常");
        System.out.println("  空字串語言檢測: " + emptyResult.getLanguage());

        // 測試純空白字符
        String whitespaceText = "   \n\t   ";
        String cleanedWhitespace = cleaningService.cleanText(whitespaceText, config);

        assertNotNull(cleanedWhitespace);
        System.out.println("  純空白字符處理正常");
    }
}
