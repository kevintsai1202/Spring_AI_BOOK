package com.example.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 产品详情工具 - 用于 Tool 链调用
 * 提供产品型号和规格查询功能
 */
@Component
@Slf4j
public class ProductDetailsTools {

    /**
     * 产品详情数据模型
     */
    public record ProductDetail(
            String product,
            String productName,
            List<String> models,
            String category,
            String description
    ) {}

    /**
     * 产品型号数据模型
     */
    public record ProductModel(
            String modelCode,
            String modelName,
            String specifications,
            String price,
            String availability
    ) {}

    /**
     * 根据产品名称获取产品型号列表
     * @param product 产品代码或名称
     * @return 产品详情包含所有型号
     */
    public ProductDetail getProductModels(String product) {
        try {
            log.info("查询产品型号：{}", product);

            // 模拟企业产品资料库
            Map<String, ProductDetail> productDatabase = Map.of(
                "PD-1405", new ProductDetail(
                    "PD-1405",
                    "智能手机 Pro 系列",
                    List.of("1405-001", "1405-002", "1405-003", "1405-004"),
                    "智能手机",
                    "高端智能手机系列，支持 5G 和 AI 摄影"
                ),
                "PD-1234", new ProductDetail(
                    "PD-1234",
                    "笔记型电脑 Ultra 系列",
                    List.of("1234-1", "1234-2", "1234-3", "1234-4", "1234-5"),
                    "笔记型电脑",
                    "轻薄高效能笔记型电脑，适合商务和创作"
                ),
                "PD-1235", new ProductDetail(
                    "PD-1235",
                    "平板电脑系列",
                    List.of("1235-4", "1235-5", "1235-6"),
                    "平板电脑",
                    "多功能平板电脑，支持手写笔和键盘"
                ),
                "PD-1385", new ProductDetail(
                    "PD-1385",
                    "智能手表系列",
                    List.of("1385-1", "1385-2", "1385-3", "1385-4"),
                    "穿戴装置",
                    "健康监测智能手表，支持运动追踪和心率监测"
                ),
                "PD-1255", new ProductDetail(
                    "PD-1255",
                    "无线耳机系列",
                    List.of("1255-1", "1255-2"),
                    "音响设备",
                    "主动降噪无线耳机，高品质音效体验"
                ),
                "PD-1300", new ProductDetail(
                    "PD-1300",
                    "智能音箱系列",
                    List.of("1300-1", "1300-2", "1300-3"),
                    "智能家居",
                    "AI 语音助手智能音箱，支持智能家居控制"
                )
            );

            ProductDetail result = productDatabase.get(product);
            if (result == null) {
                // 尝试模糊匹配
                result = productDatabase.values().stream()
                        .filter(pd -> pd.productName().toLowerCase().contains(product.toLowerCase()))
                        .findFirst()
                        .orElse(new ProductDetail(
                                product,
                                "未知产品",
                                List.of("无此产品型号"),
                                "未分类",
                                "查无此产品信息"
                        ));
            }

            log.info("查询完成，产品：{}，型号数量：{}",
                    result.productName(), result.models().size());

            return result;

        } catch (Exception e) {
            log.error("查询产品型号失败：{}", product, e);
            return new ProductDetail(
                    product,
                    "查询失败",
                    List.of("系统错误"),
                    "错误",
                    "查询产品信息时发生错误：" + e.getMessage()
            );
        }
    }

