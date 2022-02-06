import * as React from "react";

import { IconTag } from "components/icontag";

type Props = {
  status: string;
  statusMessage: string;
};

const PaygStatus = (props: Props) => {
  let icon = "system-unknown";
  if (props.status === "E") {
    icon = "system-crit";
  } else if (props.status === "S") {
    icon = "system-ok";
  }

  return (
    <React.Fragment>
      <IconTag type={icon} />
      {props.statusMessage}
    </React.Fragment>
  );
};

export default PaygStatus;
