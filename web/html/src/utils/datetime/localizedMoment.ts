import moment from "moment-timezone";

// TODO: Only for testing
/*
window.serverTimeZone = "Asia/Tokyo"; // GMT+9
window.serverTime = "2020-01-31T08:00:00.000+09:00";
window.userTimeZone = "America/Los_Angeles"; // GMT-7
*/

declare global {
  interface Window {
    /** The server time as an ISO string with offset intact, e.g. `"2020-01-31T08:00:00.000+09:00"` */
    serverTime?: string;
    /** The server IANA time zone, e.g. `"Asia/Tokyo"` */
    serverTimeZone?: string;
    /** The user's configured IANA time zone, e.g. `"Asia/Tokyo"` */
    userTimeZone?: string;
    userDateFormat?: string; // Optional
    userTimeFormat?: string; // Optional
  }
}

function validateOrGuessTimeZone(input: string | undefined, errorLabel: string) {
  try {
    if (!input) {
      throw new TypeError(`${errorLabel} time zone not configured`);
    }
    // moment.tz will throw if there's no data available for the given zone
    moment.tz(input);
  } catch (error) {
    const guess = moment.tz.guess(true);
    Loggerhead.error(`${errorLabel} time zone not available, defaulting to guessed time zone (${guess}). ${error}`);
    return guess;
  }
  return input;
}

const serverTimeZone = validateOrGuessTimeZone(window.serverTimeZone, "Server");
const userTimeZone = validateOrGuessTimeZone(window.userTimeZone, "User");

// See https://momentjs.com/docs/#/displaying/
const userDateFormat = window.userDateFormat || "YYYY-MM-DD";
const userTimeFormat = window.userTimeFormat || "HH:mm";

// Sanity check
if (window.serverTime) {
  const diff = localizedMomentConstructor(window.serverTime).diff(localizedMomentConstructor(), "minutes");
  if (Math.abs(diff) > 10) {
    Loggerhead.error(`Server and browser time differ considerably (${diff} minutes)`);
  }
} else {
  Loggerhead.error(`Server time not available`);
}

declare module "moment" {
  export interface Moment {
    /**
     * Unless you specifically need the server time, please use the `toUser...` equivalent.
     * Get a localized date-time-zone string in the server's time zone, e.g. `"2020-01-31 13:00 Asia/Tokyo"`
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

    /** Get a localized date-time-zone string in the user's time zone, e.g. `"2020-01-31 13:00 Asia/Tokyo"` */
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

  const serverTimeZone: string;
  const userTimeZone: string;
}

moment.fn.toServerString = function(this: moment.Moment): string {
  // Here and elsewhere, since moments are internally mutable, we make a copy before transitioning to a new timezone
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

// TODO: This is obsolete after the API update PR is merged
moment.fn.toAPIValue = function(this: moment.Moment): string {
  return moment(this)
    .tz("UTC")
    .toISOString(false);
};

Object.defineProperties(moment, {
  serverTimeZone: {
    value: serverTimeZone as typeof moment["serverTimeZone"],
    writable: false,
  },
  userTimeZone: {
    value: userTimeZone as typeof moment["serverTimeZone"],
    writable: false,
  },
});

function localizedMomentConstructor(input?: moment.MomentInput) {
  // We make all inputs UTC internally and only format them for output
  const utcMoment =
    typeof input === "string"
      ? moment(input, moment.ISO_8601, true)
          .utc()
          .tz("UTC")
      : moment(input, true)
          .utc()
          .tz("UTC");

  if (!utcMoment.isValid()) {
    throw new RangeError("Invalid localized moment on input " + JSON.stringify(input));
  }

  return utcMoment;
}

const localizedMoment: typeof moment = Object.setPrototypeOf(localizedMomentConstructor, moment);
export { localizedMoment };
