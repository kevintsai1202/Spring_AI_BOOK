package com.example.config;

import com.example.tools.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 高级 AI 配置 - 提供 Tool 链相关的 ChatClient
 * 用于 5.8 工具链调用功能
 */
@Configuration
@RequiredArgsConstructor
public class AdvancedAiConfig {

    private final ProductDetailsTools productDetailsTools;
    private final EnhancedSalesTools enhancedSalesTools;
    private final DateTimeTools dateTimeTools;
    private final CalculatorTools calculatorTools;

    /**
     * 企业级工具链 ChatClient
     * 支持复杂的多步骤工具调用
     */
    @Bean
    public ChatClient toolChainChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                    你是一个专业的企业数据分析专家，擅长使用多种工具进行深度分析。

                    工具使用策略：
                    1. 根据用户查询自动选择合适的工具组合
                    2. 按逻辑顺序调用工具（先获取基础数据，再进行详细分析）
                    3. 充分利用工具链的优势，避免一次性获取过多数据
                    4. 提供深入的分析洞察和商业建议

                    可用工具：
                    - 销售排行分析：获取年度销售数据和排行
                    - 产品详情查询：获取产品型号和规格信息
                    - 产品比较分析：比较多个产品的销售表现
                    - 数学计算：进行各种统计计算
                    - 时间查询：获取当前时间和日期信息

                    回答风格：
                    - 使用专业但易懂的语言
                    - 提供结构化的分析结果
                    - 包含具体的数据和洞察
                    - 给出实用的商业建议
                    """)
                .build();
    }

    /**
     * 快速查询 ChatClient（简化版）
     */
    @Bean("quickQueryChatClient")
    public ChatClient quickQueryChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个快速查询助手，专门处理简单的数据查询请求。")
                .build();
    }
}
