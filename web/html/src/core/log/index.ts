// This import is renamed to avoid a conflict with the global name
import LoggerheadInstance from "./loggerhead";

const setHeaders = function (headers) {
  const csrf_token = document.getElementsByName("csrf_token") as NodeListOf<HTMLInputElement>;
  if (csrf_token?.[0]) {
    headers["X-CSRF-Token"] = csrf_token[0].value;
  }
  return headers;
};

const loggerhead = new LoggerheadInstance("/rhn/manager/frontend-log", setHeaders);

// Store a log message about visiting and leaving pages
window.addEventListener("load", () => {
  loggerhead.info("Loading `" + window.location + "`");
});
window.addEventListener("unload", () => {
  loggerhead.info("Leaving `" + window.location + "`");
});
// Store a log message for uncaught errors
window.addEventListener("error", (event) => {
  // Note that col & error are new to the HTML 5 and may not be supported in every browser.
  const extra = `${!event.colno ? "" : "\ncolumn: " + event.colno}${!event.error ? "" : "\nerror: " + event.error}`;
  const errorMessage = event.message + "\nurl: " + event.filename + "\nline: " + event.lineno + extra;

  const ignoredErrors = [
    // This is thrown due to react-virtuoso, but this should actually be a warning not an error, see https://github.com/DevExpress/testcafe/issues/4857#issuecomment-598775956
    "ResizeObserver loop limit exceeded",
  ];
  if (ignoredErrors.some((message) => errorMessage.startsWith(message))) {
    return;
  }

  loggerhead.error(errorMessage);
});

declare global {
  var Loggerhead: LoggerheadInstance;

  namespace NodeJS {
    interface Global {
      Loggerhead: LoggerheadInstance;
    }
  }
}

window.Loggerhead = loggerhead;
