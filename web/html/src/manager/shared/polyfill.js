// Improvement for future: don't load this file on the most recent Chrome/Firefox versions
/* eslint-disable */
require('@babel/polyfill/dist/polyfill');
require('core-js/shim');
require('regenerator-runtime/runtime');



const moment = require('moment-timezone')
window.moment = moment;
