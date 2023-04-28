/**
 * Test env setup
 * For global variables such as time zones see web/html/src/jest.config.js
 */

import "manager/polyfills";

import jQuery from "jquery";

import Loggerhead from "core/log/loggerhead";

import t from "./t";

// Allows us to mock and test the existing network layer easily
global.jQuery = jQuery;

global.t = t;

const loggerHead = new Loggerhead("", (headers) => headers);

loggerHead.info = (message: string) => console.info(`[Loggerhead] INFO : ${message}`);
loggerHead.debug = (message: string) => console.debug(`[Loggerhead] DEBUG : ${message}`);
loggerHead.warn = (message: string) => console.warn(`[Loggerhead] WARN : ${message}`);
loggerHead.error = (message: string) => console.error(`[Loggerhead] ERROR : ${message}`);

global.Loggerhead = loggerHead;
