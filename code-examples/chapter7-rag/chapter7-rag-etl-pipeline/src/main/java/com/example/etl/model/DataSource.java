package com.example.etl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * 資料來源模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSource {
    /** 資料源名稱 */
    private String name;

    /** 資料源類型 */
    private DataSourceType type;

    /** 資源物件 */
    private Resource resource;

    /** 文件路徑 */
    private String path;

    /** 自定義元資料 */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
