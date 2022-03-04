// The proxy runs on this address by default
const proxyHost = "https://localhost:8080";
// eslint-disable-next-line local-rules/no-raw-date
const relativeToProxyUrl = (url) => `${proxyHost}${url}?cb${new Date().valueOf()}`;

module.exports = {
  head: {
    // Some of these will currently fail due to CORS, but will be obsolete once we move branding into web
    links: [
      "/fonts/font-awesome/css/font-awesome.css",
      "/fonts/font-spacewalk/css/spacewalk-font.css",
      "/css/jquery.timepicker.css",
      "/css/bootstrap-datepicker.css",
      "/javascript/select2/select2.css",
      "/javascript/select2/select2-bootstrap.css",
    ]
      .map(relativeToProxyUrl)
      .map((url) => ({
        rel: "stylesheet",
        href: url,
      })),
    scripts: [
      // Ignore these for now
      // "/javascript/loggerhead.js",
      // "/javascript/frontend-log.js",
      "/javascript/jquery.js",
      "/javascript/bootstrap.js",
      "/javascript/select2/select2.js",
      "/javascript/spacewalk-essentials.js",
      "/javascript/spacewalk-checkall.js",
      "/rhn/dwr/engine.js",
      "/rhn/dwr/util.js",
      "/rhn/dwr/interface/DWRItemSelector.js",
      "/javascript/jquery.timepicker.js",
      "/javascript/bootstrap-datepicker.js",
      "/javascript/momentjs/moment-with-langs.min.js",
    ]
      .map(relativeToProxyUrl)
      .map((url) => ({
        type: "text/javascript",
        src: url,
      })),
  },
};
