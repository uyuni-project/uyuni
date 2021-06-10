import * as React from "react";
import { useState } from "react";
import { DateTimePicker } from "./datetimepicker";

import * as moment from "moment";
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
        user time zone: {value.toUserTimeZoneString()} (
        {localizedMoment(value)
          .tz(value.toUserTimeZoneString())
          .format("Z")}
        )
      </p>
      <p>
        server time zone: {value.toServerTimeZoneString()} (
        {localizedMoment(value)
          .tz(value.toServerTimeZoneString())
          .format("Z")}
        )
      </p>
      <p>
        user time:
        {localizedMoment(value)
          .tz(value.toUserTimeZoneString())
          .toISOString(true)}
      </p>
      <p>
        server time:
        {localizedMoment(value)
          .tz(value.toServerTimeZoneString())
          .toISOString(true)}
      </p>
      <p>iso time: {value.toISOString()}</p>
      <p>browser time: {moment().toISOString(true)}</p>
      <DateTimePicker value={value} onChange={newValue => setValue(newValue)} />
    </div>
  );
};
