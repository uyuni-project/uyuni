// Humanize dates for `FormatDateTag.java` the same way we do in frontend code

import * as React from "react";

import ReactDOM from "react-dom";

import { FromNow, HumanDateTime } from "components/datetime";

import { localizedMoment } from "utils";

const FROM_NOW_CLASSNAME = "human-from";
const HUMAN_DATE_TIME_CLASSNAME = "human-calendar";

const getValue = (element: HTMLElement) => {
  // If the attribute is not set, the content should be a valid date
  const input = element.getAttribute("datetime") ?? element.innerText;
  const value = localizedMoment(input);
  if (!value.isValid()) {
    return undefined;
  }
  return value;
};

function mountFromNow(mountingPoint: HTMLElement | null) {
  if (!mountingPoint) {
    Loggerhead.error("Found no mounting point for FromNow");
    return;
  }
  // Only ever bind once
  mountingPoint.classList.remove(FROM_NOW_CLASSNAME);

  const value = getValue(mountingPoint);
  if (value) {
    // Replace the original mounting point with the result since we want to avoid double wrapping
    const node = document.createElement("div");
    ReactDOM.render(<FromNow value={value} />, node);
    mountingPoint.outerHTML = node.innerHTML;
  }
}

function mountHumanDateTime(mountingPoint: HTMLElement | null) {
  if (!mountingPoint) {
    Loggerhead.error("Found no mounting point for FromNow");
    return;
  }
  // Only ever bind once
  mountingPoint.classList.remove(HUMAN_DATE_TIME_CLASSNAME);

  const value = getValue(mountingPoint);
  if (value) {
    // Replace the original mounting point with the result since we want to avoid double wrapping
    const node = document.createElement("div");
    ReactDOM.render(<HumanDateTime value={value} />, node);
    mountingPoint.outerHTML = node.innerHTML;
  }
}

function mountAll() {
  Array.from(document.querySelectorAll<HTMLDivElement>(`time.${FROM_NOW_CLASSNAME}`)).forEach((node) =>
    mountFromNow(node)
  );
  Array.from(document.querySelectorAll<HTMLDivElement>(`time.${HUMAN_DATE_TIME_CLASSNAME}`)).forEach((node) =>
    mountHumanDateTime(node)
  );
}

jQuery(document).ready(mountAll);
window.pageRenderers?.spaengine?.onSpaEndNavigation?.(mountAll);
