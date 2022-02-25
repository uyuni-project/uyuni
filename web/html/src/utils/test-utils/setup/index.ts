import "manager/polyfills";

import jQuery from "jquery";

import Loggerhead from "core/log/loggerhead";

import t from "./t";

// Allows us to mock and test the existing network layer easily
global.jQuery = jQuery;

global.t = t;

global.Loggerhead = new Loggerhead("", (headers) => headers);
