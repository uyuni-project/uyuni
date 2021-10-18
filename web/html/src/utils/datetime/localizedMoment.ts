/* eslint-disable local-rules/no-raw-date */
import moment from "moment-timezone";

declare global {
  interface Window {
    /** The server IANA time zone, e.g. `"Asia/Tokyo"` */
    serverTimeZone?: string;
    /**
     * The server time as an ISO string with offset intact, e.g. `"2020-01-31T08:00:00.000+09:00"`
     * This is **not** used for calculations, only for sanity checks
     */
    serverTime?: string;
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

/**
 * The login page doesn't have user-specific data available, but includes this file via the legacy support module.
 * To avoid throwing or storing incorrect values, we only initialize config values once actually used.
 */
const config = {
  _serverTimeZone: undefined as string | undefined,
  get serverTimeZone(): string {
    return (this._serverTimeZone ??= validateOrGuessTimeZone(window.serverTimeZone, "Server"));
  },

  _userTimeZone: undefined as string | undefined,
  get userTimeZone(): string {
    return (this._userTimeZone ??= validateOrGuessTimeZone(window.userTimeZone, "User"));
  },

  // See https://momentjs.com/docs/#/displaying/
  _userDateFormat: undefined as string | undefined,
  get userDateFormat(): string {
    return (this._userDateFormat ??= window.userDateFormat || "YYYY-MM-DD");
  },

  _userTimeFormat: undefined as string | undefined,
  get userTimeFormat(): string {
    return (this._userTimeFormat ??= window.userTimeFormat || "HH:mm");
  },
};

// Sanity check, if the server and the browser have wildly differing time zone adjusted time, someone is probably wrong
if (window.serverTime) {
  const diffMinutes = localizedMomentConstructor(window.serverTime).diff(localizedMomentConstructor(), "minutes");
  if (Math.abs(diffMinutes) > 10) {
    Loggerhead.error(
      `Server and browser disagree on what the time is despite accounting for time zones, server time is ${moment(
        window.serverTime
      ).toISOString(true)}, browser time is ${moment().toISOString(true)}`
    );
  }
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
  }

  const serverTimeZone: string;
  const userTimeZone: string;
}

moment.fn.toServerString = function (this: moment.Moment): string {
  // Here and elsewhere, since moments are internally mutable, we make a copy before transitioning to a new timezone
  return moment(this)
    .tz(config.serverTimeZone)
    .format(`${config.userDateFormat} ${config.userTimeFormat} [${config.serverTimeZone}]`);
};

moment.fn.toServerDateTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.serverTimeZone).format(`${config.userDateFormat} ${config.userTimeFormat}`);
};

moment.fn.toServerDateString = function (this: moment.Moment): string {
  return moment(this).tz(config.serverTimeZone).format(config.userDateFormat);
};

moment.fn.toServerTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.serverTimeZone).format(config.userTimeFormat);
};

moment.fn.toUserString = function (this: moment.Moment): string {
  return moment(this)
    .tz(config.userTimeZone)
    .format(`${config.userDateFormat} ${config.userTimeFormat} [${config.userTimeZone}]`);
};

moment.fn.toUserDateTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.userTimeZone).format(`${config.userDateFormat} ${config.userTimeFormat}`);
};

moment.fn.toUserDateString = function (this: moment.Moment): string {
  return moment(this).tz(config.userTimeZone).format(config.userDateFormat);
};

moment.fn.toUserTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.userTimeZone).format(config.userTimeFormat);
};

Object.defineProperties(moment, {
  serverTimeZone: {
    get() {
      return config.serverTimeZone as typeof moment["serverTimeZone"];
    },
  },
  userTimeZone: {
    get() {
      return config.userTimeZone as typeof moment["serverTimeZone"];
    },
  },
});

function localizedMomentConstructor(input?: moment.MomentInput) {
  // We make all inputs UTC internally and only format them for output
  const utcMoment =
    typeof input === "string"
      ? moment(input, moment.ISO_8601, true).utc().tz("UTC")
      : moment(input, true).utc().tz("UTC");

  if (!utcMoment.isValid()) {
    throw new RangeError("Invalid localized moment on input " + JSON.stringify(input));
  }

  return utcMoment;
}

const localizedMoment: typeof moment = Object.setPrototypeOf(localizedMomentConstructor, moment);
export { localizedMoment };
