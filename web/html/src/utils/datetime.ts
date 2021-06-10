import moment from "moment-timezone";

// TODO: Remove, these are only for easier debugging
window.userTimeZone = "America/Los_Angeles"; // GMT-7
window.serverTimeZone = "Asia/Tokyo"; // GMT+9

declare global {
  interface Window {
    userTimeZone?: string; // Mandatory, but try to recover if they're not present
    userDateFormat?: string; // Mandatory, but try to recover if they're not present
    userTimeFormat?: string; // Fully optional
    serverTimeZone?: string; // Fully optional
  }
}

if (!window.userTimeZone) {
  Loggerhead.error("User time zone not set, defaulting to UTC");
}
if (!window.serverTimeZone) {
  Loggerhead.error("Server time zone not set, defaulting to UTC");
}

const userTimeZone = window.userTimeZone || "UTC";
// See https://momentjs.com/docs/#/displaying/
const userDateFormat = window.userDateFormat || "YYYY-MM-DD";
const userTimeFormat = window.userTimeFormat || "HH:mm";
const serverTimeZone = window.serverTimeZone || "UTC";

declare module "moment" {
  export interface Moment {
    /** Get a localized date-time string in user's time zone, e.g. `"January 31, 2020 1:00 AM"` */
    toUserDateTimeString(): string;
    /** Get a localized date string in user's time zone, e.g. `"January 31, 2020"` */
    toUserDateString(): string;
    /** Get a localized time string in user's time zone, e.g. `"1:00 AM"` */
    toUserTimeString(): string;
    // TODO: Where and how do we need this?
    // TODO: Move this to the prototype not the instance instead?
    // TODO: Same coverage for server
    toServerDateTimeString(): string;
    toServerDateString(): string;
    toServerTimeString(): string;
    /** TODO: Check if redundant. Equal to .toISOString() */
    toAPIValue(): string;
  }

  /** Localized time zone string for the user, e.g. `"America/Los_Angeles"` */
  const userTimeZone: string;
  const userDateFormat: string;
  const userTimeFormat: string;
  const serverTimeZone: string;
}

// TODO: What else do we need here?
// TODO: Add descriptions
// TODO: What's a good way to name these
// TODO: Add tests that ensure the assigned props remain after using operations on it etc
moment.fn.toUserDateTimeString = function(this: moment.Moment): string {
  // Here and elsewhere, since moments are internally mutable, we make a copy before transitioning to a new timezone
  return moment(this)
    .tz(userTimeZone)
    .format(`${userDateFormat} ${userTimeFormat}`);
};

moment.fn.toUserDateString = function(this: moment.Moment): string {
  return moment(this)
    .tz(userTimeZone)
    .format(userDateFormat);
};

moment.fn.toUserTimeString = function(this: moment.Moment): string {
  return moment(this)
    .tz(userTimeZone)
    .format(userTimeFormat);
};

moment.fn.toServerDateTimeString = function(this: moment.Moment): string {
  return moment(this)
    .tz(serverTimeZone)
    .format(`${userDateFormat} ${userTimeFormat}`);
};

moment.fn.toServerDateString = function(this: moment.Moment): string {
  return moment(this)
    .tz(serverTimeZone)
    .format(userDateFormat);
};

moment.fn.toServerTimeString = function(this: moment.Moment): string {
  return moment(this)
    .tz(serverTimeZone)
    .format(userTimeFormat);
};

// TODO: Specify whether this should be a string, a Unix timestamp, or something else
moment.fn.toAPIValue = function(this: moment.Moment): string {
  return moment(this)
    .tz("UTC")
    .toISOString(false);
};

Object.defineProperties(moment, {
  userTimeZone: {
    value: userTimeZone,
    writable: false,
  },
  userDateFormat: {
    value: userDateFormat,
    writable: false,
  },
  userTimeFormat: {
    value: userTimeFormat,
    writable: false,
  },
  serverTimeZone: {
    value: serverTimeZone,
    writable: false,
  }
});

function localizedMoment(input?: moment.MomentInput) {
  // TODO: Specify string formats, don't allow inputs without a timezone
  const allowedFormats = [moment.ISO_8601];
  // We parse all inputs into UTC and only format them for output
  const utcMoment =
    typeof input === "string" ? moment.utc(input, allowedFormats, true).tz("UTC") : moment.utc(input, true).tz("UTC");

  if (!utcMoment.isValid()) {
    throw new RangeError("Invalid localized moment on input " + input);
  }

  return utcMoment;
}

const merged: typeof moment = Object.setPrototypeOf(localizedMoment, moment);
export { merged as localizedMoment };

// TODO: Only for debugging
(window as any).localizedMoment = merged;
