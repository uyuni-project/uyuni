import { type Dispatch, type SetStateAction, useEffect, useRef } from "react";

export function useWebSocket(
  errors: string[],
  setErrors: Dispatch<SetStateAction<string[]>>,
  property: string,
  callback: (value: any) => void
) {
  const callbackRef = useRef(callback);
  const errorsRef = useRef(errors);

  callbackRef.current = callback;
  errorsRef.current = errors;

  useEffect(() => {
    let closedByCleanup = false;
    let pageUnloading = false;
    let webSocketErr = false;

    const { port } = window.location;
    const url = `wss://${window.location.hostname}${port ? `:${port}` : ""}/rhn/websocket/notifications`;
    const ws = new WebSocket(url);

    const onBeforeUnload = () => {
      pageUnloading = true;
    };

    ws.onopen = () => {
      // Tell the websocket what we want to hear about
      ws.send(`[${property}]`);
    };

    ws.onclose = () => {
      if (!closedByCleanup && !pageUnloading && !webSocketErr) {
        setErrors((currentErrors) =>
          (currentErrors || errorsRef.current || []).concat(
            t("Websocket connection closed. Refresh the page to try again.")
          )
        );
      }
    };

    ws.onerror = () => {
      if (!closedByCleanup) {
        webSocketErr = true;
        setErrors([t("Error connecting to server. Refresh the page to try again.")]);
      }
    };

    ws.onmessage = (e) => {
      if (!closedByCleanup && typeof e.data === "string") {
        const data = JSON.parse(e.data);
        callbackRef.current(data[property]);
      }
    };

    window.addEventListener("beforeunload", onBeforeUnload);

    return () => {
      closedByCleanup = true;
      window.removeEventListener("beforeunload", onBeforeUnload);
      // Detach handlers before closing to prevent events from updating state after unmount.
      ws.onopen = null;
      ws.onclose = null;
      ws.onerror = null;
      ws.onmessage = null;
      ws.close();
    };
  }, [property]);
}
