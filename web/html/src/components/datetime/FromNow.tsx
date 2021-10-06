import * as React from "react";
import { localizedMoment } from "utils";

type Props = {
  value?: string | moment.Moment;
  children?: string;
};

export const FromNow = (props: Props) => {
  const rawValue = props.value ?? props.children;
  if (!rawValue) {
    return null;
  }
  const value = localizedMoment(rawValue).tz(localizedMoment.userTimeZone);
  return <span title={value.toUserString()}>{value.fromNow()}</span>;
};

export const fromNowStringfied = (value) => {
  return value ? localizedMoment(value).tz(localizedMoment.userTimeZone).fromNow() : null;
}
