/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export const asyncIdleCallback = async <T extends () => any>(callback: T, timeout = 100) => {
  return new Promise<ReturnType<T>>((resolve) => {
    if (Object.prototype.hasOwnProperty.call(window, "requestIdleCallback")) {
      (window as any).requestIdleCallback(() => resolve(callback()), { timeout });
    } else {
      window.setTimeout(() => resolve(callback()), 0);
    }
  });
};
