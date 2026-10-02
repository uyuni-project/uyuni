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
  });
});
