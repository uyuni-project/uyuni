/**
 * https://github.com/gkushang/cucumber-html-reporter
 */

var reporter = require('cucumber-html-reporter');

var options = {
  theme: 'bootstrap',
  jsonDir: '.',
  output: './cucumber_report/cucumber_report.html',
  reportSuiteAsScenarios: true,
  launchReport: false,
  columnLayout: 1,
  screenshotsDirectory: './cucumber_report/screenshots/',
  storeScreenshots: true,
  noInlineScreenshots: true,
  ignoreBadJsonFile: true,
  name: 'SUSE Manager Testsuite',
  brandTitle: ' ',
  // metadata: {
  //   "App Version":"",
  //   "Test Environment": "",
  //   "Browser": "",
  //   "Platform": "",
  //   "Parallel": "",
  //   "Executed": ""
  // }
};

reporter.generate(options);
