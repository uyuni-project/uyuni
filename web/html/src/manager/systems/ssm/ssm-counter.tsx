import * as React from "react";

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
  return (
    <div id="header_selcount">
      <span id="spacewalk-set-system_list-counter" className="badge">
        {count}
      </span>
      {count === 1 ? t("system selected") : t("systems selected")}
    </div>
  );
}
SsmCounter.defaultProps = {
  count: 0,
};
