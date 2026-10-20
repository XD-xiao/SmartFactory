### 一、获取开关变化事件

1.方向：设备 => 服务端

2.订阅主题：

```
order/service/switch

order/service/lamps
```

4.payload结构：

```json
{
    "index": 1,
    "action": 1,				//0 关 1开
    "sn": "c0:bf:be:e7:3b:e3",
    "totalLamps": 4
}
```

 

 



### 二、控制产线

1.方向：服务端 => 设备

2.订阅主题：

```json
order/adapter/action 
```

4.payload结构：

```
{
    "sn": "c0:bf:be:e7:3b:e3",
    "index": 0,
    "action": 0			//0 关 1开
}
```







### 三、PLC状态回显

1.方向：服务端 <=> 设备

2.订阅主题：

```json
order/adapter/sync
```

3.payload结构：

```
发送 == 接收
```

4.作用触发order/service/sync回显



### 四、PLC状态显示

1.方向：设备 => 服务端

2.订阅主题：

```
order/service/sync
```

4.payload结构：

```json
{
    "sn": "c0:bf:be:e6:99:57",
    "device": [0,1,0,0],
    "totalLamps": 4,
    "lamps": [0,1,0,0],
    "version": "0.00"
}
```

 

