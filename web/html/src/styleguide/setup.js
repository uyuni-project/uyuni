import "../../../../branding/css/uyuni.less";

import moment from "moment-timezone";

// eslint-disable-next-line local-rules/no-raw-date
const guess = moment.tz.guess(true);

// eslint-disable-next-line no-console
console.log(`Mocking timezone "${guess}"`);

window.userTimeZone = guess;
window.serverTimeZone = guess;
// eslint-disable-next-line local-rules/no-raw-date
window.serverTime = new Date().toISOString();
