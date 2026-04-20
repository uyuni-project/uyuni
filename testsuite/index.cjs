/**
 * https://github.com/gkushang/cucumber-html-reporter
 */

var reporter = require('cucumber-html-reporter');
var path = require('path');
var fs = require('fs');

// Read command-line arguments
var args = process.argv.slice(2);
var jsonDir = args[0] || '.'; // Default to current directory if no argument provided

var options = {
  theme: 'bootstrap',
  jsonDir: jsonDir,
  output: 'cucumber_report/cucumber_report.html',
  reportSuiteAsScenarios: true,
  launchReport: false,
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

var customScript = `
<script>
  setTimeout(() => {
    const descriptions = document.querySelectorAll('.description');
    const urlPatternTest = /(https?:\\/\\/[^\\s]+)/;
    const urlPattern = /(https?:\\/\\/[^\\s]+)/g;
    let count = 0;

    descriptions.forEach(el => {
      const originalText = el.textContent;
      if (urlPatternTest.test(originalText)) {
        const newHTML = originalText.replace(
          urlPattern,
          '<a href="$1" target="_blank" rel="noopener noreferrer" style="color: #007bff; text-decoration: underline; word-break: break-all;">$1</a>'
        );
        el.innerHTML = newHTML;
        count++;
      }
    });
  }, 500);
</script>
`;

try {
    var htmlFilePath = path.resolve(options.output);
    var htmlContent = fs.readFileSync(htmlFilePath, 'utf8');
    var updatedHtmlContent = htmlContent.replace(/<\/body\s*>/i, customScript + '\n</body>');

    if (updatedHtmlContent === htmlContent) {
        throw new Error('Could not find closing </body> tag in the generated report.');
    }

    fs.writeFileSync(htmlFilePath, updatedHtmlContent, 'utf8');
} catch (error) {
    console.error('Error injecting custom script into the report:', error);
}