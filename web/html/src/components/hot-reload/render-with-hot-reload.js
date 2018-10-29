require('react-hot-loader/patch');
const { AppContainer } = require('react-hot-loader');
const React = require('react');
const ReactDOM = require('react-dom');

module.exports = {
  renderWithHotReload: function renderWithHotReload(Component, props, id) {
    const element = document.getElementById(id);
    if (element !== null) {
      ReactDOM.render(
        <AppContainer>
          <Component
            {...props}
          />
        </AppContainer>,
        element,
      );
    }
  },
};
