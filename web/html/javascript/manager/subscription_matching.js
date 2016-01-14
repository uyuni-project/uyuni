'use strict';

var SubscriptionMatching = React.createClass({
   getInitialState: function() {
     return null;
   },

  componentWillMount: function() {
    $.get("/rhn/manager/subscription_matching/data", data => {
      this.setState(data);
    });
  },

  render: function() {
    if (this.state == null) {
      return <h1>{t("Loading...")}</h1>;
    }
    else {
      return <h1>{t("Matched on {0}", this.state.timestamp)}!</h1>;
    }
  }
});

React.render(
  <SubscriptionMatching />,
  document.getElementById('subscription_matching')
);
