import * as React from "react";

export function useVirtNotification(
  errors: Array<string>,
  setErrors: Function,
  serverId: string,
  refresh: (type: string) => void,
  listen: boolean = true
): [Object, Function] {
  const [actionsResults, setActionsResults] = React.useState({});
  const [webSocketErr, setWebSocketErr] = React.useState(false);
  const [pageUnloading, setPageUnloading] = React.useState(false);

  let websocket: any = {};

  // FIXME this is a hack:
  // in the onmessage callback we don't see the actionsResults changes.
  // To workaround this we use this normal variable that is updated timely.
  // However, the state is still needed since those actionsResults are displayed.
  // I am not React-ive enough to figure out what is wrong here, leaving it
  // as is since it works... maybe someone with ReactJS super powers will see
  // the problem later.
  let actions: any = {};

  const updateActions = (newActions: any): void => {
    actions = newActions;
    setActionsResults(newActions);
  };

  const onBeforeUnload = () => {
    setPageUnloading(true);
  };

  React.useEffect(() => {
    if (listen) {
      const { port } = window.location;
      const url = `wss://${window.location.hostname}${port ? `:${port}` : ""}/rhn/websocket/minion/virt-notifications`;
      const ws = new WebSocket(url);

      ws.onopen = () => {
        // Tell the websocket that we want to hear from all action results on this virtual host.
        ws.send(`{sid: ${serverId}}`);
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
          const newActions = JSON.parse(e.data);
          const refreshKind = newActions["refresh"];
          if (refreshKind != null) {
            refresh(refreshKind);
            return;
          }

          /*
           * The received items are split in two maps:
           *   - one with the actions that we don't already know about
           *   - one with existing actions.
           * Since for creation the key in the newActions map will not fit
           * the one we already got before, update the existing actions by matching
           * their IDs.
           */
          const aidMap = Object.keys(actions).reduce(
            (res, key) => Object.assign({}, res, { [actions[key].id]: key }),
            {}
          );
          const updatedActions = Object.keys(newActions)
            .filter((key) => Object.keys(aidMap).includes(String(newActions[key].id)))
            .reduce((res, key) => {
              const newAction = newActions[key];
              return Object.assign({}, res, { [aidMap[newAction.id]]: newAction });
            }, {});
          const addedActions = Object.keys(newActions)
            .filter((key) => !Object.keys(aidMap).includes(String(newActions[key].id)))
            .reduce((res, key) => Object.assign({}, res, { [key]: newActions[key] }), {});
          actions = Object.assign({}, actions, updatedActions, addedActions);
          setActionsResults(actions);
        }
      };

      window.addEventListener("beforeunload", onBeforeUnload);

      websocket = ws;
    }

    return () => {
      window.removeEventListener("beforeunload", onBeforeUnload);
      if (websocket !== null) {
        websocket.close();
      }
    };
  }, [serverId]);
  return [actionsResults, updateActions];
}
