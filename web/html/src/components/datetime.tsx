import * as React from "react";
import { localizedMoment } from "utils";

type Props = {
  time: string | moment.Moment;
};

// TODO: Rename FromNow or something similar
const DateTime = (props: Props) => {
  const value = localizedMoment(props.time).tz(localizedMoment.userTimeZone);
  return <span title={value.toUserISOString()}>{value.fromNow()}</span>;
};

export { DateTime };
