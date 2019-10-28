global.t = string => string;

global.Loggerhead = {
  error: (string) => {console.log(string)},
  warning: (string) => {console.log(string)},
  debug: (string) => {console.log(string)},
  info: (string) => {console.log(string)}
}
