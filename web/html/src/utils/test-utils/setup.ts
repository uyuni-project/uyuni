import "manager/polyfills";
import jQuery from "jquery";

// Allows us to mock and test the existing network layer easily
global.jQuery = jQuery;

global.t = string => string;

global.Loggerhead = {
  error: string => {
    console.log(string);
  },
  warning: string => {
    console.log(string);
  },
  debug: string => {
    console.log(string);
  },
  info: string => {
    console.log(string);
  },
};
