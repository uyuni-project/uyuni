import * as React from "react";
import { localizedMoment } from "utils";

type Props = {
  time: string | moment.Moment;
};

const DateTime = (props: Props) => {
  const value = localizedMoment(props.time);
  return <span title={value.toISOString()}>{value.fromNow()}</span>;
};

export { DateTime };
