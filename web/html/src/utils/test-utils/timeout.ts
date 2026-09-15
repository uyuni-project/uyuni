/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export const timeout = (ms: number) => {
  return new Promise<void>((resolve) => {
    window.setTimeout(() => {
      resolve();
    }, ms);
  });
};
