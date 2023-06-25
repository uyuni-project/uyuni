/**
 * This binding is a replacement for the old spacewalk-datetimepicker.js
 * It is the Javascript helper side for DateTimePickerTag.java but bound to the timezone-aware picker component.
 * This wrapper is NOT HMR-ready, you will need to reload for your changes.
 */
import * as React from "react";
import { useState } from "react";

import ReactDOM from "react-dom";

import { DateTimePicker } from "components/datetime";

import { localizedMoment } from "utils";

function mountDateTimePickerTo(mountingPoint: HTMLElement | null) {
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
  const rawValue = mountingPoint.getAttribute("data-value") || mountingPoint.innerText;
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

  const StatefulWrapper = () => {
    const [value, setValue] = useState(initialValue);

    const onChange = (value: moment.Moment) => {
      // We transfer the date without timezone information and the server implicitly expects the values
      // to match the users configured timezone. Since we get the date in utc we need to convert it to the
      // users configured timezone again.
      const adjustedValue = localizedMoment(value).tz(localizedMoment.userTimeZone);
      yearInput.value = adjustedValue.year().toString();
      monthInput.value = adjustedValue.month().toString();
      dateInput.value = adjustedValue.date().toString();

      const hour = adjustedValue.hour();
      if (isAmPm) {
        hourInput.value = (hour > 12 ? hour % 12 : hour).toString();
        amPmInput!.value = hour >= 12 ? "1" : "0";
      } else {
        hourInput.value = hour.toString();
      }

      minuteInput.value = adjustedValue.minute().toString();
      setValue(value);
    };

    return (
      <DateTimePicker
        legacyId={name}
        value={value}
        onChange={onChange}
        hideDatePicker={!hasDatePicker}
        hideTimePicker={!hasTimePicker}
      />
    );
  };

  ReactDOM.render(<StatefulWrapper />, mountingPoint);

  // Only ever bind once
  mountingPoint.removeAttribute("class");
  // Clean up the DOM
  mountingPoint.removeAttribute("data-name");
  mountingPoint.removeAttribute("data-has-date");
  mountingPoint.removeAttribute("data-has-time");
  mountingPoint.removeAttribute("data-is-am-pm");
  mountingPoint.removeAttribute("data-value");
}

function mountAll() {
  Array.from(document.querySelectorAll<HTMLDivElement>(".legacy-date-time-picker")).forEach((node) =>
    mountDateTimePickerTo(node)
  );
}

jQuery(document).ready(mountAll);
window.pageRenderers?.spaengine?.onSpaEndNavigation?.(mountAll);
