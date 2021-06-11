/**
 * This binding is a replacement for the old spacewalk-datetimepicker.js
 * It is the Javascript helper side for DateTimePickerTag.java but bound to the timezone-aware picker component
 *
 * options:
 *   startDate: preselected date in the picker (Date object)
 */

import * as React from "react";
import ReactDOM from "react-dom";
import { localizedMoment } from "utils";
// TODO: Fix capitalization & `git mv`
import { DateTimePicker } from "components/datetime";

console.log("Legacy fire!");

type Props = {
  name: string;
};

class LegacyDatetimePicker extends React.PureComponent<Props> {
  render() {
    return <p>{this.props.name}</p>;
  }
}

function getNestedAttribute(targetGroup: HTMLElement, attributeName: string): string | undefined {
  return targetGroup.querySelector(`[${attributeName}]`)?.getAttribute(attributeName) ?? undefined;
}

function maybeNumber(input?: string): number | undefined {
  if (typeof input === "undefined") {
    return undefined;
  }

  const parsed = parseInt(input, 10);
  if (isNaN(parsed)) {
    return undefined;
  }
  return parsed;
}

function getInitialValue(targetGroup: HTMLElement, fieldName: string) {
  return getNestedAttribute(targetGroup, `data-initial-${fieldName}`);
}

function getFallbackValue(pickerName: string, fieldName: string) {
  const target = document.getElementById(`${pickerName}_${fieldName}`) as HTMLInputElement | null;
  if (!target) {
    throw new TypeError(`Found no ${fieldName} field for picker ${pickerName}`);
  }
  return target.value;
}

/** Try and get an initial value from target group, a fallback value from hidden inputs, or undefined otherwise */
function getValue(targetGroup: HTMLElement, fallbackName: string, fieldName: string) {
  return getInitialValue(targetGroup, fieldName) ?? getFallbackValue(fallbackName, fieldName) ?? undefined;
}

function mountDatePickerTo(inputGroupDiv: HTMLElement | null) {
  if (!inputGroupDiv) {
    Loggerhead.error("Found no mounting point for picker");
    return;
  }

  // This is the same for both legacy date and time pickers
  const name = getNestedAttribute(inputGroupDiv, "data-picker-name");
  if (!name) {
    Loggerhead.error("Found no valid name for picker");
    return;
  }

  // Extract configuration options
  const hasDatePicker = !!inputGroupDiv.querySelector('[data-provide="date-picker"]');
  const hasTimePicker = !!inputGroupDiv.querySelector('[data-provide="time-picker"]');
  // Depending on the config, the expected result can be 12-hour or 24-hour
  const expectsAmPm = !!document.getElementById(`${name}_am_pm`);

  const year = maybeNumber(getValue(inputGroupDiv, name, "year"));
  const month = maybeNumber(getValue(inputGroupDiv, name, "month"));
  const date = maybeNumber(getValue(inputGroupDiv, name, "day")); // Note different terminology
  const minute = maybeNumber(getValue(inputGroupDiv, name, "minute"));

  const amPm = (expectsAmPm ? maybeNumber(getValue(inputGroupDiv, name, "am_pm")) : 0) ?? 0;
  // The initial value is provided in 24-hour format
  let hour = maybeNumber(getInitialValue(inputGroupDiv, "hour"));
  if (typeof hour === "undefined") {
    // NB! The fallback value _may or may not_ be in 12-hour format
    hour = maybeNumber(getFallbackValue(name, "hour"));
    if (typeof hour !== "undefined") {
      hour = (hour + amPm * 12) % 24;
    }
  }

  // TODO: Make work
  const outputTimezone = getValue(inputGroupDiv, name, "tz");

  // Merge the initial value
  const value = localizedMoment();
  if (year) {
    value.year(year);
  }
  if (month) {
    value.month(month);
  }
  if (date) {
    value.date(date);
  }
  if (hour) {
    value.hour(hour);
  }
  if (minute) {
    value.minute(minute);
  }
  console.log(value.toUserString());
  console.log({
    name,
    hasDatePicker,
    hasTimePicker,
    year,
    month,
    date,
    hour,
    minute,
    expectsAmPm,
    amPm,
    outputTimezone,
  });
  console.log(":::");

  // TODO: NB!!! Handle AM/PM divide when setting back values
  // TODO: What time zone do legacy pages want the inputs to be in?

  // Only ever bind once
  inputGroupDiv.removeAttribute("data-provide");
  inputGroupDiv.removeAttribute("class");
  inputGroupDiv.removeAttribute("id");
  inputGroupDiv.innerHTML = "";
  ReactDOM.render(<DateTimePicker value={value} onChange={value => console.log(value.toISOString())} />, inputGroupDiv);
}

function mountAll() {
  Array.from(document.querySelectorAll('input[data-provide="date-picker"]')).forEach(node =>
    mountDatePickerTo(node.closest(".input-group"))
  );
  Array.from(document.querySelectorAll('input[data-provide="time-picker"]')).forEach(node =>
    mountDatePickerTo(node.closest(".input-group"))
  );
}

// TODO: Also on SPA navigation
jQuery(document).ready(function() {
  mountAll();
});

export {};
