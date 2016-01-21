'use strict';

var SubscriptionMatching = React.createClass({
   getInitialState: function() {
     return {};
   },

  refreshServerData: function() {
   $.get("/rhn/manager/subscription_matching/data", data => {
     this.setState({"serverData" : data});
   });
 },

  componentWillMount: function() {
    this.refreshServerData();
    setInterval(this.refreshServerData, this.props.refreshInterval);
  },

  render: function() {
    var data = this.state.serverData;
    var latestStart = data == null ? null : data.latestStart;
    var latestEnd = data == null ? null : data.latestEnd;
    var messages = data == null ? null : data.messages;
    var subscriptions = data == null ? null : data.subscriptions;

    var body;
    if (data != null && data.matcherDataAvailable) {
      body = (
        <div>
          <Subscriptions subscriptions={subscriptions} />
          <Messages messages={messages} />
        </div>
      );
    }

    return (
      <div>
        <div className="spacewalk-toolbar-h1">
          <h1><i className="fa spacewalk-icon-subscription-counting"></i>{t("Subscription Matching")}</h1>
        </div>
        {body}
        <MatcherRunPanel dataAvailable={data != null} initialLatestStart={latestStart} initialLatestEnd={latestEnd} />
      </div>
    );
  }
});

var MatcherRunPanel = React.createClass({
  getInitialState: function() {
    return {
      "latestStart": this.props.initialLatestStart,
      "latestEnd": this.props.initialLatestEnd,
      "error": false,
    }
  },

  componentWillReceiveProps: function(nextProps) {
    if (this.state.latestStart == null || nextProps.initialLatestStart >= this.state.latestStart) {
      this.setState({
        "latestStart": nextProps.initialLatestStart,
        "latestEnd": nextProps.initialLatestEnd,
        "error": false,
      });
    }
  },

  onScheduled: function() {
    this.setState({
        "latestStart": new Date().toJSON(),
        "latestEnd": null,
        "error": false,
      }
    );
  },

  onError: function() {
    this.setState({"error" : true});
  },

  render: function() {
    if (!this.props.dataAvailable) {
      return null;
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Matcher status")}</h2>
        <MatcherRunDescription latestStart={this.state.latestStart} latestEnd={this.state.latestEnd} error={this.state.error} />
        <MatcherScheduleButton
          matcherRunning={!this.state.error && this.state.latestStart != null && this.state.latestEnd == null}
          onScheduled={this.onScheduled}
          onError={this.onError}
        />
      </div>
    );
  }
});

var MatcherRunDescription  = React.createClass({
  render: function() {
    if (this.props.error) {
      return <p className="text-danger">{t("Could not start a matching run. Please contact your SUSE Manager administrator to make sure the task scheduler is running.")}</p>
    }

    if (this.props.latestStart == null) {
      return <p>{t("The matcher has not run yet. You can trigger a first run by clicking the button below.")}</p>
    }

    if (this.props.latestEnd == null) {
      return <p>{t("Matcher is currently running, it was started on {0}.", this.props.latestStart)}</p>
    }

    return <p>{t("Latest successful matcher run was on {0}, you can trigger a new run by clicking the button below.", this.props.latestEnd)}</p>
  }
});

var MatcherScheduleButton = React.createClass({
  onClick: function() {
    $.post("/rhn/manager/subscription_matching/schedule_matcher_run")
      .error(() => { this.props.onError(); });
    this.props.onScheduled();
  },

  render: function() {
    var buttonClass = "btn spacewalk-btn-margin-vertical " +
      (!this.props.matcherRunning ? "btn-success" : "btn-default");

    return (
      <button
        type="button"
        key="matcherScheduleButton"
        className={buttonClass}
        disabled={this.props.matcherRunning}
        onClick={this.onClick}
      >
        <i className="fa fa-refresh"></i>{t("Run the matcher")}
      </button>
    );
  }
});

