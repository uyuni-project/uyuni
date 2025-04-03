import { useState } from "react";

import moment from "moment";

import { localizedMoment } from "utils";

import { DateTimePicker } from "./DateTimePicker";
import { DEPRECATED_DateTimePicker } from "./DEPRECATED_DateTimePicker";

export default () => {
  const [value, setValue] = useState(localizedMoment());
  const legacyId = "legacy";

  const [newPickerValue, setNewPickerValue] = useState(localizedMoment());
  const newPickerLegacyId = "new-legacy";

  return (
    <>
      <p>
        This demo is mostly useful for debugging updates to the picker and ensuring all the values still line up
        correctly.
      </p>
      <div>
        <div>
          <p>
            <b>old date time picker</b>
          </p>
          <p>
            user time zone: {localizedMoment.userTimeZone} (
            {localizedMoment(value).tz(localizedMoment.userTimeZone).format("Z")})
          </p>
          <p>
            server time zone: {localizedMoment.serverTimeZone} (
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
          <p>legacy id: "{legacyId}"</p>
          <DEPRECATED_DateTimePicker value={value} onChange={(newValue) => setValue(newValue)} legacyId={legacyId} />
        </div>

        <div>
          <p>
            <b>new date time picker</b>
          </p>
          <p>
            user time zone: {localizedMoment.userTimeZone} (
            {localizedMoment(newPickerValue).tz(localizedMoment.userTimeZone).format("Z")})
          </p>
          <p>
            server time zone: {localizedMoment.serverTimeZone} (
            {localizedMoment(newPickerValue).tz(localizedMoment.serverTimeZone).format("Z")})
          </p>
          <p>
            user time:
            {localizedMoment(newPickerValue).tz(localizedMoment.userTimeZone).toISOString(true)}
          </p>
          <p>
            server time:
            {localizedMoment(newPickerValue).tz(localizedMoment.serverTimeZone).toISOString(true)}
          </p>
          <p>iso time: {newPickerValue.toISOString()}</p>
          {/* eslint-disable-next-line local-rules/no-raw-date */}
          <p>browser time: {moment().toISOString(true)}</p>
          <p>legacy id: "{newPickerLegacyId}"</p>
          <DateTimePicker
            value={newPickerValue}
            onChange={(newValue) => setNewPickerValue(newValue)}
            legacyId={newPickerLegacyId}
          />
        </div>
      </div>
    </>
  );
};
