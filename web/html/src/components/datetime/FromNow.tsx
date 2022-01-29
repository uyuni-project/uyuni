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

// It returns a simple string instead of a component in the `fromNow` localized format
// Use case: at the moment the `t()` function (to translate strings) does not support complex components
export const fromNow = (value) => {
  return value ? localizedMoment(value).tz(localizedMoment.userTimeZone).fromNow() : null;
};
