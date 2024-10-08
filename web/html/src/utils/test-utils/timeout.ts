export const timeout = (ms = 0) => new Promise<void>((resolve) => window.setTimeout(() => resolve(), ms));
