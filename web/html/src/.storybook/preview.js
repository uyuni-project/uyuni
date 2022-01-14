import moment from "moment-timezone";

import "../../../../branding/css/uyuni.less";

const guess = moment.tz.guess(true);

console.log(`Mocking timezone "${guess}" for Storybook`);
window.userTimeZone = guess;
window.serverTimeZone = guess;
window.serverTime = new Date().toISOString();
