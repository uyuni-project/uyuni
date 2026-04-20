/**
 * https://github.com/gkushang/cucumber-html-reporter
 */

const reporter = require('cucumber-html-reporter');
const path = require('path');
const fs = require('fs');
const args = process.argv.slice(2);
const jsonDir = args[0] || '.'; // Default to current directory if no argument provided

const options = {
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

const customScript = `
<script>
  document.addEventListener('DOMContentLoaded', () => {
    const urlPatternTest = /(https?:\\/\\/[^\\s<]+[^<.,:;"')\\]\\s])/;
    const urlPattern = /(https?:\\/\\/[^\\s<]+[^<.,:;"')\\]\\s])/g;

    const linkifyDescriptions = () => {
      const descriptions = document.querySelectorAll('.description:not([data-linkified="true"])');
      let updateCount = 0;
      
      descriptions.forEach(el => {
        el.setAttribute('data-linkified', 'true');
        
        const originalText = el.textContent;
        if (urlPatternTest.test(originalText)) {
          const newHTML = originalText.replace(
            urlPattern,
            '<a href="$1" target="_blank" rel="noopener noreferrer" style="color: #007bff; text-decoration: underline; word-break: break-all;">$1</a>'
          );
          el.innerHTML = newHTML;
          updateCount++;
        }
      });

      if (updateCount > 0) {
        console.log(\`Cucumber Report: Linkified \${updateCount} description(s).\`);
      }
    };

    linkifyDescriptions();

    const observer = new MutationObserver((mutations) => {
      let shouldCheck = false;
      for (const mutation of mutations) {
        if (mutation.addedNodes.length > 0) {
          shouldCheck = true;
          break; 
        }
      }
      
      if (shouldCheck) {
        linkifyDescriptions();
      }
    });

    observer.observe(document.body, { childList: true, subtree: true });
  });
</script>
`;

async function generateAndInject() {
  try {
    await reporter.generate(options);

    const htmlFilePath = path.resolve(options.output);
    
    if (!fs.existsSync(htmlFilePath)) {
      console.warn('File not immediately found. Yielding to file system...');
      await new Promise(resolve => setTimeout(resolve, 1500));
    }

    const htmlContent = fs.readFileSync(htmlFilePath, 'utf8');
    const updatedHtmlContent = htmlContent.replace(/<\/body\s*>/i, customScript + '\n</body>');

    if (updatedHtmlContent === htmlContent) {
      throw new Error('Could not find closing </body> tag in the generated report.');
    }

    fs.writeFileSync(htmlFilePath, updatedHtmlContent, 'utf8');
    console.log('Report generated and custom URL script injected successfully.');
    
  } catch (error) {
    console.error('Error during report generation or script injection:', error);
  }
}

generateAndInject();
