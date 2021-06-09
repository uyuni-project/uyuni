import moment from "moment-timezone";

// TODO: Remove, these are only for easier debugging
window.userTimeZone = "America/Los_Angeles";
window.serverTimeZone = "Asia/Tokyo";

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
// See "Localized formats" in https://momentjs.com/docs/#/displaying/
const userDateFormat = window.userDateFormat || "LL";
const userTimeFormat = window.userTimeFormat || "LT";
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
    /** Get a localized time zone string for the user, e.g. `"America/Los_Angeles"` */
    toUserTimeZoneString(): string;
    // TODO: Same coverage for server
    toServerDateTimeString(): string;
    toServerDateString(): string;
    toServerTimeString(): string;
    toServerTimeZoneString(): string;
    toAPIValue(): string;
  }
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

moment.fn.toUserTimeZoneString = function(this: moment.Moment) {
  return userTimeZone;
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

moment.fn.toServerTimeZoneString = function(this: moment.Moment) {
  return serverTimeZone;
};

// TODO: Specify whether this should be a string, a Unix timestamp, or something else
moment.fn.toAPIValue = function(this: moment.Moment): string {
  return moment(this)
    .tz("UTC")
    .toISOString(false);
};

function localizedMoment(input?: moment.MomentInput) {
  // TODO: Specify string formats, don't allow inputs without a timezone
  const allowedFormats = [moment.ISO_8601];
  const utcMoment =
    typeof input === "string"
      ? // We parse all inputs into UTC and only format them for output
        moment.utc(input, allowedFormats, true).tz("UTC")
      : moment.utc(input, true).tz("UTC");

  if (!utcMoment.isValid()) {
    throw new RangeError("Invalid localized moment");
  }

  return utcMoment;
}

const merged: typeof moment = Object.setPrototypeOf(localizedMoment, moment);
export { merged as localizedMoment };
