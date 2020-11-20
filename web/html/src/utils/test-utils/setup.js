const jQuery = require("jQuery");

/*
const consoleError = console.error;
console.error = function error(...args) {
  // console.log('got args >', ...args, '<');
  // Throw on any logged errors, even if the error is swallowed in the stack
  consoleError.apply(console, args);
  const firstArg = args && args[0];
  throw (firstArg instanceof Error ? firstArg : new Error(...args));
}
*/

// Allows us to mock and test the existing network layer easily
global.jQuery = jQuery;

global.t = string => string;

global.Loggerhead = {
  error: (string) => {console.log(string)},
  warning: (string) => {console.log(string)},
  debug: (string) => {console.log(string)},
  info: (string) => {console.log(string)}
}
