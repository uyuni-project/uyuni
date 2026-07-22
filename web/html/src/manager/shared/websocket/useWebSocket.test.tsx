import { type Dispatch, type SetStateAction } from "react";

import { render } from "utils/test-utils";

import { useWebSocket } from "./useWebSocket";

class MockWebSocket {
  static instances: MockWebSocket[] = [];

  readonly url: string;
  onopen: ((event: Event) => void) | null = null;
  onclose: ((event: CloseEvent) => void) | null = null;
  onerror: ((event: Event) => void) | null = null;
  onmessage: ((event: MessageEvent) => void) | null = null;
  send = jest.fn();
  close = jest.fn(() => this.onclose?.(new CloseEvent("close")));

  constructor(url: string | URL) {
    this.url = url.toString();
    MockWebSocket.instances.push(this);
  }
}

type TestComponentProps = {
  callback: (value: number) => void;
  setErrors: Dispatch<SetStateAction<string[]>>;
};

function TestComponent({ callback, setErrors }: TestComponentProps) {
  useWebSocket([], setErrors, "ssm-count", callback);
  return null;
}

describe("useWebSocket", () => {
  const originalWebSocket = window.WebSocket;

  beforeEach(() => {
    MockWebSocket.instances = [];
    window.WebSocket = MockWebSocket as unknown as typeof WebSocket;
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  afterAll(() => {
    window.WebSocket = originalWebSocket;
  });

  function getSocket() {
    expect(MockWebSocket.instances).toHaveLength(1);
    return MockWebSocket.instances[0];
  }

  test("subscribes to the requested property when connected", () => {
    const setErrors = jest.fn();
    render(<TestComponent callback={jest.fn()} setErrors={setErrors} />);

    const socket = getSocket();
    socket.onopen?.(new Event("open"));

    expect(socket.send).toHaveBeenCalledWith("[ssm-count]");
  });

  test("closes and detaches the socket without reporting an error on unmount", () => {
    const setErrors = jest.fn();
    const removeEventListener = jest.spyOn(window, "removeEventListener");
    const { unmount } = render(<TestComponent callback={jest.fn()} setErrors={setErrors} />);
    const socket = getSocket();

    unmount();

    expect(removeEventListener).toHaveBeenCalledWith("beforeunload", expect.any(Function));
    expect(socket.close).toHaveBeenCalledTimes(1);
    expect(socket.onopen).toBeNull();
    expect(socket.onclose).toBeNull();
    expect(socket.onerror).toBeNull();
    expect(socket.onmessage).toBeNull();
    expect(setErrors).not.toHaveBeenCalled();
  });

  test("reports an unexpected connection close", () => {
    const setErrors = jest.fn();
    render(<TestComponent callback={jest.fn()} setErrors={setErrors} />);

    getSocket().onclose?.(new CloseEvent("close"));

    expect(setErrors).toHaveBeenCalledTimes(1);
    const updateErrors = setErrors.mock.calls[0][0] as (currentErrors: string[]) => string[];
    const updatedErrors = updateErrors(["Existing error"]);
    expect(updatedErrors[0]).toBe("Existing error");
    expect(updatedErrors[1]).toContain("Websocket connection closed");
  });

  test("uses the latest callback without reconnecting", () => {
    const firstCallback = jest.fn();
    const latestCallback = jest.fn();
    const setErrors = jest.fn();
    const { rerender } = render(<TestComponent callback={firstCallback} setErrors={setErrors} />);
    const socket = getSocket();

    rerender(<TestComponent callback={latestCallback} setErrors={setErrors} />);
    socket.onmessage?.(
      new MessageEvent("message", {
        data: JSON.stringify({ "ssm-count": 7 }),
      })
    );

    expect(MockWebSocket.instances).toHaveLength(1);
    expect(firstCallback).not.toHaveBeenCalled();
    expect(latestCallback).toHaveBeenCalledWith(7);
  });
});
