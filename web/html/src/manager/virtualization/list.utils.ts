import { Utils } from "utils/functions";

function sortByUpdate(aRaw: any, bRaw: any, columnKey: string, sortDirection: number) {
  const statusValues = {
    critical: 0,
    updates: 1,
    "actions scheduled": 2,
    "updates scheduled": 3,
    up2date: 4,
    kickstarting: 5,
    awol: 6,
    unentitled: 7,
  };
  const a = statusValues[aRaw[columnKey]];
  const b = statusValues[bRaw[columnKey]];
  return (Math.sign(a - b) || Utils.sortById(aRaw, bRaw)) * sortDirection;
}

function sortByState(aRaw: any, bRaw: any, columnKey: string, sortDirection: number) {
  const stateValues = {
    running: 0,
    stopped: 1,
    crashed: 2,
    paused: 3,
    unknown: 4,
  };
  const a = stateValues[aRaw[columnKey]];
  const b = stateValues[bRaw[columnKey]];
  return (Math.sign(a - b) || Utils.sortById(aRaw, bRaw)) * sortDirection;
}

const ListUtils = {
  sortByUpdate,
  sortByState,
};

export { ListUtils as Utils };
