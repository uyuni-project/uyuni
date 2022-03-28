export const asyncIdleCallback = async <T extends () => any>(callback: T, timeout: number = 100) => {
  return new Promise<ReturnType<T>>((resolve) => {
    if (Object.prototype.hasOwnProperty.call(window, "requestIdleCallback")) {
      (window as any).requestIdleCallback(() => resolve(callback()), { timeout });
    } else {
      window.setTimeout(() => resolve(callback()), 0);
    }
  });
};
