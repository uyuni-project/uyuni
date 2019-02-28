/* eslint-disable */
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
  var result = aRaw[columnKey].toLowerCase().localeCompare(bRaw[columnKey].toLowerCase());
  return (result || sortById(aRaw, bRaw)) * sortDirection;
}

function sortByNumber(aRaw: Object, bRaw: Object, columnKey: string, sortDirection: number): number {
    const result = aRaw[columnKey] > bRaw[columnKey] ? 1 : (aRaw[columnKey] < bRaw[columnKey] ? -1 : 0);
    return result * sortDirection;
}

function sortByDate(aRaw: Object, bRaw: Object, columnKey: string, sortDirection: number): number {
    const aDate = aRaw[columnKey] instanceof Date ? aRaw[columnKey] : new Date(aRaw[columnKey]);
    const bDate = bRaw[columnKey] instanceof Date ? bRaw[columnKey] : new Date(bRaw[columnKey]);

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
        return $.extend(true, {}, e);
    }
    return e;
}


module.exports = {
    Utils: {
        cancelable: cancelable,
        sortById: sortById,
        sortByText: sortByText,
        dateWithTimezone: dateWithTimezone,
        sortByNumber: sortByNumber,
        sortByDate: sortByDate,
        urlBounce: urlBounce,
        capitalize: capitalize,
        generatePassword: generatePassword,
        deepCopy: deepCopy
    },
    Formats: {
        LocalDateTime: LocalDateTime
    },
    Formulas: {
        EditGroupSubtype: EditGroupSubtype,
        getEditGroupSubtype: getEditGroupSubtype
    }
}
