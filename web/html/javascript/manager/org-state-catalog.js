'use strict';

var StateCatalog = React.createClass({

    getInitialState: function() {
        return {"serverData": []};
    },

    refreshServerData: function() {
        $.get("/rhn/manager/state_catalog/data", data => {
          this.setState({"serverData" : data});
        });
    },

    componentWillMount: function() {
        this.refreshServerData();
    },

    render: function() {
        return (<ul>
           { this.state.serverData.map( (e) => {
                return <li>{e}</li>
            })
           }
        </ul>);
    }

});

// hello
React.render(
  <StateCatalog />,
  document.getElementById('state-catalog')
);