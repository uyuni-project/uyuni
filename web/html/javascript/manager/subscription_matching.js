'use strict';

var SubscriptionMatching = React.createClass({
   getInitialState: function() {
     return { "messages" : [] };
   },

  componentWillMount: function() {
    $.get("/rhn/manager/subscription_matching/data", data => {
      this.setState(data);
    });
  },

  render: function() {
    return (
      <div>
        <div className="spacewalk-toolbar-h1">
          <h1><i className="fa spacewalk-icon-subscription-counting"></i>{t("Subscription Matching")}</h1>
        </div>
        <div>
          <div className="row col-md-12">
            <p>{t("Please review warning and information messages from last matching below.")}</p>
            <Table headers={[t("Message"), t("Additional information")]} data={humanReadable(this.state.messages)} />
          </div>
        </div>
      </div>
    );
  }
});

function humanReadable(raw_messages){
  var result= raw_messages.map(function(raw_message){
    var data = raw_message["data"];
    var message;
    var additionalInformation;
    switch(raw_message["type"]) {
      case "unknown_part_number" :
        message = t("Unsupported part number detected");
        additionalInformation = data["part_number"];
        break;
      case "physical_guest" :
        message = t("Physical system is reported as virtual guest, please check hardware data");
        additionalInformation = data["name"];
        break;
      case "guest_with_unknown_host" :
        message = t("Virtual guest has unknown host, assuming it is a physical system");
        additionalInformation = data["name"];
        break;
      case "unknown_cpu_count" :
        message = t("System has an unknown number of sockets, assuming 16");
        additionalInformation = data["name"];
        break;
      case "unsatisfied_pinned_match" :
        message = t("Matcher was not able to satisfy a pinned subscription");
        additionalInformation = t("{0} to system {1}", data["subscription_name"], data["system_name"]);
        break;
      default:
        message = raw_message["type"];
        additionalInformation = data;
    }
    return [message, additionalInformation];
  });
  return result;
}

var Table = React.createClass({
  render: function() {
    return (
      <div className="panel panel-default">
        <table className="table table-striped">
          <TableHeader headers={this.props.headers} />
          {this.props.data.map(function(columns){
            return (<TableRow columns={columns}/>);
          })}
        </table>
      </div>
    );
  }
});

var TableHeader = React.createClass({
  render: function() {
    return (
      <thead>
        <tr>
          {this.props.headers.map(function(header){
            return (<th>{header}</th>);
          })}
        </tr>
      </thead>
    );
  }
});

var TableRow = React.createClass({
  render: function() {
    return (
      <tbody className="table-content">
        <tr>
          {this.props.columns.map(function(column){
            return (<td>{column}</td>);
          })}
        </tr>
      </tbody>
    );
  }
});

React.render(
  <SubscriptionMatching />,
  document.getElementById('subscription_matching')
);
