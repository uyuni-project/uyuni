/* eslint-disable local-rules/no-raw-date */
import moment from "moment-timezone";

// NB! This must be up to date with UserPreferenceUtils.java
const dateFormat = "YYYY-MM-DD";
const shortTimeFormat = "HH:mm";
const longTimeFormat = "HH:mm:ss";

const fallbackLocale = "en";
const locale = window.preferredLocale ?? fallbackLocale;
if (!window.preferredLocale) {
  Loggerhead.error(`No locale info available, falling back to default locale ${fallbackLocale}`);
}

moment.locale(locale);
// Ensure consistent formatting between our own formatters and `localizedMoment().calendar()` etc
moment.updateLocale(locale, {
  longDateFormat: {
    LT: shortTimeFormat,
    LTS: longTimeFormat,
    L: dateFormat,
    LL: dateFormat,
    LLL: dateFormat,
    LLLL: dateFormat,
  },
  calendar: {
    lastWeek: dateFormat,
    nextWeek: dateFormat,
    sameElse: dateFormat,
  },
});

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
  }
}

if (process.env.NODE_ENV !== "production") {
  (window as any).localizedMoment = localizedMomentConstructor;
}

function validateOrGuessTimeZone(input: string | undefined, errorLabel: "Server" | "User") {
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
 * The login page doesn't have data such as `window.serverTimeZone` and `window.userTimeZone` available, but includes
 * this file via the legacy support module. To avoid throwing or storing incorrect values, we only initialize those
 * config values once actually used.
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

  // We currently don't support configuring these, but keep them in the config in case we ever do.
  // NB! If these ever become configurable, check whether the `moment.updateLocale()` call needs to be updated too.
  userDateFormat: dateFormat,
  userShortTimeFormat: shortTimeFormat,
  userLongTimeFormat: longTimeFormat,
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

  /** Full server time zone string, e.g. `"Asia/Tokyo"` */
  const serverTimeZone: string;
  /** Abbreviated server time zone string, e.g. `"JST"` */
  const serverTimeZoneAbbr: string;
  /** Full user time zone string, e.g. `"Asia/Tokyo"` */
  const userTimeZone: string;
  /** Abbreviated user time zone string, e.g. `"JST"` */
  const userTimeZoneAbbr: string;
}

moment.fn.toServerString = function (this: moment.Moment): string {
  // Here and elsewhere, since moments are internally mutable, we make a copy before transitioning to a new timezone
  return moment(this).tz(config.serverTimeZone).format(`${config.userDateFormat} ${config.userShortTimeFormat} z`);
};

moment.fn.toServerDateTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.serverTimeZone).format(`${config.userDateFormat} ${config.userShortTimeFormat}`);
};

moment.fn.toServerDateString = function (this: moment.Moment): string {
  return moment(this).tz(config.serverTimeZone).format(config.userDateFormat);
};

moment.fn.toServerTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.serverTimeZone).format(config.userShortTimeFormat);
};

moment.fn.toUserString = function (this: moment.Moment): string {
  return moment(this).tz(config.userTimeZone).format(`${config.userDateFormat} ${config.userShortTimeFormat} z`);
};

moment.fn.toUserDateTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.userTimeZone).format(`${config.userDateFormat} ${config.userShortTimeFormat}`);
};

moment.fn.toUserDateString = function (this: moment.Moment): string {
  return moment(this).tz(config.userTimeZone).format(config.userDateFormat);
};

moment.fn.toUserTimeString = function (this: moment.Moment): string {
  return moment(this).tz(config.userTimeZone).format(config.userShortTimeFormat);
};

let serverTimeZoneAbbr: undefined | string = undefined;
let userTimeZoneAbbr: undefined | string = undefined;
Object.defineProperties(moment, {
  serverTimeZone: {
    get() {
      return config.serverTimeZone as typeof moment["serverTimeZone"];
    },
  },
  serverTimeZoneAbbr: {
    get() {
      return (serverTimeZoneAbbr ??= moment().tz(config.serverTimeZone).format("z"));
    },
  },
  userTimeZone: {
    get() {
      return config.userTimeZone as typeof moment["serverTimeZone"];
    },
  },
  userTimeZoneAbbr: {
    get() {
      return (userTimeZoneAbbr ??= moment().tz(config.userTimeZone).format("z"));
    },
  },
});

const parseTimeString = function (input: string): { hours: number; minutes: number } | null {
  const parsed = moment(input.trim(), ["H:m", "Hm"]);
  if (parsed.isValid()) {
    return {
      hours: parsed.hours(),
      minutes: parsed.minutes(),
    };
  }
  return null;
};

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
export type LocalizedMoment = ReturnType<typeof localizedMoment>;
export { localizedMoment, parseTimeString };
