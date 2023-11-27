import "../manager/polyfills";

import moment from "moment-timezone";

// eslint-disable-next-line local-rules/no-raw-date
const guess = moment.tz.guess(true);

// eslint-disable-next-line no-console
console.log(`Mocking timezone "${guess}"`);

window.preferredLocale = "en";
window.userTimeZone = guess;
window.serverTimeZone = guess;
// eslint-disable-next-line local-rules/no-raw-date
window.serverTime = new Date().toISOString();
