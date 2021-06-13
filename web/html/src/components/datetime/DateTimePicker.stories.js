import * as React from "react";
import { storiesOf } from "@storybook/react";

import { useState } from "react";
import { DateTimePicker } from "./DateTimePicker";

import moment from "moment";
import { localizedMoment } from "utils";

storiesOf("DateTimePicker", module).add("basic timezone support", () => {
  const [value, setValue] = useState(localizedMoment());
  return (
    <div>
      <p>
        user time zone: {localizedMoment.userTimeZone.displayValue} (
        {localizedMoment(value)
          .utcOffset(localizedMoment.userTimeZone.utcOffset)
          .format("Z")}
        )
      </p>
      <p>
        server time zone: {localizedMoment.serverTimeZone.displayValue} (
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
});
