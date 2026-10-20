package com.example.smartfactoryservice.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * STOMP 订阅追踪器。
 * <p>
 * 监听客户端 subscribe / unsubscribe 事件，维护每个 topic 的订阅者计数。
 * 业务方通过 {@link #register} 注册 topic 的启动/停止回调：
 * 首个订阅者接入时触发 start 回调，最后一个订阅者离开时触发 stop 回调。
 * </p>
 */
@Slf4j
@Component
public class SubscriptionTracker {

    /** topic → 当前订阅者计数 */
    private final ConcurrentHashMap<String, AtomicInteger> topicSubscribers = new ConcurrentHashMap<>();
    /** topic → 启动回调 */
    private final ConcurrentHashMap<String, Runnable> onFirstSubscribe = new ConcurrentHashMap<>();
    /** topic → 停止回调 */
    private final ConcurrentHashMap<String, Runnable> onLastUnsubscribe = new ConcurrentHashMap<>();

    /**
     * 注册一个 topic，并绑定启动/停止回调。
     * 在 {@code @PostConstruct} 中调用，确保事件到达前已完成注册。
     */
    public void register(String topic, Runnable start, Runnable stop) {
        onFirstSubscribe.put(topic, start);
        onLastUnsubscribe.put(topic, stop);
        topicSubscribers.putIfAbsent(topic, new AtomicInteger(0));
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        String destination = StompHeaderAccessor.wrap(event.getMessage()).getDestination();
        if (destination == null) {
            return;
        }
        AtomicInteger counter = topicSubscribers.get(destination);
        if (counter == null) {
            return; // 未注册的 topic 忽略
        }
        int count = counter.incrementAndGet();
        if (count == 1) {
            Runnable start = onFirstSubscribe.get(destination);
            if (start != null) {
                log.info("[SubscriptionTracker] 首个订阅者接入 {}，启动推送", destination);
                start.run();
            }
        }
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        String destination = StompHeaderAccessor.wrap(event.getMessage()).getDestination();
        if (destination == null) {
            return;
        }
        AtomicInteger counter = topicSubscribers.get(destination);
        if (counter == null) {
            return;
        }
        int count = counter.decrementAndGet();
        if (count <= 0) {
            counter.set(0); // 防止负数
            Runnable stop = onLastUnsubscribe.get(destination);
            if (stop != null) {
                log.info("[SubscriptionTracker] {} 无订阅者，停止推送", destination);
                stop.run();
            }
        }
    }
}
