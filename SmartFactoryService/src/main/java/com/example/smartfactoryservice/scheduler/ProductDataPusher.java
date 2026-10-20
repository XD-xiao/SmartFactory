package com.example.smartfactoryservice.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduct;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduction;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductStore;
import com.example.smartfactoryservice.Domain.Vo.Product.ProductVO;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductionMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductStoreMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * 大屏产品数据推送任务。
 * 仅在有客户端订阅 /topic/product 时才执行查询和推送，
 * 最后一个订阅者离开后自动停止。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDataPusher {

    private static final String TOPIC = "/topic/product";
    private static final long INTERVAL_MS = 3000;

    /** 入库动作 */
    private static final int ACTION_INBOUND = 1;
    /** 出库动作 */
    private static final int ACTION_OUTBOUND = 2;

    private final SimpMessagingTemplate messagingTemplate;
    private final ProductStoreMapper productStoreMapper;
    private final ProductProductionMapper productProductionMapper;
    private final ProductProductMapper productProductMapper;
    private final SubscriptionTracker subscriptionTracker;
    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledFuture;
    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        subscriptionTracker.register(TOPIC, this::startPushing, this::stopPushing);
    }

    private void startPushing() {
        synchronized (lock) {
            if (scheduledFuture == null || scheduledFuture.isCancelled()) {
                // 立即推送首帧数据，消除首次 3s 延迟
                pushProductData();
                scheduledFuture = taskScheduler.scheduleAtFixedRate(
                        this::pushProductData,
                        new Date(System.currentTimeMillis() + INTERVAL_MS),
                        INTERVAL_MS);
                log.info("产品数据推送已启动: topic={}, interval={}ms", TOPIC, INTERVAL_MS);
            }
        }
    }

    private void stopPushing() {
        synchronized (lock) {
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
                scheduledFuture = null;
                log.info("产品数据推送已停止: topic={}", TOPIC);
            }
        }
    }

    private void pushProductData() {
        try {
            ProductVO data = buildProductData();
            messagingTemplate.convertAndSend(TOPIC, data);
            log.debug("产品数据推送成功: totalInbound={}, totalOutput={}, plans={}",
                    data.getTotalInbound(), data.getTotalOutput(),
                    data.getPlans() != null ? data.getPlans().size() : 0);
        } catch (Exception e) {
            log.error("推送产品数据失败", e);
        }
    }

    private ProductVO buildProductData() {
        // 查询所有出入库记录
        List<ProductStore> allStores = productStoreMapper.selectList(null);

        // 查询所有生产计划
        List<ProductProduction> allProductions = productProductionMapper.selectList(null);

        // 查询所有未删除的产品
        List<ProductProduct> allProducts = productProductMapper.selectList(
                new QueryWrapper<ProductProduct>().eq("delete_flag", false));

        // 构建 productId -> ProductProduct 映射
        Map<Long, ProductProduct> productMap = allProducts.stream()
                .collect(Collectors.toMap(ProductProduct::getId, p -> p, (p1, p2) -> p1));

        // 统计总体数据
        int totalInbound = allStores.stream()
                .filter(s -> s.getAction() != null && s.getAction() == ACTION_INBOUND)
                .mapToInt(s -> 1)
                .sum();

        int totalOutbound = allStores.stream()
                .filter(s -> s.getAction() != null && s.getAction() == ACTION_OUTBOUND)
                .mapToInt(s -> 1)
                .sum();

        int totalOutput = allProductions.stream()
                .mapToInt(p -> p.getActualProduction() != null ? p.getActualProduction() : 0)
                .sum();

        int totalPlanned = allProductions.stream()
                .mapToInt(p -> p.getPlannedProduction() != null ? p.getPlannedProduction() : 0)
                .sum();

        int totalQualified = allProductions.stream()
                .mapToInt(p -> p.getQualifiedNum() != null ? p.getQualifiedNum() : 0)
                .sum();

        double qualifyRate = totalInbound > 0
                ? Math.min(100.0, Math.round((double) totalQualified / totalInbound * 1000) / 10.0)
                : 0.0;

        // 近30日出入库趋势
        List<Integer> inboundSeries = buildDailySeries(allStores, ACTION_INBOUND, 30);
        List<Integer> outboundSeries = buildDailySeries(allStores, ACTION_OUTBOUND, 30);

        // 生产计划列表
        List<ProductVO.PlanItem> plans = new ArrayList<>();
        for (ProductProduction pp : allProductions) {
            String planName = pp.getPlanName();
            if (planName == null || planName.isBlank()) {
                ProductProduct product = productMap.get(pp.getProductId());
                planName = product != null ? product.getProductCode() : "未知计划";
            }

            int planned = pp.getPlannedProduction() != null ? pp.getPlannedProduction() : 0;
            int actual = pp.getActualProduction() != null ? pp.getActualProduction() : 0;
            int qualified = pp.getQualifiedNum() != null ? pp.getQualifiedNum() : 0;
            int storeNum = pp.getStoreNum() != null ? pp.getStoreNum() : 0;

            int rate = planned > 0 ? (int) Math.round((double) actual / planned * 100) : 0;

            plans.add(new ProductVO.PlanItem(
                    pp.getId(), planName, planned, actual, qualified, storeNum, rate));
        }

        return new ProductVO(totalInbound, totalQualified, totalOutput, totalPlanned,
                qualifyRate, inboundSeries, outboundSeries, plans);
    }

    /**
     * 按天统计近N日出入库数量
     */
    private List<Integer> buildDailySeries(List<ProductStore> stores, int action, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        // 按日期分组统计
        Map<LocalDate, Integer> dailyMap = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            dailyMap.put(startDate.plusDays(i), 0);
        }

        for (ProductStore store : stores) {
            if (store.getAction() == null || store.getAction() != action) {
                continue;
            }
            LocalDateTime dt = store.getDatetime();
            if (dt == null) {
                continue;
            }
            LocalDate date = dt.toLocalDate();
            if (!date.isBefore(startDate) && !date.isAfter(today)) {
                dailyMap.merge(date, 1, Integer::sum);
            }
        }

        return new ArrayList<>(dailyMap.values());
    }
}
