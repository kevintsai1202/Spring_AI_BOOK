package com.example.mcp.server.advanced.provider;

import com.example.mcp.server.advanced.entity.CompletionData;
import com.example.mcp.server.advanced.repository.CompletionDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult.CompleteCompletion;
import org.springaicommunity.mcp.annotation.McpComplete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MCP 自動完成提供者
 * 提供 URI 補全和 Prompt 參數補全功能
 */
@Service
@Slf4j
public class CompletionProvider {

    @Autowired
    private CompletionDataRepository completionDataRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 記憶體快取（用於快速查詢）
    private Map<String, List<String>> usernameCache;
    private Map<String, List<String>> countryCache;
    private Map<String, List<String>> languageCache;

    /**
     * 初始化補全數據快取
     */
    @PostConstruct
    public void initializeCache() {
        log.info("初始化補全數據快取");

        // 從資料庫載入數據到快取
        loadCacheFromDatabase("username");
        loadCacheFromDatabase("country");
        loadCacheFromDatabase("language");

        log.info("補全數據快取初始化完成");
    }

    /**
     * 從資料庫載入數據到快取
     */
    private void loadCacheFromDatabase(String category) {
        Optional<CompletionData> dataOpt = completionDataRepository.findByCategory(category);
        if (dataOpt.isPresent()) {
            List<String> values = dataOpt.get().getValues();
            switch (category) {
                case "username" -> usernameCache = groupByFirstLetter(values);
                case "country" -> countryCache = groupByFirstLetter(values);
                case "language" -> languageCache = groupByFirstLetter(values);
            }
            log.debug("載入 {} 筆 {} 補全數據", values.size(), category);
        } else {
            // 如果資料庫中沒有數據，使用預設值
            log.warn("資料庫中未找到 {} 類別的補全數據，使用預設值", category);
            initializeDefaultData(category);
        }
    }

    /**
     * 初始化預設補全數據
     */
    private void initializeDefaultData(String category) {
        switch (category) {
            case "username" -> usernameCache = Map.of(
                "a", List.of("alice", "andrew", "amanda"),
                "b", List.of("bob", "betty", "brian"),
                "c", List.of("charlie", "chris", "carol"),
                "j", List.of("john", "jane", "jack")
            );
            case "country" -> countryCache = Map.of(
                "j", List.of("Japan", "Jordan"),
                "t", List.of("Taiwan", "Thailand", "Turkey"),
                "u", List.of("United States", "United Kingdom", "Uruguay")
            );
            case "language" -> languageCache = Map.of(
                "j", List.of("Java", "JavaScript"),
                "p", List.of("Python", "PHP", "Perl"),
                "r", List.of("Ruby", "Rust", "R")
            );
        }
    }

    /**
     * 將列表按首字母分組
     */
    private Map<String, List<String>> groupByFirstLetter(List<String> values) {
        Map<String, List<String>> grouped = new HashMap<>();
        for (String value : values) {
            if (value == null || value.isEmpty()) continue;
            String firstLetter = value.substring(0, 1).toLowerCase();
            grouped.computeIfAbsent(firstLetter, k -> new ArrayList<>()).add(value);
        }
        return grouped;
    }

    /**
     * 用戶名補全（URI 補全）
     * 為 user-profile://{username} URI 提供補全建議
     *
     * @param usernamePrefix 用戶名前綴
     * @return 補全建議列表
     */
    @McpComplete(uri = "user-profile://{username}")
    public List<String> completeUsername(String usernamePrefix) {
        log.info("用戶名 URI 補全: prefix={}", usernamePrefix);

        if (usernamePrefix == null || usernamePrefix.isEmpty()) {
            return List.of("Enter a username (e.g., alice, bob, charlie)");
        }

        // 轉換為小寫進行比對
        String prefix = usernamePrefix.toLowerCase();
        String firstLetter = prefix.substring(0, 1);

        List<String> usernames = usernameCache.getOrDefault(firstLetter, new ArrayList<>());

        List<String> matches = usernames.stream()
            .filter(u -> u.toLowerCase().startsWith(prefix))
            .sorted()
            .limit(10)  // 限制返回數量
            .toList();

        log.debug("找到 {} 個匹配的用戶名", matches.size());
        return matches;
    }

