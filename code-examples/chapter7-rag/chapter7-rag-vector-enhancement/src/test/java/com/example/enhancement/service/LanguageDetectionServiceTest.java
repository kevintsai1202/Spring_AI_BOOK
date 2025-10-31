package com.example.enhancement.service;

import com.example.enhancement.model.LanguageDetectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LanguageDetectionService 單元測試
 */
@DisplayName("語言檢測服務測試")
class LanguageDetectionServiceTest {

    private LanguageDetectionService languageDetectionService;

    @BeforeEach
    void setUp() {
        languageDetectionService = new LanguageDetectionService();
    }

    @Test
    @DisplayName("測試中文檢測")
    void testChineseDetection() {
        // Given
        String chineseText = "這是一段中文測試文本，用於驗證語言檢測功能是否正常運作。";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(chineseText);

        // Then
        assertNotNull(result);
        assertEquals("zh", result.getLanguage());
        assertTrue(result.getConfidence() > 0.0);
        assertTrue(result.getConfidence() <= 1.0);
    }

    @Test
    @DisplayName("測試英文檢測")
    void testEnglishDetection() {
        // Given
        String englishText = "This is an English text sample for testing the language detection functionality.";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(englishText);

        // Then
        assertNotNull(result);
        assertEquals("en", result.getLanguage());
        assertTrue(result.getConfidence() > 0.0);
        assertTrue(result.getConfidence() <= 1.0);
    }

    @Test
    @DisplayName("測試日文檢測")
    void testJapaneseDetection() {
        // Given
        String japaneseText = "これは日本語のテストテキストです。言語検出機能をテストしています。";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(japaneseText);

        // Then
        assertNotNull(result);
        assertEquals("ja", result.getLanguage());
        assertTrue(result.getConfidence() > 0.0);
        assertTrue(result.getConfidence() <= 1.0);
    }

    @Test
    @DisplayName("測試混合語言文本（中英混合）")
    void testMixedLanguageText() {
        // Given
        String mixedText = "這是一段包含 English words 的混合文本";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(mixedText);

        // Then
        assertNotNull(result);
        assertNotNull(result.getLanguage());
        // 混合文本應該檢測為主要語言（中文字符較多）
        assertTrue(result.getLanguage().equals("zh") || result.getLanguage().equals("en"));
    }

    @Test
    @DisplayName("測試空字串")
    void testEmptyString() {
        // Given
        String emptyText = "";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(emptyText);

        // Then
        assertNotNull(result);
        assertEquals("unknown", result.getLanguage());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    @DisplayName("測試純數字文本")
    void testNumericText() {
        // Given
        String numericText = "1234567890";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(numericText);

        // Then
        assertNotNull(result);
        // 純數字應該被識別為 unknown
        assertTrue(result.getLanguage().equals("unknown") || result.getLanguage().equals("en"));
    }

    @Test
    @DisplayName("測試純標點符號文本")
    void testPunctuationOnlyText() {
        // Given
        String punctuationText = "!@#$%^&*().,;:?";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(punctuationText);

        // Then
        assertNotNull(result);
        assertEquals("unknown", result.getLanguage());
    }

    @Test
    @DisplayName("測試短文本")
    void testShortText() {
        // Given
        String shortChineseText = "中文";
        String shortEnglishText = "Hi";

        // When
        LanguageDetectionResult chineseResult = languageDetectionService.detectLanguage(shortChineseText);
        LanguageDetectionResult englishResult = languageDetectionService.detectLanguage(shortEnglishText);

        // Then
        assertNotNull(chineseResult);
        assertNotNull(englishResult);
        assertEquals("zh", chineseResult.getLanguage());
        assertEquals("en", englishResult.getLanguage());
    }

    @Test
    @DisplayName("測試長文本")
    void testLongText() {
        // Given
        String longChineseText = "這是一段很長的中文測試文本。" +
                "它包含了多個句子和段落，用於測試語言檢測服務在處理長文本時的表現。" +
                "長文本通常能提供更多的語言特徵，從而提高檢測的準確性。" +
                "我們希望這個測試能夠驗證服務在實際使用場景中的可靠性。";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(longChineseText);

        // Then
        assertNotNull(result);
        assertEquals("zh", result.getLanguage());
        assertTrue(result.getConfidence() >= 0.8, "長文本的置信度應該較高");
    }

    @Test
    @DisplayName("測試繁體中文")
    void testTraditionalChinese() {
        // Given
        String traditionalChineseText = "這是繁體中文測試文本，與簡體中文稍有不同。";

        // When
        LanguageDetectionResult result = languageDetectionService.detectLanguage(traditionalChineseText);

        // Then
        assertNotNull(result);
        assertEquals("zh", result.getLanguage());
        assertTrue(result.getConfidence() > 0.0);
    }
}
