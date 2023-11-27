/**
 * Test env setup
 * For global variables such as time zones see web/html/src/jest.config.js
 */

import "manager/polyfills";

import jQuery from "jquery";

import { t } from "core/intl";
import Loggerhead from "core/log/loggerhead";

// Allows us to mock and test the existing network layer easily
global.jQuery = jQuery;

global.t = t;

const loggerHead = new Loggerhead("", (headers) => headers);

loggerHead.info = console.info.bind(console, "[Loggerhead] INFO:");
loggerHead.debug = console.debug.bind(console, "[Loggerhead] DEBUG:");
loggerHead.warn = console.warn.bind(console, "[Loggerhead] WARN:");
loggerHead.error = console.error.bind(console, "[Loggerhead] ERROR:");

global.Loggerhead = loggerHead;
