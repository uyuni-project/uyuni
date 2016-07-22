"use strict";

const React = require("react")

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

function sortById(aRaw, bRaw) {
  const aId = aRaw["id"];
  const bId = bRaw["id"];
  return aId > bId ? 1 : (aId < bId ? -1 : 0);
}

function sortByText(aRaw, bRaw, columnKey, sortDirection) {
  var result = aRaw[columnKey].toLowerCase().localeCompare(bRaw[columnKey].toLowerCase());
  return (result || sortById(aRaw, bRaw)) * sortDirection;
}

module.exports = {
    Utils: {
        cancelable: cancelable,
        sortById: sortById,
        sortByText: sortByText
    },
    Formats: {
        LocalDateTime: LocalDateTime
    }
}
