"use strict";

const React = require("react")

const mappingComparator = (comparator, map) => (a, b) => comparator(map(a), map(b));
const mappingFilter = (filter, map) => (cell, filterValue) => filter(map(cell), filterValue);

const stringLocaleComparator = (a, b) => a.localeCompare(b);
const numberComparator = (a, b) => a > b ? 1 : a === b ? 0 : -1;

const lengthComparator = mappingComparator(numberComparator, (x) => x.length);

const stringSubstringFilter = (cell, filterValue) => cell.indexOf(filterValue) > -1;

const cancelable = (promise, onCancel) => {
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
        cancel: (reason) => {
            isCanceled = true;
            rejectFn(reason);
        }
    };
}

function LocalDateTime(date) {
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

module.exports = {
    Comparators : {
        mapping: mappingComparator,
        locale: stringLocaleComparator,
        number: numberComparator,
        length: lengthComparator
    },
    Filters: {
        mapping: mappingFilter,
        substring: stringSubstringFilter
    },
    Utils: {
        cancelable: cancelable
    },
    Formats: {
        LocalDateTime: LocalDateTime
    }
}
