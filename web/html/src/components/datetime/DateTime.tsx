import * as React from "react";

import { localizedMoment } from "utils";

type Props = {
  value?: string | moment.Moment;
  children?: string;
};

export const DateTime = (props: Props) => {
  const rawValue = props.value ?? props.children;
  if (!rawValue) {
    return null;
  }
  const value = localizedMoment(rawValue).tz(localizedMoment.userTimeZone);
  return <React.Fragment>{value.toUserString()}</React.Fragment>;
};
