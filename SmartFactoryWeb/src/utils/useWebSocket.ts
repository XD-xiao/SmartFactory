import { Client, IMessage, type StompSubscription } from '@stomp/stompjs';
import { onUnmounted, ref } from 'vue';
import { websocketUrl } from '@/config/runtimeConfig';

export function useWebSocket() {
  const connected = ref(false);
  let client: Client | null = null;

  const createSockJS = async () => {
    const module = await import('sockjs-client/dist/sockjs');
    return module.default;
  };

  const connect = async (onConnect?: () => void) => {
    if (client?.active || client?.connected) {
      onConnect?.();
      return;
    }

    const SockJS = await createSockJS();

    client = new Client({
      webSocketFactory: () => new SockJS(websocketUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        connected.value = true;
        onConnect?.();
      },
      onDisconnect: () => {
        connected.value = false;
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers.message);
      },
      onWebSocketError: (event) => {
        connected.value = false;
        console.error('WebSocket error:', event);
      },
    });

    client.activate();
  };

  const subscribe = (destination: string, callback: (msg: IMessage) => void): StompSubscription | undefined => {
    if (!client?.connected) {
      console.warn('WebSocket not connected');
      return undefined;
    }

    return client.subscribe(destination, callback);
  };

  const send = (destination: string, body: Record<string, unknown>) => {
    if (!client?.connected) {
      return;
    }

    client.publish({ destination, body: JSON.stringify(body) });
  };

  const disconnect = async () => {
    if (!client) {
      connected.value = false;
      return;
    }

    try {
      await client.deactivate();
    } finally {
      connected.value = false;
      client = null;
    }
  };

  onUnmounted(() => {
    connected.value = false;
    client?.deactivate();
  });

  return { connected, connect, subscribe, send, disconnect };
}
