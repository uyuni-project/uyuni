/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import Network from "utils/network";

type NetworkMocks = Partial<Pick<typeof Network, "get" | "post" | "put" | "del">>;

const createMockScope = <T>(read: () => T, write: (value: T) => void) => {
  const entries: Array<{ value: T }> = [];
  let original: T;

  return (value: T) => {
    const current = read();
    if (entries.length === 0 || current !== entries[entries.length - 1].value) {
      entries.length = 0;
      original = current;
    }
    const entry = { value };
    entries.push(entry);
    write(value);

    return () => {
      const index = entries.indexOf(entry);
      if (index === -1) return;
      const isCurrent = index === entries.length - 1;
      entries.splice(index, 1);
      // Preserve a replacement installed outside this helper.
      if (isCurrent && read() === value) {
        write(entries.length > 0 ? entries[entries.length - 1].value : original);
      }
    };
  };
};

const scopes = {
  get: createMockScope(
    () => Network.get,
    (value) => {
      Network.get = value;
    }
  ),
  post: createMockScope(
    () => Network.post,
    (value) => {
      Network.post = value;
    }
  ),
  put: createMockScope(
    () => Network.put,
    (value) => {
      Network.put = value;
    }
  ),
  del: createMockScope(
    () => Network.del,
    (value) => {
      Network.del = value;
    }
  ),
};

/**
 * Replace Network methods globally in the current window until cleanup.
 * Overlapping scopes use the most recently installed replacement, regardless of the caller.
 * Use isolated story iframes when simultaneous examples need different responses.
 * Cleanup restores active replacements in either order.
 */
export const mockNetwork = ({ get, post, put, del }: NetworkMocks) => {
  const cleanups: Array<() => void> = [];
  if (get) cleanups.push(scopes.get(get));
  if (post) cleanups.push(scopes.post(post));
  if (put) cleanups.push(scopes.put(put));
  if (del) cleanups.push(scopes.del(del));
  return () => cleanups.forEach((cleanup) => cleanup());
};
