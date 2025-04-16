package com.hhu.Service.impl;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务类，提供订单查询相关的功能
 */
@Service
@Slf4j
public class OrderService {

    // 模拟订单数据库
    private final Map<String, Order> orderDatabase = new HashMap<>();
    
    // 模拟商品名称与ID的映射
    private final Map<String, String> productNameToId = new HashMap<>();
    
    // 模拟用户手机尾号与用户ID的映射
    private final Map<String, String> phoneToUserId = new HashMap<>();
    
    // 模拟用户ID与订单的映射
    private final Map<String, String> userIdToOrderIds = new HashMap<>();

    public OrderService() {
        // 初始化模拟数据
        initMockData();
    }

    @Tool("根据产品ID查询订单状态，返回订单的详细信息")
    public String queryOrderByProductId(
            @P("产品的唯一标识符，例如P001") String productId) {
        
        log.info("通过产品ID查询订单: {}", productId);
        
        if (!orderDatabase.containsKey(productId)) {
            return "未找到与产品ID " + productId + " 相关的订单信息。";
        }
        
        Order order = orderDatabase.get(productId);
        return formatOrderInfo(order);
    }

    @Tool("根据产品名称查询订单状态，返回订单的详细信息")
    public String queryOrderByProductName(
            @P("产品的名称，例如智能手机、笔记本电脑等") String productName) {
        
        log.info("通过产品名称查询订单: {}", productName);
        
        if (!productNameToId.containsKey(productName)) {
            return "未找到名为 " + productName + " 的产品信息。";
        }
        
        String productId = productNameToId.get(productName);
        return queryOrderByProductId(productId);
    }

    @Tool("根据用户手机尾号（后四位）查询订单")
    public String queryOrderByPhoneLastDigits(
            @P("用户手机号的后四位数字") String phoneLastDigits) {
        
        log.info("通过手机尾号查询订单: {}", phoneLastDigits);
        
        if (!phoneToUserId.containsKey(phoneLastDigits)) {
            return "未找到与手机尾号 " + phoneLastDigits + " 关联的用户。";
        }
        
        String userId = phoneToUserId.get(phoneLastDigits);
        if (!userIdToOrderIds.containsKey(userId)) {
            return "该用户没有订单记录。";
        }
        
        String productId = userIdToOrderIds.get(userId);
        return queryOrderByProductId(productId);
    }

    @Tool("当用户提供的信息不足以查询订单时，请求用户提供更多信息")
    public String requestMoreInfo(
            @P("缺失的信息类型，例如'productId'、'phoneNumber'等") String missingInfo) {
        
        log.info("请求用户提供更多信息: {}", missingInfo);
        
        switch (missingInfo) {
            case "productId":
                return "为了帮您查询订单，请提供产品ID（例如P001）或产品名称。";
            case "phoneNumber":
                return "为了帮您查询订单，请提供您的手机号码后四位。";
            case "orderNumber":
                return "为了帮您查询订单，请提供订单号。";
            default:
                return "为了帮您查询订单，请提供更多信息，例如产品ID、产品名称或手机号码后四位。";
        }
    }

    private String formatOrderInfo(Order order) {
        return String.format(
                "订单信息：\n产品ID：%s\n产品名称：%s\n订单状态：%s\n预计发货时间：%s\n物流信息：%s",
                order.getProductId(),
                order.getProductName(),
                order.getStatus(),
                order.getExpectedDeliveryDate(),
                order.getLogisticsInfo()
        );
    }

    private void initMockData() {
        // 创建模拟订单
        Order order1 = new Order("P001", "智能手机", "已发货", "2023-04-10", "顺丰快递：SF1234567890，已到达配送站");
        Order order2 = new Order("P002", "笔记本电脑", "待发货", "2023-04-15", "正在仓库备货中");
        Order order3 = new Order("P003", "智能手表", "已签收", "2023-04-05", "已于2023-04-06签收");
        
        // 存储订单数据
        orderDatabase.put("P001", order1);
        orderDatabase.put("P002", order2);
        orderDatabase.put("P003", order3);
        
        // 建立产品名称与ID的映射
        productNameToId.put("智能手机", "P001");
        productNameToId.put("笔记本电脑", "P002");
        productNameToId.put("智能手表", "P003");
        
        // 建立用户手机尾号与用户ID的映射
        phoneToUserId.put("1234", "U001");
        phoneToUserId.put("5678", "U002");
        phoneToUserId.put("9012", "U003");
        
        // 建立用户ID与订单的映射
        userIdToOrderIds.put("U001", "P001");
        userIdToOrderIds.put("U002", "P002");
        userIdToOrderIds.put("U003", "P003");
    }

    @Data
    static class Order {
        private final String productId;
        private final String productName;
        private final String status;
        private final String expectedDeliveryDate;
        private final String logisticsInfo;
    }
} 