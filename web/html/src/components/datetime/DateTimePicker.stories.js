import * as React from "react";
import { useState } from "react";

import { storiesOf } from "@storybook/react";
import moment from "moment";

import { localizedMoment } from "utils";

import { DateTimePicker } from "./DateTimePicker";

storiesOf("DateTimePicker", module).add("basic timezone support", () => {
  const [value, setValue] = useState(localizedMoment());
  return (
    <div>
      <p>
        user time zone: {localizedMoment.userTimeZone.displayValue} (
        {localizedMoment(value).tz(localizedMoment.userTimeZone).format("Z")})
      </p>
      <p>
        server time zone: {localizedMoment.serverTimeZone.displayValue} (
        {localizedMoment(value).tz(localizedMoment.serverTimeZone).format("Z")})
      </p>
      <p>
        user time:
        {localizedMoment(value).tz(localizedMoment.userTimeZone).toISOString(true)}
      </p>
      <p>
        server time:
        {localizedMoment(value).tz(localizedMoment.serverTimeZone).toISOString(true)}
      </p>
      <p>iso time: {value.toISOString()}</p>
      {/* eslint-disable-next-line local-rules/no-raw-date */}
      <p>browser time: {moment().toISOString(true)}</p>
      <DateTimePicker value={value} onChange={(newValue) => setValue(newValue)} />
    </div>
  );
});
