### 一、实时获取传感器数据

1.方向：设备 => 服务端

2.Topic格式：`eduBox/server/data/{sn}`

3.订阅写法：

```
eduBox/server/data/#
```

4.payload结构：

```json
{
  "type": {
    "com": 10,
    "sensor": 25
  },
  "value": {
    "temp": 22,
    "humi": 52
  },
  "sn": "ea010e5350654d4d43100d0c4c8d1a00"
}
```









### 二、切换实验类型

1.方向：设备 => 服务端

2.Topic格式：`eduBox/server/examId/{sn}`

3.订阅写法：

```
eduBox/server/examId/#
```

4.payload结构：

```json
{
  "id": 1,
  "sensorList": [
    25,
    26
  ]
}
```





### 三、控制传感器

1.方向：服务端 => 设备

2.Topic格式：`eduBox/device/set/{sn}`

3.订阅写法：

```
eduBox/server/set/{sn}
```

4.payload结构：

```json
{
  "source": 1,
  "type": { "sensor": 26 },
  "value": {}
}
```

5.value内容示例：

```json
{ "source": 1, "type": { "sensor": 26 }, "value": { "fan": 0 } }
{ "source": 1, "type": { "sensor": 44 }, "value": { "led": 0 } }
{ "source": 1, "type": { "sensor": 22 }, "value": { "alarm": 0 } }
{ "source": 1, "type": { "sensor": 35 }, "value": { "water": 0 } }
```



### 四、设置传感器阈值

1.方向：服务端 => 设备

2.Topic格式：`eduBox/device/examConfig`

3.订阅写法：

```
eduBox/device/examConfig
```

4.payload结构：

```json
{
  "source": 1,
  "sensor": 26,				//当前下发的实验类型
  "config": {}
}
```

5.config示例：

```json
"config": {"max_threshold": 1000,"mid_threshold": 500,"min_threshold": 100}
```



### ~~五、设置照明策略~~

1.方向：服务端 => 设备

2.Topic格式：`eduBox/device/illuminationStrategy/{sn}`

3.订阅写法：

```
eduBox/device/illuminationStrategy/{sn}
```

4.payload结构：

```json
{
  "source": 1,
  "type": { "sensor": 27 },
  "value": {
    "threshold": 100,
    "step": 1
  }
}
```





















































































































