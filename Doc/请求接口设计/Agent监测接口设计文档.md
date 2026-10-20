---
title: Agent智能检测
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# Agent智能检测

Base URLs:

# Authentication

# Agent智能检测/检测结果接口

## GET 获取检测结果列表

GET /api/agent/getDetectionList

分页查询 Agent 智能检测结果列表。Agent 定时任务会每 30 秒自动从传感器记录中采集数据，调用 AI 大模型进行预测性维护和异常检测分析，并将分析报告保存到数据库。

### 请求参数

|名称|位置|类型|必选|中文名|说明|
|---|---|---|---|---|---|
|status|query|string| 否 |检测状态|SUCCESS: 成功 / FAILED: 失败 / UNREAD: 未读 / READ: 已读|
|pageNum|query|integer| 否 |页码|默认 1|
|pageSize|query|integer| 否 |每页条数|默认 10|
|Authorization|header|string| 否 ||none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "total": 0,
    "list": [
      {
        "id": 0,
        "status": "string",
        "isNotified": 0,
        "dataStartTime": "string",
        "dataEndTime": "string",
        "resultReport": "string",
        "analysisStartTime": "string",
        "analysisEndTime": "string",
        "createTime": "string"
      }
    ]
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||none|
|» message|string|true|none||none|
|» data|object|true|none||none|
|»» total|integer|true|none||总记录数|
|»» list|[object]|true|none||数据列表|
|»»» id|integer|true|none||检测记录ID|
|»»» status|string|true|none||检测状态(SUCCESS/FAILED/UNREAD/READ)|
|»»» isNotified|integer|true|none||是否通知(0:未通知; 1:已通知)|
|»»» dataStartTime|string|true|none||分析的传感器数据起始时间|
|»»» dataEndTime|string|true|none||分析的传感器数据截止时间|
|»»» resultReport|string|true|none||检测结果报告(AI生成)|
|»»» analysisStartTime|string|true|none||分析开始时间|
|»»» analysisEndTime|string|true|none||分析结束时间|
|»»» createTime|string|true|none||记录创建时间|

# 说明

## Agent 检测流程

Agent 智能检测由后端定时任务 `AgentDetectionScheduler` 驱动，每 30 秒自动执行一次，具备幂等性保护（10 秒窗口内不会重复执行）。

检测流程如下：

1. **数据采集**：从 `edubox_sensor_record` 表中获取最新的传感器记录数据
2. **AI 分析**：调用 AI 大模型（通过 `AIAgentService`），结合传感器配置信息进行预测性维护分析和异常检测
3. **报告生成**：AI 返回结构化分析报告，系统自动检测报告中是否包含 `【异常】` 标记
4. **结果存储**：将检测结果（状态、报告内容、时间区间等）保存到 `agent_detection_result` 表

## 状态说明

| 状态值 | 说明 |
|---|---|
| SUCCESS | 检测完成，包含正常或异常检测结果 |
| FAILED | 检测失败（如 AI 调用异常、数据不足等） |
| UNREAD | 初始状态，检测结果尚未被前端查看 |
| READ | 检测结果已被前端查看 |

# 前端调用说明

前端 `DeviceApi.js` 中对应的调用函数：

| 接口 | 前端函数 | 请求方式 |
|---|---|---|
| GET /api/agent/getDetectionList | getAgentDetectionListService(status, pageNum, pageSize) | GET(Query) |

前端页面 `MonitoringRecordPage.vue` 提供检测记录的列表展示，支持按状态筛选、分页浏览，点击"查看报告"可弹出 `DetectionReportDialog.vue` 对话框查看完整 AI 分析报告。

# 数据模型
