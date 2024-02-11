import * as React from "react";

import { Button } from "components/buttons";

import Network from "utils/network";

import { useWebSocket } from "../../shared/websocket/useWebSocket";

type Props = {
  count?: number;
};

export function SsmCounter(props: Props) {
  const [count, setCount] = React.useState(props.count);
  const [errors, setErrors] = React.useState([]);
  useWebSocket(errors, setErrors, "ssm-count", (value: number) => {
    setCount(value);
  });
  const ssm_clear = () => {
    Network.post("/rhn/manager/api/sets/system_list/clear");
  };

  return (
    <>
      <a href="/rhn/ssm/index.do" id="manage-ssm" title={t("Manage selected system set")}>
        <div id="ssm-counter" />
        <div id="header_selcount">
          <span id="spacewalk-set-system_list-counter" className="badge">
            {count}
          </span>
          {count === 1 ? t("system selected") : t("systems selected")}
        </div>
      </a>
      <Button id="clear-ssm" title={t("Clear selected system set")} handler={ssm_clear} icon="fa-eraser" />
    </>
  );
}
SsmCounter.defaultProps = {
  count: 0,
};
