'use strict';

class PackageStates extends React.Component {
  render() {
    return (
      <div>
        <div className="spacewalk-toolbar-h1">
          <h1>
            <i className="fa spacewalk-icon-package-add"></i>
            Package States for {serverName}
          </h1>
        </div>
      </div>
    );
  }
}

React.render(
  <PackageStates />,
  document.getElementById('package-states')
);
