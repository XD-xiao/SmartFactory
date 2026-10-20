type RuntimeConfig = {
  backendOrigin?: string;
};

declare global {
  interface Window {
    __SMART_FACTORY_CONFIG__?: RuntimeConfig;
  }
}

const DEFAULT_BACKEND_ORIGIN = 'http://127.0.0.1:8080';

const normalizeOrigin = (value?: string) => {
  const origin = value?.trim() || DEFAULT_BACKEND_ORIGIN;
  return origin.replace(/\/+$/, '');
};

const backendOrigin = normalizeOrigin(window.__SMART_FACTORY_CONFIG__?.backendOrigin);

export const baseURL = backendOrigin;
export const websocketUrl = `${backendOrigin}/ws`;
