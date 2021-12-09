import * as React from "react";
import ReactDOM from "react-dom";
import { localizedMoment } from "utils";
import { DateTime } from "components/datetime";

function mountDateTimeTo(mountingPoint: HTMLElement | null) {
  if (!mountingPoint) {
    Loggerhead.error("Found no mounting point for datetime");
    return;
  }

  // Raw value is an ISO 8601 format date time string with timezone info intact, a-la `"2021-06-08T20:00+0100"`
  const rawValue = mountingPoint.getAttribute("data-value") || mountingPoint.innerText;
  const value = localizedMoment(rawValue);
  if (!rawValue || !value.isValid()) {
    Loggerhead.error("Found no valid value for datetime");
    return;
  }

  ReactDOM.render(<DateTime value={value} />, mountingPoint);

  // Only ever bind once
  mountingPoint.removeAttribute("class");
  mountingPoint.removeAttribute("data-value");
}

function mountAll() {
  Array.from(document.querySelectorAll<HTMLDivElement>(".legacy-date-time")).forEach((node) => mountDateTimeTo(node));
}

jQuery(document).ready(mountAll);
window.pageRenderers?.spaengine?.onSpaEndNavigation?.(mountAll);
