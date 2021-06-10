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

console.log("Legacy fire!");

type Props = {
  name: string;
};

class LegacyDatetimePicker extends React.PureComponent<Props> {
  render() {
    return <p>{this.props.name}</p>;
  }
}

function getNestedAttribute(target: HTMLElement, attributeName: string): string | undefined {
  return target.querySelector(`[${attributeName}]`)?.getAttribute(attributeName) ?? undefined;
}

function getNestedNumericAttribute(target: HTMLElement, attributeName: string): number | undefined {
  return maybeNumber(getNestedAttribute(target, attributeName));
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

  // Extract relevant options
  const hasDate = !!inputGroupDiv.querySelector('[data-provide="date-picker"]');
  const hasTime = !!inputGroupDiv.querySelector('[data-provide="time-picker"]');
  const year = getNestedNumericAttribute(inputGroupDiv, "data-initial-year");
  const month = getNestedNumericAttribute(inputGroupDiv, "data-initial-month");
  const date = getNestedNumericAttribute(inputGroupDiv, "data-initial-day"); // Note different terminology
  const hour = getNestedNumericAttribute(inputGroupDiv, "data-initial-hour"); // TODO: This can be 12-hour AM/PM?
  const minute = getNestedNumericAttribute(inputGroupDiv, "data-initial-minute");

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
  console.log({ name, hasDate, hasTime, year, month, date, hour, minute });
  console.log(':::');

  // TODO: NB!!! Handle AM/PM divide when setting back values
  // TODO: What time zone do legacy pages want the inputs to be in?

  // Only ever bind once
  // inputGroupDiv.removeAttribute("data-provide");
  // inputGroupDiv.innerHTML = "";
  // ReactDOM.render(<LegacyDatetimePicker name="TODO" />, inputGroupDiv);
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
