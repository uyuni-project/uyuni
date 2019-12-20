import { configure, addDecorator, addParameters } from '@storybook/react';
import { themes } from '@storybook/theming';
import { DocsPage, DocsContainer } from '@storybook/addon-docs/blocks';


addParameters({
  options: {
    theme: themes.dark,
    showPanel: true
  },
  docs: {
    container: DocsContainer,
    page: DocsPage,
  },
});

configure(require.context('../components', true, /\.stories\.js$/), module);

configure(loadStories, module);


