"use strict";
const React = require("react");

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
        encodeURIComponent(key).replace(/[\.\+\*]/g, "\\$&") + "(?:\\=([^&]*))?)?.*$", "i"), "$1"));
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


module.exports = {
    Utils: {
        cancelable: cancelable,
        sortById: sortById,
        sortByText: sortByText,
        dateWithTimezone: dateWithTimezone,
        sortByNumber: sortByNumber,
        sortByDate: sortByDate,
        urlBounce: urlBounce,
        capitalize: capitalize
    },
    Formats: {
        LocalDateTime: LocalDateTime
    }
}
