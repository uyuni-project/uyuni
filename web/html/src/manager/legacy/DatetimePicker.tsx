/**
 * This binding is a replacement for the old spacewalk-datetimepicker.js
 * It is the Javascript helper side for DateTimePickerTag.java but bound to the timezone-aware picker component
 */

import * as React from "react";
import { useState } from "react";
import ReactDOM from "react-dom";
import { localizedMoment } from "utils";
// TODO: Fix capitalization & `git mv`
import { DateTimePicker } from "components/datetime";

function mountDatePickerTo(mountingPoint: HTMLElement | null) {
  if (!mountingPoint) {
    Loggerhead.error("Found no mounting point for picker");
    return;
  }

  const name = mountingPoint.getAttribute("data-name");
  if (!name) {
    Loggerhead.error("Found no valid name for picker");
    return;
  }

  // Extract configuration options
  const hasDatePicker = mountingPoint.hasAttribute("data-has-date");
  const hasTimePicker = mountingPoint.hasAttribute("data-has-time");
  // Depending on the config, the expected format can be either 12-hour or 24-hour, if isAmPm is true, the format is 12-hour
  const isAmPm = mountingPoint.hasAttribute("data-is-am-pm");

  // Raw value is an ISO 8601 format date time string with timezone info intact, a-la `"2021-06-08T20:00+0100"`
  const rawValue = mountingPoint.getAttribute("data-value");
  // We store the expected UTC offset separately so we can set it back before setting values for the legacy inputs
  const utcOffset = localizedMoment.parseZone(rawValue).utcOffset();
  const initialValue = localizedMoment(rawValue);
  if (!rawValue || !initialValue.isValid()) {
    Loggerhead.error("Found no valid value for picker");
    return;
  }

  // Legacy input fields to store the result
  const yearInput = document.getElementById(`${name}_year`) as HTMLInputElement | null;
  const monthInput = document.getElementById(`${name}_month`) as HTMLInputElement | null;
  const dateInput = document.getElementById(`${name}_day`) as HTMLInputElement | null;
  const hourInput = document.getElementById(`${name}_hour`) as HTMLInputElement | null;
  const minuteInput = document.getElementById(`${name}_minute`) as HTMLInputElement | null;
  const amPmInput = document.getElementById(`${name}_am_pm`) as HTMLInputElement | null;
  if (!(yearInput && monthInput && dateInput && hourInput && minuteInput) || (isAmPm && !amPmInput)) {
    Loggerhead.error("Found no outputs for picker");
    return;
  }

  console.log({ hasDatePicker, hasTimePicker, isAmPm, rawValue, utcOffset });

  const Component = () => {
    const [value, setValue] = useState(initialValue);

    const onChange = (value: moment.Moment) => {
      // Create a copy of the value in the original UTC offset
      const adjustedValue = localizedMoment(value).utcOffset(utcOffset);
      console.log(
        adjustedValue.toString(),
        adjustedValue.year(),
        adjustedValue.month(),
        adjustedValue.date(),
        adjustedValue.hour(),
        adjustedValue.minute()
      );
      yearInput.value = adjustedValue.year().toString();
      monthInput.value = adjustedValue.month().toString();
      dateInput.value = adjustedValue.date().toString();

      const hour = adjustedValue.hour();
      if (isAmPm) {
        hourInput.value = (hour > 12 ? hour % 12 : hour).toString();
        amPmInput!.value = hour > 12 ? "1" : "0";
      } else {
        hourInput.value = hour.toString();
      }
      minuteInput.value = adjustedValue.minute().toString();

      setValue(value);
    };
    return (
      <DateTimePicker
        value={value}
        onChange={onChange}
        hideDatePicker={!hasDatePicker}
        hideTimePicker={!hasTimePicker}
      />
    );
  };

  // Only ever bind once
  mountingPoint.removeAttribute("class");
  ReactDOM.render(<Component />, mountingPoint);
}

// TODO: Also on SPA navigation
jQuery(document).ready(function() {
  Array.from(document.querySelectorAll<HTMLDivElement>(".legacy-date-time-picker")).forEach(node =>
    mountDatePickerTo(node)
  );
});

export {};
