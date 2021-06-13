import * as React from "react";
import { localizedMoment } from "utils";

type Props = {
  time: string | moment.Moment;
};

const FromNow = (props: Props) => {
  const value = localizedMoment(props.time).utcOffset(localizedMoment.userTimeZone.utcOffset);
  return <span title={value.toUserString()}>{value.fromNow()}</span>;
};

export { FromNow };
