// This as opposed to a regular type definition lets Typescript know we're dealing with a real promise-like in async contexts
export class Cancelable<T = any> extends Promise<T> {
  promise!: Promise<T>;
  cancel!: (reason?: any) => void;
}

function cancelable<T = any>(promise: Promise<T>, onCancel?: (arg0: Error | void) => void): Cancelable<T> {
  let rejectFn: (reason: any) => void;
  let isCancelled = false;

  const cancelPromise = new Promise<T>((resolve, reject) => {
    rejectFn = reject;
  });

  const race = Promise.race([promise, cancelPromise]).catch(error => {
    if (isCancelled) {
      onCancel?.(error);
    }
    throw error;
  });

  /**
   * Instead of returning a plain object, we return a promise with additional fields
   * This way we can be backwards compatible with old usage of the Cancelable interface
   *  while also allowing using await directly on a Cancelable.
   */
  const castRace = race as Cancelable<T>;
  castRace.promise = race;
  castRace.cancel = (reason: any) => {
    isCancelled = true;
    rejectFn(reason);
  };
  return castRace;
}

function LocalDateTime(date: Date): string {
  const padTo = v => {
    v = v.toString();
    if (v.length >= 2) return v;
    else return padTo("0" + v);
  };
  const year = date.getFullYear();
  const month = date.getMonth();
  const days = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();
  return (
    "" +
    year +
    "-" +
    padTo(month + 1) +
    "-" +
    padTo(days) +
    "T" +
    padTo(hours) +
    ":" +
    padTo(minutes) +
    ":" +
    padTo(seconds)
  );
}

function sortById(aRaw: any, bRaw: any): number {
  const aId = aRaw["id"];
  const bId = bRaw["id"];
  return aId > bId ? 1 : aId < bId ? -1 : 0;
}

function sortByText(aRaw: any, bRaw: any, columnKey: string, sortDirection: number): number {
  var a = aRaw[columnKey];
  var b = bRaw[columnKey];
  var result = (a == null ? "" : a).toLowerCase().localeCompare((b == null ? "" : b).toLowerCase());
  return (result || sortById(aRaw, bRaw)) * sortDirection;
}

function sortByNumber(aRaw: any, bRaw: any, columnKey: string, sortDirection: number): number {
  const result = aRaw[columnKey] > bRaw[columnKey] ? 1 : aRaw[columnKey] < bRaw[columnKey] ? -1 : 0;
  return result * sortDirection;
}

function sortByDate(aRaw: any, bRaw: any, columnKey: string, sortDirection: number): number {
  /**
   *  HACK
   *
   * Expected input String format: "YYYY-MM-DD HH:mm:ss z" where "z" is the timezone
   * This is an 'unparsable' format for a javascript Date
   *
   * Assuming both dates are on the same timezone, we need to drop the timezone information in order to
   * convert the String into a javascript Date and compare which one is before the other. In the end,
   * if the initial assumption is true, the offset is the same.
   * The troubles could start if the String format is not as expected or if the two timezones to compare are not the same.
   *
   * Expected output String format to convert to a javascript Date "YYYY-MM-DD HH:mm:ss"
   *
   * Explaining the regex:
   *   "d{2,4}" => DD or YYYY
   *   "." => anyseparator
   *   "d{2}" => MM
   *   "." => anyseparator
   *   "d{2,4}" => DD or YYYY
   *   "." => anyseparator
   *   "d{1,2}" => h or HH
   *   "." => anyseparator
   *   "d{2}" => mm
   *   "." => anyseparator
   *   "d{2}" => ss
   *   " \w+" => any timezone letters with a 'space' as a prefix separator from seconds; the timezone catching group is optionable
   */
  const unparsableDateRegex = /(\d{2,4}.\d{2}.\d{2,4}.\d{1,2}.\d{2}.\d{2})( \w+)*/g;

  const aDate =
    aRaw[columnKey] instanceof Date ? aRaw[columnKey] : new Date(aRaw[columnKey].replace(unparsableDateRegex, "$1"));
  const bDate =
    bRaw[columnKey] instanceof Date ? bRaw[columnKey] : new Date(bRaw[columnKey].replace(unparsableDateRegex, "$1"));

  const result = aDate > bDate ? 1 : aDate < bDate ? -1 : 0;
  return result * sortDirection;
}

