package com.example.memory.service;

import com.example.memory.advisor.Advisor;
import com.example.memory.advisor.AdvisorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AdvisorChainService 測試
 */
class AdvisorChainServiceTest {

    /**
     * 簡單的測試 Advisor 實現
     */
    private static class TestAdvisor implements Advisor {
        private String name;
        private int order;
        private List<String> executionLog = new ArrayList<>();

        TestAdvisor(String name, int order) {
            this.name = name;
            this.order = order;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public AdvisorContext adviseRequest(AdvisorContext context) {
            executionLog.add(name + " - adviseRequest");
            context.putMetadata(name + "_visited", true);
            return context;
        }

        @Override
        public AdvisorContext adviseResponse(AdvisorContext context) {
            executionLog.add(name + " - adviseResponse");
            return context;
        }

        public List<String> getExecutionLog() {
            return executionLog;
        }

        public void clearLog() {
            executionLog.clear();
        }
    }

    /**
     * 測試 Advisor 執行順序
     */
    @Test
    void testAdvisorExecutionOrder() {
        // 創建多個 Advisor，故意亂序
        TestAdvisor advisor3 = new TestAdvisor("Advisor3", 300);
        TestAdvisor advisor1 = new TestAdvisor("Advisor1", 100);
        TestAdvisor advisor2 = new TestAdvisor("Advisor2", 200);

        // 模擬執行
        List<Advisor> advisors = new ArrayList<>();
        advisors.add(advisor3);
        advisors.add(advisor1);
        advisors.add(advisor2);

        // 按優先級排序
        advisors.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));

        // 驗證排序結果
        assertEquals("Advisor1", advisors.get(0).getName());
        assertEquals("Advisor2", advisors.get(1).getName());
        assertEquals("Advisor3", advisors.get(2).getName());
    }

    /**
     * 測試 Advisor 上下文修改
     */
    @Test
    void testAdvisorContextModification() {
        TestAdvisor advisor = new TestAdvisor("TestAdvisor", 0);

        AdvisorContext context = AdvisorContext.success("conv-123", "Hello");

        // 執行 Advisor
        context = advisor.adviseRequest(context);

        // 驗證修改
        assertTrue(context.hasMetadata("TestAdvisor_visited"));
        Object visited = context.getMetadata("TestAdvisor_visited");
        assertTrue((Boolean) visited);
    }

    /**
     * 測試 Advisor 鏈式執行
     */
    @Test
    void testAdvisorChainExecution() {
        TestAdvisor advisor1 = new TestAdvisor("First", 0);
        TestAdvisor advisor2 = new TestAdvisor("Second", 100);
        TestAdvisor advisor3 = new TestAdvisor("Third", 200);

        AdvisorContext context = AdvisorContext.success("conv-123", "Test Message");

        // 模擬執行
        context = advisor1.adviseRequest(context);
        context = advisor2.adviseRequest(context);
        context = advisor3.adviseRequest(context);

        // 驗證所有 Advisor 都被執行了
        assertTrue(context.hasMetadata("First_visited"));
        assertTrue(context.hasMetadata("Second_visited"));
        assertTrue(context.hasMetadata("Third_visited"));

        // 驗證執行順序
        List<String> execution = new ArrayList<>();
        execution.addAll(advisor1.getExecutionLog());
        execution.addAll(advisor2.getExecutionLog());
        execution.addAll(advisor3.getExecutionLog());

        assertEquals(3, execution.size());
        assertEquals("First - adviseRequest", execution.get(0));
        assertEquals("Second - adviseRequest", execution.get(1));
        assertEquals("Third - adviseRequest", execution.get(2));
    }

    /**
     * 測試錯誤處理
     */
    @Test
    void testErrorHandling() {
        AdvisorContext context = AdvisorContext.failure("conv-123", "Test Error");

        assertTrue(context.isError());
        assertEquals("Test Error", context.getErrorMessage());
    }

    /**
     * 測試 Advisor 優先級計算
     */
    @Test
    void testAdvisorOrdering() {
        // 安全檢查優先級最高（最先執行）
        TestAdvisor securityAdvisor = new TestAdvisor("SecurityAdvisor", -1000);
        // 記憶管理
        TestAdvisor memoryAdvisor = new TestAdvisor("MemoryAdvisor", 0);
        // 內容過濾
        TestAdvisor filterAdvisor = new TestAdvisor("FilterAdvisor", 100);
        // 日誌記錄
        TestAdvisor loggingAdvisor = new TestAdvisor("LoggingAdvisor", 800);

        List<Advisor> advisors = new ArrayList<>();
        advisors.add(loggingAdvisor);
        advisors.add(filterAdvisor);
        advisors.add(securityAdvisor);
        advisors.add(memoryAdvisor);

        // 按優先級排序
        advisors.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));

        // 驗證排序順序：優先級從小到大
        assertEquals(-1000, advisors.get(0).getOrder());
        assertEquals(0, advisors.get(1).getOrder());
        assertEquals(100, advisors.get(2).getOrder());
        assertEquals(800, advisors.get(3).getOrder());
    }
}
