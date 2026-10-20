<script setup lang="ts">
import { computed, ref } from 'vue';
import type { IMessage } from '@stomp/stompjs';
import { useWebSocket } from '@/utils/useWebSocket';

const subscriptionTopic = ref('/topic/public');
const sendDestination = ref('/app/chat.send');

const { connected, connect, subscribe, send, disconnect } = useWebSocket();

const messages = ref<string[]>([]);
const inputValue = ref('');
const statusMessage = ref('正在连接...');

const statusText = computed(() => (connected.value ? '已连接' : '未连接'));

const formatMessage = (body: string) => {
  try {
    const payload = JSON.parse(body) as Record<string, unknown>;
    return String(payload.content ?? payload.message ?? body);
  } catch {
    return body;
  }
};

const handleIncomingMessage = (message: IMessage) => {
  messages.value.unshift(formatMessage(message.body));
};

const sendMessage = () => {
  const content = inputValue.value.trim();
  if (!content || !connected.value) {
    return;
  }

  send(sendDestination.value, {
    content,
  });
  inputValue.value = '';
};


const connectWebSocket = async () => {
  try {
    await connect(() => {
      statusMessage.value = `已连接，订阅 ${subscriptionTopic}`;
      subscribe(subscriptionTopic.value, handleIncomingMessage);
    });
  } catch {
    statusMessage.value = '连接失败，请检查后端服务是否已启动';
  }
};

const disconnectWebSocket = async () => {
  await disconnect();
  statusMessage.value = '已断开连接';
};

</script>

<template>
  <section class="websocket-demo">
    <h1 class="title">WebSocket 测试页</h1>
    <p class="subtitle">{{ statusMessage }}</p>
    <div class="endpoint-row">
      <label class="endpoint-item">
        <span class="endpoint-label">订阅：</span>
        <el-input v-model="subscriptionTopic" class="endpoint-input" />
      </label>
      <label class="endpoint-item">
        <span class="endpoint-label">发送：</span>
        <el-input v-model="sendDestination" class="endpoint-input" />
      </label>
    </div>
    <div class="actions">
      <button class="button primary" :disabled="connected" @click="connectWebSocket">连接</button>
      <button class="button" :disabled="!connected" @click="disconnectWebSocket">断开连接</button>
    </div>

    <p class="status" :class="connected ? 'online' : 'offline'">{{ statusText }}</p>

    <section class="panel">
      <label class="label" for="message-input">发送消息</label>
      <div class="composer">
        <input
          id="message-input"
          v-model="inputValue"
          class="input"
          type="text"
          placeholder="请输入要发送的内容"
          @keyup.enter="sendMessage"
        />
        <button class="button primary" :disabled="!connected || !inputValue.trim()" @click="sendMessage">
          发送
        </button>
      </div>
    </section>

    <section class="panel">
      <h3 class="list-title">收到的消息</h3>
      <ul v-if="messages.length > 0" class="message-list">
        <li v-for="(msg, index) in messages" :key="`${index}-${msg}`" class="message-item">
          {{ msg }}
        </li>
      </ul>
      <div v-else class="empty-state">
        暂无消息。
      </div>
    </section>
  </section>
</template>

<style scoped>
.websocket-demo {
  max-width: 720px;
  margin: 24px auto;
  padding: 0 16px 24px;
  color: #1f2937;
}

.title {
  margin: 0 0 8px;
}

.subtitle,
.meta {
  margin: 0 0 8px;
  color: #6b7280;
}

.endpoint-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  align-items: center;
  margin: 0 0 12px;
}

.endpoint-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
}

.endpoint-label {
  white-space: nowrap;
}

.endpoint-input {
  width: 240px;
}

.status {
  display: inline-block;
  margin: 8px 0 16px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 14px;
}

.status.online {
  color: #166534;
  background: #dcfce7;
}

.status.offline {
  color: #991b1b;
  background: #fee2e2;
}

.actions {
  display: flex;
  gap: 12px;
  margin: 8px 0 16px;
}

.panel {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #ffffff;
}

.label {
  display: block;
  margin-bottom: 10px;
}

.composer {
  display: flex;
  gap: 12px;
}

.input {
  flex: 1;
  min-width: 0;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
}

.input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.button {
  height: 40px;
  padding: 0 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.button.primary {
  color: #fff;
  border-color: #2563eb;
  background: #2563eb;
}

.message-list {
  margin: 12px 0 0;
  padding: 0;
  list-style: none;
}

.message-item + .message-item {
  margin-top: 12px;
}

.message-item {
  padding: 12px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  word-break: break-word;
}

.empty-state {
  margin-top: 12px;
  padding: 16px;
  text-align: center;
  border: 1px dashed #d1d5db;
  border-radius: 10px;
  color: #6b7280;
}

@media (max-width: 768px) {
  .endpoint-row,
  .actions,
  .composer {
    flex-direction: column;
    align-items: stretch;
  }

  .endpoint-item {
    width: 100%;
  }

  .endpoint-input {
    width: 100%;
  }

  .button {
    width: 100%;
  }
}
</style>
