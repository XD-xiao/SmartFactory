package com.example.smartfactoryservice.Service.ProductModule.impl;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

abstract class ProductServiceSupport {

    protected static final int BAD_REQUEST_CODE = 400;
    protected static final int NOT_FOUND_CODE = 404;

    protected void requireBody(Map<String, Object> body, String message) {
        if (body == null || body.isEmpty()) {
            throw new BusinessException(BAD_REQUEST_CODE, message);
        }
    }

    protected String getString(Map<String, Object> body, String... keys) {
        Object value = getValue(body, keys);
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    protected String requireString(Map<String, Object> body, String fieldName, String... keys) {
        String value = getString(body, keys);
        if (value == null) {
            throw new BusinessException(BAD_REQUEST_CODE, fieldName + "不能为空");
        }
        return value;
    }

    protected Integer getInteger(Map<String, Object> body, String... keys) {
        Object value = getValue(body, keys);
        return toInteger(value);
    }

    protected Integer requireInteger(Map<String, Object> body, String fieldName, String... keys) {
        Integer value = getInteger(body, keys);
        if (value == null) {
            throw new BusinessException(BAD_REQUEST_CODE, fieldName + "不能为空");
        }
        return value;
    }

    protected Long getLong(Map<String, Object> body, String... keys) {
        Object value = getValue(body, keys);
        return toLong(value);
    }

    protected Long requireLong(Map<String, Object> body, String fieldName, String... keys) {
        Long value = getLong(body, keys);
        if (value == null) {
            throw new BusinessException(BAD_REQUEST_CODE, fieldName + "不能为空");
        }
        return value;
    }

    protected BigDecimal getBigDecimal(Map<String, Object> body, String... keys) {
        Object value = getValue(body, keys);
        return toBigDecimal(value);
    }

    protected LocalDateTime getLocalDateTime(Map<String, Object> body, String... keys) {
        Object value = getValue(body, keys);
        return toLocalDateTime(value);
    }

    protected Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            throw new BusinessException(BAD_REQUEST_CODE, "参数不是有效整数: " + text);
        }
    }

    protected Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer number) {
            return number;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new BusinessException(BAD_REQUEST_CODE, "参数不是有效整数: " + text);
        }
    }

    protected BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            throw new BusinessException(BAD_REQUEST_CODE, "参数不是有效数字: " + text);
        }
    }

    protected LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(text).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(BAD_REQUEST_CODE, "时间格式不正确: " + text);
        }
    }

    protected PageBean buildPageBean(List<?> fullList, int pageNum, int pageSize) {
        List<?> safeList = fullList == null ? Collections.emptyList() : fullList;
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = (safePageNum - 1) * safePageSize;
        if (fromIndex >= safeList.size()) {
            return new PageBean((long) safeList.size(), Collections.emptyList());
        }
        int toIndex = Math.min(fromIndex + safePageSize, safeList.size());
        return new PageBean((long) safeList.size(), safeList.subList(fromIndex, toIndex));
    }

    private Object getValue(Map<String, Object> body, String... keys) {
        if (body == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key != null && body.containsKey(key)) {
                return body.get(key);
            }
        }
        return null;
    }
}
