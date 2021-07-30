import { configure, addParameters } from '@storybook/react';
import { DocsPage, DocsContainer } from '@storybook/addon-docs/blocks';
import '../../../../branding/css/uyuni.less';


addParameters({
  options: {
    showPanel: true
  },
  docs: {
    container: DocsContainer,
    page: DocsPage,
  },
});

configure(require.context('../components', true, /\.stories\.(ts|js)x?$/), module);
