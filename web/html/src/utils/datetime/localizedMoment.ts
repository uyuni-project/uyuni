import moment from "moment";

// TODO: Remove, these are only for easier debugging
window.serverTime = moment()
  .utcOffset(9)
  .toISOString(true);
window.serverTimeZoneString = "GMT+9";
window.userTime = moment()
  .utcOffset(-7)
  .toISOString(true);
window.userTimeZoneString = "GMT-7";

declare global {
  interface Window {
    serverTime?: string;
    serverTimeZoneString?: string;
    userTime?: string;
    userTimeZoneString?: string;
    userDateFormat?: string; // Optional
    userTimeFormat?: string; // Optional
  }
}

const serverUtcOffset = moment.parseZone(window.serverTime || moment(), moment.ISO_8601, true).utcOffset();
const serverTimeZoneString = window.serverTimeZoneString || t("Local");
if (!window.serverTime) {
  Loggerhead.error(`Server time not available, defaulting to browser time (UTC offset ${serverUtcOffset} minutes)`);
}

const userUtcOffset = window.userTime
  ? moment.parseZone(window.userTime, moment.ISO_8601, true).utcOffset()
  : serverUtcOffset;
const userTimeZoneString = window.userTimeZoneString || serverTimeZoneString;
if (!window.userTime) {
  Loggerhead.error(`User time not available, defaulting to server time (UTC offset ${serverUtcOffset} minutes)`);
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

  const serverTimeZone: {
    /** The server's time zone as an offset from UTC in minutes */
    utcOffset: number;
    /** The server's time zone as a string, e.g. `"GMT+9"` */
    displayValue: string;
  };
  const userTimeZone: {
    /** The user's time zone as an offset from UTC in minutes */
    utcOffset: number;
    /** The user's time zone as a string, e.g. `"GMT+9"` */
    displayValue: string;
  };
}

moment.fn.toServerString = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(serverUtcOffset)
    .format(`${userDateFormat} ${userTimeFormat} [${serverTimeZoneString}]`);
};

moment.fn.toServerDateTimeString = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(serverUtcOffset)
    .format(`${userDateFormat} ${userTimeFormat}`);
};

moment.fn.toServerDateString = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(serverUtcOffset)
    .format(userDateFormat);
};

moment.fn.toServerTimeString = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(serverUtcOffset)
    .format(userTimeFormat);
};

moment.fn.toUserString = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(userUtcOffset)
    .format(`${userDateFormat} ${userTimeFormat} [${userTimeZoneString}]`);
};

moment.fn.toUserDateTimeString = function(this: moment.Moment): string {
  // Here and elsewhere, since moments are internally mutable, we make a copy before transitioning to a new timezone
  return moment(this)
    .utcOffset(userUtcOffset)
    .format(`${userDateFormat} ${userTimeFormat}`);
};

moment.fn.toUserDateString = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(userUtcOffset)
    .format(userDateFormat);
};

moment.fn.toUserTimeString = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(userUtcOffset)
    .format(userTimeFormat);
};

// TODO: This is obsolete after the API update PR is merged
moment.fn.toAPIValue = function(this: moment.Moment): string {
  return moment(this)
    .utcOffset(0)
    .toISOString(false);
};

Object.defineProperties(moment, {
  serverTimeZone: {
    value: {
      utcOffset: serverUtcOffset,
      displayValue: serverTimeZoneString,
    } as typeof moment["serverTimeZone"],
    writable: false,
  },
  userTimeZone: {
    value: {
      utcOffset: userUtcOffset,
      displayValue: userTimeZoneString,
    } as typeof moment["userTimeZone"],
    writable: false,
  },
});

function localizedMomentConstructor(input?: moment.MomentInput) {
  // We make all inputs UTC internally and only format them for output
  const utcMoment = typeof input === "string" ? moment(input, moment.ISO_8601, true).utc() : moment(input, true).utc();

  if (!utcMoment.isValid()) {
    throw new RangeError("Invalid localized moment on input " + JSON.stringify(input));
  }

  return utcMoment;
}

const localizedMoment: typeof moment = Object.setPrototypeOf(localizedMomentConstructor, moment);
export { localizedMoment };

// TODO: Only for debugging
(window as any).localizedMoment = localizedMoment;