    /**
     * 获取特定型号的详细规格
     * @param productCode 产品代码
     * @param modelCode 型号代码
     * @return 型号详细信息
     */
    public ProductModel getModelSpecifications(String productCode, String modelCode) {
        try {
            log.info("查询型号规格：{}-{}", productCode, modelCode);

            // 模拟型号规格资料库
            String fullModelCode = productCode + "-" + modelCode;

            // 根据产品类型生成规格信息
            ProductModel model = switch (productCode) {
                case "PD-1405" -> new ProductModel(
                        modelCode,
                        "智能手机 Pro " + modelCode,
                        generatePhoneSpecs(modelCode),
                        generatePrice("phone", modelCode),
                        "现货供应"
                );
                case "PD-1234" -> new ProductModel(
                        modelCode,
                        "笔记型电脑 Ultra " + modelCode,
                        generateLaptopSpecs(modelCode),
                        generatePrice("laptop", modelCode),
                        "现货供应"
                );
                case "PD-1385" -> new ProductModel(
                        modelCode,
                        "智能手表 " + modelCode,
                        generateWatchSpecs(modelCode),
                        generatePrice("watch", modelCode),
                        "现货供应"
                );
                default -> new ProductModel(
                        modelCode,
                        "产品型号 " + modelCode,
                        "标准规格",
                        "价格面议",
                        "请洽询"
                );
            };

            log.info("型号规格查询完成：{}", model.modelName());
            return model;

        } catch (Exception e) {
            log.error("查询型号规格失败：{}-{}", productCode, modelCode, e);
            return new ProductModel(
                    modelCode,
                    "查询失败",
                    "无法获取规格信息",
                    "价格未知",
                    "查询错误"
            );
        }
    }

    private String generatePhoneSpecs(String modelCode) {
        return switch (modelCode) {
            case "1405-001" -> "6.1吋 OLED 屏幕, 128GB 存储, 双镜头";
            case "1405-002" -> "6.1吋 OLED 屏幕, 256GB 存储, 三镜头";
            case "1405-003" -> "6.7吋 OLED 屏幕, 512GB 存储, 三镜头 Pro";
            case "1405-004" -> "6.7吋 OLED 屏幕, 1TB 存储, 四镜头 Pro Max";
            default -> "标准智能手机规格";
        };
    }

    private String generateLaptopSpecs(String modelCode) {
        return switch (modelCode) {
            case "1234-1" -> "13吋, Intel i5, 8GB RAM, 256GB SSD";
            case "1234-2" -> "13吋, Intel i7, 16GB RAM, 512GB SSD";
            case "1234-3" -> "15吋, Intel i7, 16GB RAM, 1TB SSD";
            case "1234-4" -> "15吋, Intel i9, 32GB RAM, 1TB SSD";
            case "1234-5" -> "16吋, Intel i9, 64GB RAM, 2TB SSD";
            default -> "标准笔记型电脑规格";
        };
    }

    private String generateWatchSpecs(String modelCode) {
        return switch (modelCode) {
            case "1385-1" -> "41mm, GPS, 心率监测";
            case "1385-2" -> "45mm, GPS, 心率监测, 血氧检测";
            case "1385-3" -> "41mm, GPS+Cellular, 心率监测, 血氧检测";
            case "1385-4" -> "45mm, GPS+Cellular, 心率监测, 血氧检测, ECG";
            default -> "标准智能手表规格";
        };
    }

    private String generatePrice(String category, String modelCode) {
        return switch (category) {
            case "phone" -> switch (modelCode) {
                case "1405-001" -> "NT$ 25,900";
                case "1405-002" -> "NT$ 29,900";
                case "1405-003" -> "NT$ 35,900";
                case "1405-004" -> "NT$ 42,900";
                default -> "NT$ 25,000";
            };
            case "laptop" -> switch (modelCode) {
                case "1234-1" -> "NT$ 35,900";
                case "1234-2" -> "NT$ 45,900";
                case "1234-3" -> "NT$ 55,900";
                case "1234-4" -> "NT$ 75,900";
                case "1234-5" -> "NT$ 95,900";
                default -> "NT$ 40,000";
            };
            case "watch" -> switch (modelCode) {
                case "1385-1" -> "NT$ 12,900";
                case "1385-2" -> "NT$ 15,900";
                case "1385-3" -> "NT$ 18,900";
                case "1385-4" -> "NT$ 22,900";
                default -> "NT$ 15,000";
            };
            default -> "价格面议";
        };
    }
}
