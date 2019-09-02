import { configure, addDecorator, addParameters } from '@storybook/react';
import centered from '@storybook/addon-centered/react';
import { withInfo } from '@storybook/addon-info';
import { themes } from '@storybook/theming';

addDecorator(withInfo({
  inline: true,
  header: false
}));
// addDecorator(centered);


addParameters({
  options: {
    theme: themes.dark,
    showPanel: false
  },
});

const req = require.context('../components', true, /\.stories\.js$/);

function loadStories() {
  req.keys().forEach(filename => req(filename));
}

configure(loadStories, module);


