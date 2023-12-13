// The proxy runs on this address by default
const proxyHost = "https://localhost:8080";
// eslint-disable-next-line local-rules/no-raw-date
const relativeToProxyUrl = (url) => `${proxyHost}${url}?cb${new Date().valueOf()}`;

// eslint-disable-next-line local-rules/no-raw-date
const noCache = new Date().valueOf();

module.exports = {
  head: {
    // Some of these will currently fail due to CORS, but will be obsolete once we move branding into web
    links: [
      `/css/susemanager-light.css?cb=${noCache}`,
      `/fonts/font-awesome/css/font-awesome.css?cb=${noCache}`,
      `/fonts/font-spacewalk/css/spacewalk-font.css?cb=${noCache}`,
      `/css/jquery.timepicker.css?cb=${noCache}`,
      `/css/bootstrap-datepicker.css?cb=${noCache}`,
    ]
      .map(relativeToProxyUrl)
      .map((url) => ({
        rel: "stylesheet",
        href: url,
      })),
    scripts: [
      // Ignore these for now
      // `/javascript/loggerhead.js`,
      // `/javascript/frontend-log.js`,
      `/javascript/legacy/jquery.min.js?cb=${noCache}`,
      `/javascript/legacy/bootstrap.min.js?cb=${noCache}`,
      `/javascript/spacewalk-essentials.js?cb=${noCache}`,
      `/javascript/spacewalk-checkall.js?cb=${noCache}`,
      `/javascript/legacy/jquery.timepicker.js?cb=${noCache}`,
      `/javascript/bootstrap-datepicker.js?cb=${noCache}`,
      `/javascript/momentjs/moment-with-langs.min.js?cb=${noCache}`,
    ]
      .map(relativeToProxyUrl)
      .map((url) => ({
        type: "text/javascript",
        src: url,
      })),
  },
};
