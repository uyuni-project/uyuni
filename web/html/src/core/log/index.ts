Loggerhead.set({ url: "/rhn/manager/frontend-log" });

Loggerhead.setHeaders = function (headers) {
  const csrf_token = document.getElementsByName("csrf_token") as NodeListOf<HTMLInputElement>;
  if (csrf_token?.[0]) {
    headers["X-CSRF-Token"] = csrf_token[0].value;
  }
  return headers;
};

// store a log message about the page has been loaded
window.addEventListener("load", () => {
  // eslint-disable-next-line local-rules/no-raw-date
  Loggerhead.info("[" + new Date().toUTCString() + "] - Loading `" + window.location + "`");
});
// store a log message about a page has been left
window.addEventListener("unload", () => {
  // eslint-disable-next-line local-rules/no-raw-date
  Loggerhead.info("[" + new Date().toUTCString() + "] - Leaving `" + window.location + "`");
});
// store a log message about the error that just happened
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

  Loggerhead.error(errorMessage);
});

export {};
