/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import Network from "utils/network";

import { mockNetwork } from "./mock-network";

describe("mockNetwork", () => {
  const originalGet = Network.get;
  const originalPost = Network.post;
  const originalPut = Network.put;
  const originalDelete = Network.del;

  afterEach(() => {
    Network.get = originalGet;
    Network.post = originalPost;
    Network.put = originalPut;
    Network.del = originalDelete;
  });

  test("installs and restores the supplied methods", () => {
    const get = jest.fn() as unknown as typeof Network.get;
    const post = jest.fn() as unknown as typeof Network.post;
    const cleanup = mockNetwork({ get, post });

    expect(Network.get).toBe(get);
    expect(Network.post).toBe(post);

    cleanup();

    expect(Network.get).toBe(originalGet);
    expect(Network.post).toBe(originalPost);
  });

  test("does not overwrite a newer replacement during cleanup", () => {
    const get = jest.fn() as unknown as typeof Network.get;
    const newerGet = jest.fn() as unknown as typeof Network.get;
    const cleanup = mockNetwork({ get });
    Network.get = newerGet;

    cleanup();

    expect(Network.get).toBe(newerGet);
    // Cleanup must be idempotent even when an external replacement won.
    cleanup();
  });

  test.each([false, true])("preserves external replacements across new scopes (oldest first: %s)", (oldestFirst) => {
    const first = jest.fn() as unknown as typeof Network.get;
    const external = jest.fn() as unknown as typeof Network.get;
    const second = jest.fn() as unknown as typeof Network.get;
    const cleanupFirst = mockNetwork({ get: first });
    Network.get = external;
    const cleanupSecond = mockNetwork({ get: second });
    expect(Network.get).toBe(second);

    if (oldestFirst) {
      cleanupFirst();
      expect(Network.get).toBe(second);
      cleanupSecond();
    } else {
      cleanupSecond();
      expect(Network.get).toBe(external);
      cleanupFirst();
    }
    expect(Network.get).toBe(external);
    cleanupFirst();
    cleanupSecond();
    expect(Network.get).toBe(external);
  });
  test.each([false, true])("restores overlapping scopes (oldest first: %s)", (oldestFirst) => {
    const first = jest.fn() as unknown as typeof Network.get;
    const second = jest.fn() as unknown as typeof Network.get;
    const cleanupFirst = mockNetwork({ get: first });
    const cleanupSecond = mockNetwork({ get: second });

    if (oldestFirst) {
      cleanupFirst();
      expect(Network.get).toBe(second);
      cleanupSecond();
    } else {
      cleanupSecond();
      expect(Network.get).toBe(first);
      cleanupFirst();
    }
    expect(Network.get).toBe(originalGet);
    cleanupFirst();
    cleanupSecond();
    expect(Network.get).toBe(originalGet);
  });

  test("tracks two scopes using the same replacement independently", () => {
    const replacement = jest.fn() as unknown as typeof Network.get;
    const first = mockNetwork({ get: replacement });
    const second = mockNetwork({ get: replacement });
    first();
    expect(Network.get).toBe(replacement);
    second();
    expect(Network.get).toBe(originalGet);
  });
});