    /**
     * 國家名補全（Prompt 參數補全）
     * 為 travel-plan prompt 的 country 參數提供補全
     *
     * @param request 補全請求
     * @return 補全結果
     */
    @McpComplete(prompt = "travel-plan")
    public McpSchema.CompleteResult completeCountryName(McpSchema.CompleteRequest request) {
        log.info("國家名 Prompt 補全: request={}", request);

        // 取得參數值並處理為最終值
        String rawPrefix = request.argument() != null ?
            request.argument().value() : "";

        if (rawPrefix == null) {
            rawPrefix = "";
        }

        final String prefix = rawPrefix.toLowerCase();

        if (prefix.isEmpty()) {
            // 返回提示訊息
            return new McpSchema.CompleteResult(
                new CompleteCompletion(
                    List.of("Taiwan", "Thailand", "Turkey", "Tunisia"),
                    4,
                    false
                )
            );
        }

        // 查找匹配的國家
        String firstLetter = prefix.substring(0, 1);
        List<String> countries = countryCache.getOrDefault(firstLetter, new ArrayList<>());

        List<String> matches = countries.stream()
            .filter(c -> c.toLowerCase().startsWith(prefix))
            .sorted()
            .limit(10)
            .toList();

        log.debug("找到 {} 個匹配的國家", matches.size());

        return new McpSchema.CompleteResult(
            new CompleteCompletion(
                matches,
                matches.size(),
                false  // hasMore
            )
        );
    }

    /**
     * 程式語言補全（Prompt 參數補全）
     * 為 code-review prompt 的 language 參數提供補全
     *
     * @param request 補全請求
     * @return 補全結果
     */
    @McpComplete(prompt = "code-review")
    public McpSchema.CompleteResult completeLanguageName(McpSchema.CompleteRequest request) {
        log.info("程式語言 Prompt 補全: request={}", request);

        String rawPrefix = request.argument() != null ?
            request.argument().value() : "";

        if (rawPrefix == null || rawPrefix.isEmpty()) {
            // 返回常用語言
            return new McpSchema.CompleteResult(
                new CompleteCompletion(
                    List.of("Java", "JavaScript", "Python", "C++", "Go"),
                    5,
                    true  // 還有更多
                )
            );
        }

        final String prefix = rawPrefix.toLowerCase();
        String firstLetter = prefix.substring(0, 1);
        List<String> languages = languageCache.getOrDefault(firstLetter, new ArrayList<>());

        List<String> matches = languages.stream()
            .filter(l -> l.toLowerCase().startsWith(prefix))
            .sorted()
            .limit(10)
            .toList();

        log.debug("找到 {} 個匹配的語言", matches.size());

        return new McpSchema.CompleteResult(
            new CompleteCompletion(
                matches,
                matches.size(),
                false
            )
        );
    }

    /**
     * 用戶屬性補全（URI 補全）
     * 為 user-attribute://{username}/{attribute} URI 提供屬性名稱補全
     *
     * @param attributePrefix 屬性前綴
     * @return 補全建議列表
     */
    @McpComplete(uri = "user-attribute://{username}/{attribute}")
    public List<String> completeUserAttribute(String attributePrefix) {
        log.info("用戶屬性 URI 補全: prefix={}", attributePrefix);

        List<String> attributes = List.of(
            "email", "age", "city", "country", "phone",
            "role", "department", "status", "created_date"
        );

        if (attributePrefix == null || attributePrefix.isEmpty()) {
            return attributes;
        }

        String prefix = attributePrefix.toLowerCase();
        List<String> matches = attributes.stream()
            .filter(attr -> attr.toLowerCase().startsWith(prefix))
            .sorted()
            .toList();

        log.debug("找到 {} 個匹配的屬性", matches.size());
        return matches;
    }

    /**
     * 重新載入補全數據快取
     * 當資料庫數據更新時可調用此方法刷新快取
     */
    public void reloadCache() {
        log.info("重新載入補全數據快取");
        initializeCache();
    }
}
