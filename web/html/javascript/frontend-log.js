"use strict";

Loggerhead.set({ url: '/rhn/manager/frontend-log' });

Loggerhead.setHeaders = function(headers) {
  headers['X-CSRF-Token'] = document.getElementsByName('csrf_token')[0].value;
  return headers;
}

// store a log message about the page has been loaded
window.addEventListener('load', function(event) {
  Loggerhead.info('[' + new Date().toUTCString() + '] - Loading `' + window.location + '`');
});
// store a log message about a page has been left
window.addEventListener('unload', function(event) {
  Loggerhead.info('[' + new Date().toUTCString() + '] - Leaving `' + window.location + '`');
});
// store a log message about the error that just happened
window.addEventListener('error', function(event) {
  // Note that col & error are new to the HTML 5 and may not be supported in every browser.
  var extra = !event.colno ? '' : '\ncolumn: ' + event.colno;
  extra += !event.error ? '' : '\nerror: ' + event.error;
  var errorMessage = event.message + '\nurl: ' + event.filename + '\nline: ' + event.lineno + extra;
  Loggerhead.error(errorMessage);
});
