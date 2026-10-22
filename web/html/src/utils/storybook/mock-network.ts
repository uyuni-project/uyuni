import Network from "utils/network";

type NetworkMocks = Partial<Pick<typeof Network, "get" | "post" | "put" | "del">>;

/**
 * Installs story-scoped Network method replacements and restores them after the story unmounts.
 * A method is only restored when it still points to the replacement installed by this call.
 */
export const mockNetwork = ({ get, post, put, del }: NetworkMocks) => {
  const cleanups: Array<() => void> = [];

  if (get) {
    const original = Network.get;
    Network.get = get;
    cleanups.push(() => {
      if (Network.get === get) Network.get = original;
    });
  }

  if (post) {
    const original = Network.post;
    Network.post = post;
    cleanups.push(() => {
      if (Network.post === post) Network.post = original;
    });
  }

  if (put) {
    const original = Network.put;
    Network.put = put;
    cleanups.push(() => {
      if (Network.put === put) Network.put = original;
    });
  }

  if (del) {
    const original = Network.del;
    Network.del = del;
    cleanups.push(() => {
      if (Network.del === del) Network.del = original;
    });
  }

  return () => cleanups.reverse().forEach((cleanup) => cleanup());
};
