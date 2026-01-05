/**
 * https://github.com/gkushang/cucumber-html-reporter
 */

var reporter = require('cucumber-html-reporter');
var path = require('path');

// Read command-line arguments
var args = process.argv.slice(2);
var jsonDir = args[0] || '.'; // Default to current directory if no argument provided

var options = {
  theme: 'bootstrap',
  jsonDir: jsonDir,
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
