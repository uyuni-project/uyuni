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
  name: 'Uyuni/Head Testsuite',
  brandTitle: ' ',
  metadata: {
    "App Version":"Uyuni/Head",
    "Browser": "Chrome",
    "Platform": "x86_64",
    "Parallel": "Disabled"
  }
};

reporter.generate(options);
