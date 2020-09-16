/* eslint-disable */
// @flow
export type Cancelable = {
  promise: Promise<any>,
  cancel: (any) => void
};

function cancelable(promise: Promise<any>, onCancel: (Error|void) => void): Cancelable {
    var rejectFn;
    var isCanceled = false;

    const cancelPromise = new Promise((resolve, reject) => {
        rejectFn = reject;
    });

    const race = Promise.race([promise, cancelPromise]).catch(error => {
        if(isCanceled) {
            onCancel(error);
        }
        throw error;
    });

    return {
        promise: race,
        cancel: (reason: any) => {
            isCanceled = true;
            rejectFn(reason);
        }
    };
}

function dateWithTimezone(dateString: string): Date {
    const offsetNum = dateString[dateString.length - 1].toUpperCase() === "Z"
      ? 0
      : parseInt(dateString.substring(dateString.length - 6).replace(':', ''), 10);
    const serverOffset = Math.trunc(offsetNum / 100) * 60 + offsetNum % 100;
    const orig = new Date(dateString);
    const clientOffset = -orig.getTimezoneOffset();

    const final = new Date(orig.getTime() + (serverOffset - clientOffset) * 60000);
    return final;
}

// it does the opposite of dateWithTimezone: transforms its result on the original date
function dateWithoutTimezone(dateStringToTransform: string, originalDateString: string): Date {
  const offsetNum = originalDateString[originalDateString.length - 1].toUpperCase() === "Z"
    ? 0
    : parseInt(originalDateString.substring(originalDateString.length - 6).replace(':', ''), 10);
  const serverOffset = Math.trunc(offsetNum / 100) * 60 + offsetNum % 100;
  const dateToTransform = new Date(dateStringToTransform);
  const clientOffset = -dateToTransform.getTimezoneOffset();

  const final = new Date(dateToTransform.getTime() - (serverOffset - clientOffset) * 60000);
  return final;
}

function LocalDateTime(date: Date): string {
    const padTo = (v) => {
        v = v.toString();
        if(v.length >= 2) return v;
        else return padTo("0" + v);
    }
    const year = date.getFullYear();
    const month = date.getMonth();
    const days = date.getDate();
    const hours = date.getHours();
    const minutes = date.getMinutes();
    const seconds = date.getSeconds();
    return "" + year + "-" + padTo(month + 1) + "-" + padTo(days) +
           "T" + padTo(hours) + ":" + padTo(minutes) + ":" + padTo(seconds);
}

function sortById(aRaw: Object, bRaw: Object): number {
  const aId = aRaw["id"];
  const bId = bRaw["id"];
  return aId > bId ? 1 : (aId < bId ? -1 : 0);
}

function sortByText(aRaw: Object, bRaw: Object, columnKey: string, sortDirection: number): number {
  var a = aRaw[columnKey];
  var b = bRaw[columnKey];
  var result = (a == null ? "" : a).toLowerCase().localeCompare((b == null ? "" : b).toLowerCase());
  return (result || sortById(aRaw, bRaw)) * sortDirection;
}

function sortByNumber(aRaw: Object, bRaw: Object, columnKey: string, sortDirection: number): number {
    const result = aRaw[columnKey] > bRaw[columnKey] ? 1 : (aRaw[columnKey] < bRaw[columnKey] ? -1 : 0);
    return result * sortDirection;
}

function sortByDate(aRaw: Object, bRaw: Object, columnKey: string, sortDirection: number): number {
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

    const aDate = aRaw[columnKey] instanceof Date ? aRaw[columnKey] : new Date(aRaw[columnKey].replace(unparsableDateRegex, "$1"));
    const bDate = bRaw[columnKey] instanceof Date ? bRaw[columnKey] : new Date(bRaw[columnKey].replace(unparsableDateRegex, "$1"));

    const result = aDate > bDate ? 1 : (aDate < bDate ? -1 : 0);
    return result * sortDirection;
}

function getQueryStringValue(key: string): string {
  // See for a standard implementation:
  // https://developer.mozilla.org/en-US/docs/Web/API/URLSearchParams
  return decodeURIComponent(window.location.search.replace(new RegExp("^(?:.*[&\\?]" +
        encodeURIComponent(key).replace(/[.+*]/g, "\\$&") + "(?:\\=([^&]*))?)?.*$", "i"), "$1"));
}

function urlBounce(defaultUrl: string, qstrParamKey?: string): void {
    window.location = getQueryStringValue(qstrParamKey || "url_bounce") || defaultUrl;
}

/**
 * Replace all "_" and "-" with spaces and capitalize the first letter of each word
 */
function capitalize(str: string): string {
    return str.replace(new RegExp("_|-", 'g'), " ").replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
}

function generatePassword(): string {
    const length = Math.floor(Math.random() * 10) + 15;
    const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-_";
    let password = "";
    if(window.crypto && window.crypto.getRandomValues) {
        var rand = new Uint16Array(length);
        window.crypto.getRandomValues(rand);
        for (let i = 0; i < length; i++)
            password += charset.charAt(Math.floor(rand[i] * charset.length / 65536));
    }
    else {
        for (let i = 0; i < length; i++)
            password += charset.charAt(Math.floor(Math.random() * charset.length));
    }
    return password;
}

// todo: shouldn't we stick to JS terminology here? (array/object vs. list/dict?)
const EditGroupSubtype = Object.freeze({
    PRIMITIVE_LIST: Symbol("primitiveList"),
    PRIMITIVE_DICTIONARY: Symbol("primitiveDictionary"),
    LIST_OF_DICTIONARIES: Symbol("listOfDictionaries"),
    DICTIONARY_OF_DICTIONARIES: Symbol("dictionaryOfDictionaries")
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

function getProductName() : string {
    return window._IS_UYUNI ? "Uyuni" : "SUSE Manager"
}

module.exports = {
    Utils: {
        cancelable: cancelable,
        sortById: sortById,
        sortByText: sortByText,
        dateWithTimezone: dateWithTimezone,
        dateWithoutTimezone: dateWithoutTimezone,
        sortByNumber: sortByNumber,
        sortByDate: sortByDate,
        urlBounce: urlBounce,
        capitalize: capitalize,
        generatePassword: generatePassword,
        deepCopy: deepCopy,
        getProductName: getProductName
    },
    Formats: {
        LocalDateTime: LocalDateTime
    },
    Formulas: {
        EditGroupSubtype: EditGroupSubtype,
        getEditGroupSubtype: getEditGroupSubtype
    }
}
