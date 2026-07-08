import { type Dispatch, type SetStateAction, useEffect, useRef } from "react";

export function useWebSocket(
  errors: string[],
  setErrors: Dispatch<SetStateAction<string[]>>,
  property: string,
  callback: (value: any) => void
) {
  const callbackRef = useRef(callback);
  const errorsRef = useRef(errors);
  const pageUnloadingRef = useRef(false);
  const webSocketErrRef = useRef(false);

  callbackRef.current = callback;
  errorsRef.current = errors;

  useEffect(() => {
    let closedByCleanup = false;
    const { port } = window.location;
    const url = `wss://${window.location.hostname}${port ? `:${port}` : ""}/rhn/websocket/notifications`;
    const ws = new WebSocket(url);

    const onBeforeUnload = () => {
      pageUnloadingRef.current = true;
    };

    ws.onopen = () => {
      // Tell the websocket what we want to hear about
      ws.send(`[${property}]`);
    };

    ws.onclose = () => {
      if (!closedByCleanup && !pageUnloadingRef.current && !webSocketErrRef.current) {
        setErrors((currentErrors) =>
          (currentErrors || errorsRef.current || []).concat(
            t("Websocket connection closed. Refresh the page to try again.")
          )
        );
      }
    };

    ws.onerror = () => {
      if (!closedByCleanup) {
        webSocketErrRef.current = true;
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
      ws.onopen = null;
      ws.onclose = null;
      ws.onerror = null;
      ws.onmessage = null;
      ws.close();
    };
  }, [property]);
}
