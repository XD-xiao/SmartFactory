### Agent检测结果表

| 属性                 | 类型         | 说明                                                         |
| -------------------- | ------------ | ------------------------------------------------------------ |
| id                   | bigint       | 主键，自增                                                   |
| status               | varchar(32)  | 状态：SUCCESS-AI调用成功 / FAILED-AI调用失败 / UNREAD-未读 / READ-已读 |
| is_notified          | tinyint      | 是否通知：0-未通知 / 1-已通知                                 |
| data_start_time      | datetime     | 分析的传感器数据起始时间                                      |
| data_end_time        | datetime     | 分析的传感器数据截止时间                                      |
| result_report        | text         | 检测结果报告（同时包含预测维护和异常检测两方面的分析）         |
| analysis_start_time  | datetime     | 分析开始时间                                                  |
| analysis_end_time    | datetime     | 分析结束时间                                                  |
| create_time          | datetime     | 记录创建时间                                                  |


---

## 说明

- 每次分析同时对传感器数据进行 **预测性维护** 和 **异常检测**，合并为一份报告
- 报告输出规则：
  - 两者均正常 → 报告以"正常"为主，简要覆盖两方面结论
  - 仅一方异常 → 异常方重点详述，正常方简要说明
  - 两者均异常 → 两部分均重点详述
- 存在任意异常时，`is_notified` 可由业务决定置为 1

---

## 建表 SQL

```sql
CREATE TABLE `agent_detection_result` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `status`              VARCHAR(32)  DEFAULT 'UNREAD'        COMMENT '状态: SUCCESS-AI调用成功; FAILED-AI调用失败; UNREAD-未读; READ-已读',
  `is_notified`         TINYINT      DEFAULT 0              COMMENT '是否通知: 0-未通知; 1-已通知',
  `data_start_time`     DATETIME     DEFAULT NULL            COMMENT '分析的传感器数据起始时间',
  `data_end_time`       DATETIME     DEFAULT NULL            COMMENT '分析的传感器数据截止时间',
  `result_report`       TEXT         DEFAULT NULL            COMMENT '检测结果报告（预测维护 + 异常检测）',
  `analysis_start_time` DATETIME     DEFAULT NULL            COMMENT '分析开始时间',
  `analysis_end_time`   DATETIME     DEFAULT NULL            COMMENT '分析结束时间',
  `create_time`         DATETIME     DEFAULT NULL            COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_data_time` (`data_start_time`, `data_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent检测结果表';
```
