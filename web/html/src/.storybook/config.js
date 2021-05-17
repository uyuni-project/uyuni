import { configure, addParameters } from "@storybook/react";
import { DocsPage, DocsContainer } from "@storybook/addon-docs/blocks";
import "../../../../branding/css/uyuni.less";

addParameters({
  options: {
    showPanel: true,
  },
  docs: {
    container: DocsContainer,
    page: DocsPage,
  },
});

const req = require.context("../components", true, /\.stories\.js$/);

function loadStories() {
  req.keys().forEach(req);
}

configure(loadStories, module);