function getQueryStringValue(key: string): string {
  // See for a standard implementation:
  // https://developer.mozilla.org/en-US/docs/Web/API/URLSearchParams
  return decodeURIComponent(
    window.location.search.replace(
      new RegExp("^(?:.*[&\\?]" + encodeURIComponent(key).replace(/[.+*]/g, "\\$&") + "(?:\\=([^&]*))?)?.*$", "i"),
      "$1"
    )
  );
}

function urlBounce(defaultUrl: string, qstrParamKey?: string): void {
  window.location.href = getQueryStringValue(qstrParamKey || "url_bounce") || defaultUrl;
}

/**
 * Replace all "_" and "-" with spaces and capitalize the first letter of each word
 */
function capitalize(str: string): string {
  return str.replace(new RegExp("_|-", "g"), " ").replace(/\w\S*/g, function(txt) {
    return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
  });
}

function generatePassword(): string {
  const length = Math.floor(Math.random() * 10) + 15;
  const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-_";
  let password = "";
  if (window.crypto && window.crypto.getRandomValues) {
    var rand = new Uint16Array(length);
    window.crypto.getRandomValues(rand);
    for (let i = 0; i < length; i++) password += charset.charAt(Math.floor((rand[i] * charset.length) / 65536));
  } else {
    for (let i = 0; i < length; i++) password += charset.charAt(Math.floor(Math.random() * charset.length));
  }
  return password;
}

// todo: shouldn't we stick to JS terminology here? (array/object vs. list/dict?)
const EditGroupSubtype = Object.freeze({
  PRIMITIVE_LIST: Symbol("primitiveList"),
  PRIMITIVE_DICTIONARY: Symbol("primitiveDictionary"),
  LIST_OF_DICTIONARIES: Symbol("listOfDictionaries"),
  DICTIONARY_OF_DICTIONARIES: Symbol("dictionaryOfDictionaries"),
});

function getEditGroupSubtype(element) {
  if (element !== undefined && element.$prototype !== undefined) {
    const prototype = element.$prototype;
    if (prototype.$key === undefined && prototype.$type !== "group") {
      return EditGroupSubtype.PRIMITIVE_LIST;
    }
    if (prototype.$key !== undefined && prototype.$type !== "group") {
      return EditGroupSubtype.PRIMITIVE_DICTIONARY;
    }
    if (prototype.$key === undefined && prototype.$type === "group") {
      return EditGroupSubtype.LIST_OF_DICTIONARIES;
    }
    if (prototype.$key !== undefined && prototype.$type === "group") {
      return EditGroupSubtype.DICTIONARY_OF_DICTIONARIES;
    }
  }
}

// Returns deep copy of object. In case of primitive values, just returns the
// value.
function deepCopy(e) {
  const type = typeof e;
  if (type === "object") {
    return jQuery.extend(true, {}, e);
  }
  return e;
}

function getProductName(): string {
  return window._IS_UYUNI ? "Uyuni" : "SUSE Manager";
}

const Utils = {
  cancelable: cancelable,
  sortById: sortById,
  sortByText: sortByText,
  sortByNumber: sortByNumber,
  sortByDate: sortByDate,
  urlBounce: urlBounce,
  capitalize: capitalize,
  generatePassword: generatePassword,
  deepCopy: deepCopy,
  getProductName: getProductName,
};

const Formats = {
  LocalDateTime: LocalDateTime,
};

const Formulas = {
  EditGroupSubtype: EditGroupSubtype,
  getEditGroupSubtype: getEditGroupSubtype,
};

export { Utils, Formats, Formulas };