var Messages = React.createClass({
  render: function() {
    var body;
    if (this.props.messages != null) {
      if (this.props.messages.length > 0) {
        body = (
          <div className="spacewalk-list">
            <p>{t("Please review warning and information messages from last matching below.")}</p>
            <Table headers={[t("Message"), t("Additional information")]} data={humanReadable(this.props.messages)} itemsPerPage={10} />
            <CsvLink name="message_report.csv" />
          </div>
        );
      }
      else {
        body = <p>{t("No matcher messages.")}</p>
      }
    }
    else {
      body = <p>{t("Loading matcher data...")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Matching messages")}</h2>
        {body}
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

var Subscriptions = React.createClass({
  render: function() {
    var body;
    if (this.props.subscriptions != null) {
      if (this.props.subscriptions.length > 0) {
        body = (
          <div className="spacewalk-list">
            <Table headers={[t("Part number"), t("Description"), t("Total quantity"), t("Matched quantity"), t("Start date"), t("End date")]}
              data={subscriptionsToRows(this.props.subscriptions)}
              itemsPerPage={10} />
            <CsvLink name="subscription_report.csv" />
          </div>
        );
      }
      else {
        body = <p>{t("No subscriptions found.")}</p>
      }
    }
    else {
      body = <p>{t("Loading matcher data...")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Your subscriptions")}</h2>
        {body}
      </div>
    );
  }
});

function subscriptionsToRows(subscriptions){
  return subscriptions.map((s) => [s.partNumber, s.description, s.totalQuantity, s.matchedQuantity, s.startDate, s.endDate]);
}

var Table = React.createClass({
  getInitialState: function(){
    return { "currentPage":1 };
  },

  componentWillReceiveProps: function(nextProps) {
    var lastPage = this.lastPage(nextProps.itemsPerPage, nextProps.data.length);
    if (this.state.currentPage > lastPage) {
      this.setState({"currentPage": lastPage});
    }
  },

  goToPage:function(p){
    this.setState({"currentPage": p});
  },

  lastPage: function(ipp, totItems) {
    return totItems <= ipp ? 1 : (totItems%ipp == 0 ? totItems/ipp : Math.floor(totItems/ipp) +1);
  },

  render: function() {
    var ipp = this.props.itemsPerPage;
    var totItems = this.props.data.length;
    var currentPage = this.state.currentPage;
    var lastPage = this.lastPage(ipp, totItems);
    var offset = (currentPage-1)*ipp;
    var firstItem = offset +1;
    var lastItem = firstItem + ipp < totItems ? firstItem + ipp -1 : totItems;
    var paginatedItems = [];
    for(var i = 0; i<ipp; i++){
      if ((offset+i) < totItems) {
          paginatedItems[i] = this.props.data[offset + i];
      }
    }
    return (
      <div className="panel panel-default">
        <div className="panel-heading">
          <div className="spacewalk-list-head-addons">
            <div className="spacewalk-list-filter">{firstItem} - {lastItem} of {totItems} {t("items")}</div>
            <div className="spacewalk-list-head-addons-extra">{ipp} {t("items per page")}</div>
          </div>
        </div>
        <div className="table-responsive">
          <table className="table table-striped">
            <TableHeader headers={this.props.headers} />
            {paginatedItems.map(function(columns){
              return (<TableRow columns={columns}/>);
            })}
          </table>
        </div>
        <div className="panel-footer">
          <div className="spacewalk-list-bottom-addons">
            {this.state.currentPage} of {lastPage} {t("pages")}
            <div className="spacewalk-list-pagination">
              <div className="spacewalk-list-pagination-btns btn-group">
                <ButtonPage onClick={this.goToPage} toPage={1} disabled={this.state.currentPage == 1} text={t("FirstPage")} />
                <ButtonPage onClick={this.goToPage} toPage={this.state.currentPage -1} disabled={this.state.currentPage == 1} text={t("PrevPage")} />
                <ButtonPage onClick={this.goToPage} toPage={this.state.currentPage + 1} disabled={this.state.currentPage == lastPage} text={t("NextPage")} />
                <ButtonPage onClick={this.goToPage} toPage={lastPage} disabled={this.state.currentPage == lastPage} text={t("LastPage")} />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
});

var ButtonPage = React.createClass({
  onClick: function(){
    this.props.onClick(this.props.toPage);
  },

  render: function(){
    return (
      <button type="button" className="btn btn-default"
        disabled={this.props.disabled} onClick={this.onClick}>
        {this.props.text}
      </button>
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

var CsvLink = React.createClass({
  render: function() {
    return (
      <div className="spacewalk-csv-download">
        <a className="btn btn-link" href={"/rhn/manager/subscription_matching/" + this.props.name}>
          <i className="fa spacewalk-icon-download-csv"></i>
          {t("Download CSV")}
        </a>
      </div>
    );
  }
});

React.render(
  <SubscriptionMatching refreshInterval={5000} />,
  document.getElementById('subscription_matching')
);
