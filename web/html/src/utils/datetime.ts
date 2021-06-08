import moment from "moment-timezone";

declare global {
  interface Window {
    userTimeZone?: string;
    serverTimeZone?: string;
  }
}

const DEFAULT_TIME_ZONE = "UTC";
if (!window.userTimeZone) {
  Loggerhead.error(`User time zone not set, defaulting to ${DEFAULT_TIME_ZONE}`);
}
if (!window.serverTimeZone) {
  Loggerhead.error(`Server time zone not set, defaulting to ${DEFAULT_TIME_ZONE}`);
}

const userTimeZone = window.userTimeZone || DEFAULT_TIME_ZONE;
const serverTimeZone = window.serverTimeZone || DEFAULT_TIME_ZONE;

// TODO: What else do we need here?
// TODO: Add descriptions
function toUserDisplayString(this: LocalizedMoment) {
  // TODO: .__internalFormat() instead
  // Since moments are internally mutable, we make a copy before transitioning to a new timezone
  return moment(this)
    .tz(userTimeZone)
    .toISOString(true);
}

function toServerDisplayString(this: LocalizedMoment) {
  return moment(this)
    .tz(serverTimeZone)
    .toISOString(true);
}

function toAPIString(this: LocalizedMoment) {
  return moment(this)
    .tz("UTC")
    .toISOString(false);
}

function throwOnManualFormat(...args: unknown[]) {
  throw new Error("Trying to manually format a localized moment");
}

export type LocalizedMoment = ReturnType<typeof localizedMoment>;

export default function localizedMoment(input?: Exclude<moment.MomentInput, Date>) {
  // Please don't use raw Javascript Date instances
  if (input instanceof Date) {
    throw new TypeError("Raw Javascript Date instance passed to localized moment");
  }

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

  const __internalFormat = utcMoment.format;
  // TODO: Add tests that ensure the assigned props remain after using operations on it etc
  return Object.assign(utcMoment, {
    toUserDisplayString,
    toServerDisplayString,
    toAPIString,
    toISOString: toAPIString,
    /**
     * Please don't use `format()` to manually format a localized moment instance.
     * Instead of doing this, please use one of the localized instance methods like `toUserString()` etc to
     * consistently output a correctly localized datetime strings.
     */
    format: throwOnManualFormat,
    /**
     * Please don't use `toString()` to manually format a localized moment instance.
     * Instead of doing this, please use one of the localized instance methods like `toUserString()` etc to
     * consistently output a correctly localized datetime strings.
     */
    toString: throwOnManualFormat,
    // TODO: Check whether `this` is bound correctly
    __internalFormat,
  });
}
