import { MomentInput } from "moment";

import { localizedMoment } from "utils";

import { getTimeProps } from "./timeProps";

type Props = {
  value?: string | moment.Moment | Date;
  children?: string;
};

export const FromNow = (props: Props) => {
  const input = props.value ?? props.children;
  if (!input) {
    return null;
  }

  const value = localizedMoment(input).tz(localizedMoment.userTimeZone);
  const { title, dateTime } = getTimeProps(value);
  return (
    <time title={title} dateTime={dateTime}>
      {value.fromNow()}
    </time>
  );
};

// It returns a simple string instead of a component in the `fromNow` localized format
// Use case: at the moment the `t()` function (to translate strings) does not support complex components
// TODO: Make this obsolete once https://github.com/SUSE/spacewalk/issues/20449 is implemented
export const fromNow = (value?: MomentInput) => {
  return value ? localizedMoment(value).tz(localizedMoment.userTimeZone).fromNow() : null;
};
