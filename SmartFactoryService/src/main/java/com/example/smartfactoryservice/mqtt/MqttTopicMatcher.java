package com.example.smartfactoryservice.mqtt;

public final class MqttTopicMatcher {

    private MqttTopicMatcher() {
    }

    public static boolean matches(String topicPattern, String topic) {
        String[] patternParts = topicPattern.split("/", -1);
        String[] topicParts = topic.split("/", -1);

        int patternIndex = 0;
        int topicIndex = 0;
        while (patternIndex < patternParts.length && topicIndex < topicParts.length) {
            String patternPart = patternParts[patternIndex];
            if ("#".equals(patternPart)) {
                return patternIndex == patternParts.length - 1;
            }
            if (!"+".equals(patternPart) && !patternPart.equals(topicParts[topicIndex])) {
                return false;
            }
            patternIndex++;
            topicIndex++;
        }

        if (patternIndex == patternParts.length && topicIndex == topicParts.length) {
            return true;
        }

        return patternIndex == patternParts.length - 1 && "#".equals(patternParts[patternIndex]);
    }
}
