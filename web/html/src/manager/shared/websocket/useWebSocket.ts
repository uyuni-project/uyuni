import * as React from "react";

export function useWebSocket(
  errors: Array<string>,
  setErrors: Function,
  property: string,
  callback: (value: any) => void
) {
  const [webSocketErr, setWebSocketErr] = React.useState(false);
  const [pageUnloading, setPageUnloading] = React.useState(false);

  const onBeforeUnload = () => {
    setPageUnloading(true);
  };

  React.useEffect(() => {
    const { port } = window.location;
    const url = `wss://${window.location.hostname}${port ? `:${port}` : ""}/rhn/websocket/notifications`;
    const ws = new WebSocket(url);

    ws.onopen = () => {
      // Tell the websocket what we want to hear about
      ws.send(`[${property}]`);
    };

    ws.onclose = () => {
      setErrors(
        (errors || []).concat(
          !pageUnloading && !webSocketErr ? t("Websocket connection closed. Refresh the page to try again.") : []
        )
      );
    };

    ws.onerror = () => {
      setErrors([t("Error connecting to server. Refresh the page to try again.")]);
      setWebSocketErr(true);
    };

    ws.onmessage = (e) => {
      if (typeof e.data === "string") {
        const data = JSON.parse(e.data);
        callback(data[property]);
      }
    };

    window.addEventListener("beforeunload", onBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", onBeforeUnload);
      ws.close();
    };
  }, [property]);
}
