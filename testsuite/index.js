/**
 * https://github.com/gkushang/cucumber-html-reporter
 */

var reporter = require('cucumber-html-reporter');

var options = {
  theme: 'bootstrap',
  jsonDir: '.',
  output: 'cucumber_report/cucumber_report.html',
  reportSuiteAsScenarios: true,
  launchReport: true,
  columnLayout: 1,
  scenarioTimestamp: true,
  screenshotsDirectory: './cucumber_report/screenshots/',
  storeScreenshots: true,
  noInlineScreenshots: true,
  ignoreBadJsonFile: true,
  name: 'SUSE Manager 4.2 Testsuite',
  brandTitle: ' ',
  metadata: {
    "App Version":"4.2",
    "Browser": "Chrome",
    "Platform": "x86_64",
    "Parallel": "Disabled"
  }
};

reporter.generate(options);
