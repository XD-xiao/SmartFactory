package com.example.smartfactoryservice.Controller;

import com.example.smartfactoryservice.Service.MqttMessageSender;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    private final MqttMessageSender mqttMessageSender;

    @EventListener
    public void onPlcStatusSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if ("/topic/plc/status".equals(destination)) {
            mqttMessageSender.sendMsg("order/adapter/sync", "1");
        }
    }

    public void pushSensorActivity(Set<String> activeSensorCodes) {
        messagingTemplate.convertAndSend("/topic/sensor/activity", (Object) Map.of(
                "activeSensors", activeSensorCodes,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    public void pushPlcDeviceStatus(String sn, int[] devices) {
        List<Map<String, Object>> deviceList = new ArrayList<>();
        for (int i = 0; i < devices.length; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("seqNo", i);
            item.put("action", devices[i]);
            deviceList.add(item);
        }

        messagingTemplate.convertAndSend("/topic/plc/status", (Object) Map.of(
                "type", "plc_status",
                "sn", sn,
                "devices", deviceList,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
