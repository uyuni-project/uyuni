import * as React from "react";
import { useState } from "react";
import { DateTimePicker } from "./DatetimePicker";

import moment from "moment";
import { localizedMoment } from "utils";

export default {
  component: DateTimePicker,
  title: "datetimepicker",
};

export const Example = () => {
  const [value, setValue] = useState(localizedMoment());
  return (
    <div>
      <p>
        user time zone: {localizedMoment.userTimeZone} (
        {localizedMoment(value)
          .utcOffset(localizedMoment.userTimeZone.utcOffset)
          .format("Z")}
        )
      </p>
      <p>
        server time zone: {localizedMoment.serverTimeZone} (
        {localizedMoment(value)
          .utcOffset(localizedMoment.serverTimeZone.utcOffset)
          .format("Z")}
        )
      </p>
      <p>
        user time:
        {localizedMoment(value)
          .utcOffset(localizedMoment.userTimeZone.utcOffset)
          .toISOString(true)}
      </p>
      <p>
        server time:
        {localizedMoment(value)
          .utcOffset(localizedMoment.serverTimeZone.utcOffset)
          .toISOString(true)}
      </p>
      <p>iso time: {value.toISOString()}</p>
      <p>browser time: {moment().toISOString(true)}</p>
      <DateTimePicker value={value} onChange={newValue => setValue(newValue)} />
    </div>
  );
};
