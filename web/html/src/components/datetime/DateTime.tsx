import { localizedMoment } from "utils";

import { getTimeProps } from "./timeProps";

type Props = {
  value?: string | moment.Moment;
  children?: string;
};

export const DateTime = (props: Props) => {
  const input = props.value ?? props.children;
  if (!input) {
    return null;
  }

  const value = localizedMoment(input).tz(localizedMoment.userTimeZone);
  const { title, dateTime } = getTimeProps(value);
  return (
    <time title={title} dateTime={dateTime}>
      {value.toUserString()}
    </time>
  );
};

export const HumanDateTime = (props: Props) => {
  const input = props.value ?? props.children;
  if (!input) {
    return null;
  }

  const value = localizedMoment(input).tz(localizedMoment.userTimeZone);
  const { title, dateTime } = getTimeProps(value);
  return (
    <time title={title} dateTime={dateTime}>
      {value.calendar()}
    </time>
  );
};
