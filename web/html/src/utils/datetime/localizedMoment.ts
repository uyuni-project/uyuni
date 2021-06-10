import moment from "moment-timezone";

// TODO: Remove, these are only for easier debugging
window.serverTimeZone = "Asia/Tokyo"; // GMT+9
window.userTimeZone = "America/Los_Angeles"; // GMT-7

declare global {
  interface Window {
    userTimeZone?: string; // Mandatory, but try to recover if they're not present
    userDateFormat?: string; // Mandatory, but try to recover if they're not present
    userTimeFormat?: string; // Fully optional
    serverTimeZone?: string; // Fully optional
  }
}

const serverTimeZone = window.serverTimeZone || "UTC";
if (!window.serverTimeZone) {
  Loggerhead.error("Server time zone not set, defaulting to UTC");
}

const userTimeZone = window.userTimeZone || serverTimeZone;
if (!window.userTimeZone) {
  Loggerhead.error(`User time zone not set, defaulting to server time zone (${serverTimeZone})`);
}

// See https://momentjs.com/docs/#/displaying/
const userDateFormat = window.userDateFormat || "YYYY-MM-DD";
const userTimeFormat = window.userTimeFormat || "HH:mm";

declare module "moment" {
  export interface Moment {
    /**
     * Unless you specifically need the server time, please use the `toUser...` equivalent.
     * Get a localized date-time-zone string in the server's time zone, e.g. `"2020-01-31 13:00 GMT+9"`
     */
    toServerString(): string;
    /**
     * Unless you specifically need the server time, please use the `toUser...` equivalent.
     * Get a localized date-time string in the servers's time zone, e.g. `"2020-01-31 13:00"`
     */
    toServerDateTimeString(): string;
    /**
     * Unless you specifically need the server time, please use the `toUser...` equivalent.
     * Get a localized date string in the server's time zone, e.g. `"2020-01-31"`
     */
    toServerDateString(): string;
    /**
     * Unless you specifically need the server time, please use the `toUser...` equivalent.
     * Get a localized time string in the server's time zone, e.g. `"13:00"`
     */
    toServerTimeString(): string;

    /** Get a localized date-time-zone string in the user's time zone, e.g. `"2020-01-31 13:00 GMT+9"` */
    toUserString(): string;
    /** Get a localized date-time string in the user's time zone, e.g. `"2020-01-31 13:00"` */
    toUserDateTimeString(): string;
    /** Get a localized date string in the user's time zone, e.g. `"2020-01-31"` */
    toUserDateString(): string;
    /** Get a localized time string in the user's time zone, e.g. `"13:00"` */
    toUserTimeString(): string;

    /** TODO: This is redundant, stringifying a moment already makes it an ISO string */
    toAPIValue(): string;
  }

  /** The server's time zone, e.g. `"GMT+9"` */
  const serverTimeZone: string;
  /** The user's time zone, e.g. `"GMT+9"` */
  const userTimeZone: string;
}

moment.fn.toServerString = function(this: moment.Moment): string {
  return moment(this)
    .tz(serverTimeZone)
    .format(`${userDateFormat} ${userTimeFormat} [${serverTimeZone}]`);
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

moment.fn.toUserString = function(this: moment.Moment): string {
  return moment(this)
    .tz(userTimeZone)
    .format(`${userDateFormat} ${userTimeFormat} [${userTimeZone}]`);
};

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

// TODO: This is obsolete
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
  serverTimeZone: {
    value: serverTimeZone,
    writable: false,
  },
});

function localizedMomentConstructor(input?: moment.MomentInput) {
  const allowedFormats = [moment.ISO_8601];
  // We parse all inputs into UTC and only format them for output
  const utcMoment =
    typeof input === "string" ? moment.utc(input, allowedFormats, true).tz("UTC") : moment.utc(input, true).tz("UTC");

  if (!utcMoment.isValid()) {
    throw new RangeError("Invalid localized moment on input " + JSON.stringify(input));
  }

  return utcMoment;
}

const localizedMoment: typeof moment = Object.setPrototypeOf(localizedMomentConstructor, moment);
export { localizedMoment };

// TODO: Only for debugging
(window as any).localizedMoment = localizedMoment;
