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
    var unmatchedSystems = data == null ? null : data.unmatchedSystems;

    var body;
    if (data != null && data.matcherDataAvailable) {
      body = (
        <div>
          <Subscriptions subscriptions={subscriptions} />
          <UnmatchedSystems unmatchedSystems={unmatchedSystems} />
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
        <h2>{t("Match data status")}</h2>
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

var MatcherRunDescription = React.createClass({
  render: function() {
    if (this.props.error) {
      return <p className="text-danger">{t("Could not start a matching run. Please contact your SUSE Manager administrator to make sure the task scheduler is running.")}</p>
    }

    if (this.props.latestStart == null) {
      return (<p>
        {t("No match data is currently available.")}<br/>
        <MatcherTaskDescription />
        {t("You can also trigger a first run now by clicking the button below.")}
      </p>);
    }

    if (this.props.latestEnd == null) {
      return (<p>
        <MatcherTaskDescription />
        {t("Matching data is currently being recomputed, it was started {0}.", moment(this.props.latestStart).fromNow())}
      </p>);
    }

    return (<p>
      <MatcherTaskDescription />
      {t("Latest successful match data was computed on {0}, you can trigger a new run by clicking the button below.", moment(this.props.latestEnd).fromNow())}
    </p>);
  }
});

var MatcherTaskDescription = React.createClass({
  render: function() {
    return (<div>
      {t("Match data is computed via a task schedule, nightly by default (you can ")}
      <a href="/rhn/admin/BunchDetail.do?label=gatherer-matcher-bunch">{t("change the task schedule from the administration page")}</a>
      {t("). ")}
    </div>);
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
        <i className="fa fa-refresh"></i>{t("Refresh matching data")}
      </button>
    );
  }
});

var UnmatchedSystems = React.createClass({
  render: function() {
    if (this.props.unmatchedSystems != null && this.props.unmatchedSystems.length > 0) {
      return (
        <div className="row col-md-12">
          <h2>{t("Unmatched Systems")}</h2>
          <div className="spacewalk-list">
            <Table headers={[t("Name"), t("Socket/IFL count"), t("Products")]}
              rows={unmatchedSystemsToRows(this.props.unmatchedSystems)}
              itemsPerPage={15} />
            <CsvLink name="unmatched_system_report.csv" />
          </div>
        </div>
      );
    }
    return null;
  }
});

function unmatchedSystemsToRows(systems){
  return systems.map((s) => {
    var columns = [
      s.name,
      s.cpuCount,
      s.products.reduce((a,b) => a+", "+b),
    ];
    return <TableRow columns={columns} />
  });
}

var Messages = React.createClass({
  render: function() {
    var body;
    if (this.props.messages != null) {
      if (this.props.messages.length > 0) {
        body = (
          <div className="spacewalk-list">
            <p>{t("Please review warning and information messages below.")}</p>
            <Table headers={[t("Message"), t("Additional information")]} rows={messagesToRows(this.props.messages)} itemsPerPage={5} />
            <CsvLink name="message_report.csv" />
          </div>
        );
      }
      else {
        body = <p>{t("No messages from the last match run.")}</p>
      }
    }
    else {
      body = <p>{t("Loading...")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Messages")}</h2>
        {body}
      </div>
    );
  }
});

function messagesToRows(raw_messages){
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
    return <TableRow columns={[message, additionalInformation]} />;
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
            <Table headers={[t("Part number"), t("Description"), t("Policy"), t("Matched/Total"), t("Start date"), t("End date")]}
              rows={subscriptionsToRows(this.props.subscriptions)}
              itemsPerPage={15} />
            <CsvLink name="subscription_report.csv" />
          </div>
        );
      }
      else {
        body = <p>{t("No subscriptions found.")}</p>
      }
    }
    else {
      body = <p>{t("Loading...")}</p>
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
  return subscriptions.map((s) => {
    var now = moment();
    var className = moment(s.endDate).isBefore(now) ?
      "text-muted" :
        moment(s.endDate).isBefore(now.add(3, "months")) ?
        "text-danger" :
        null;

    var columns = [
      s.partNumber,
      s.description,
      humanReadablePolicy(s.policy),
      s.matchedQuantity + "/" + s.totalQuantity,
      moment(s.startDate).fromNow(),
      moment(s.endDate).fromNow(),
    ];

    return <TableRow className={className} columns={columns} />
  });
}

function humanReadablePolicy(raw_policy){
  var message;
  switch(raw_policy) {
    case "physical_only" :
      message = t("Physical deployment only");
      break;
    case "unlimited_virtualization" :
      message = t("Unlimited Virtual Machines");
      break;
    case "one_two" :
      message = t("1-2 Sockets or 1-2 Virtual Machines");
      break;
    case "instance" :
      message = t("Per-instance");
      break;
    default:
      message = raw_policy;
  }
  return message;
}

var Table = React.createClass({
  getInitialState: function(){
    return { "currentPage": 1 };
  },

  componentWillReceiveProps: function(nextProps) {
    var lastPage = Math.ceil(nextProps.rows.length / nextProps.itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({"currentPage": lastPage});
    }
  },

  goToPage:function(page){
    this.setState({"currentPage": page});
  },

  render: function() {
    var itemsPerPage = this.props.itemsPerPage;
    var itemCount = this.props.rows.length;
    var lastPage = Math.ceil(itemCount / itemsPerPage);
    var currentPage = this.state.currentPage;

    var firstItemIndex = (currentPage - 1) * itemsPerPage;

    var fromItem = firstItemIndex +1;
    var toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;

    var pagination;
    if (lastPage > 1) {
      pagination = (
        <div className="spacewalk-list-pagination">
          <div className="spacewalk-list-pagination-btns btn-group">
            <PaginationButton onClick={this.goToPage} toPage={1} disabled={currentPage == 1} text={t("First")} />
            <PaginationButton onClick={this.goToPage} toPage={currentPage -1} disabled={currentPage == 1} text={t("Prev")} />
            <PaginationButton onClick={this.goToPage} toPage={currentPage + 1} disabled={currentPage == lastPage} text={t("Next")} />
            <PaginationButton onClick={this.goToPage} toPage={lastPage} disabled={currentPage == lastPage} text={t("Last")} />
          </div>
        </div>
      );
    }

    return (
      <div className="panel panel-default">
        <div className="panel-heading">
          <div className="spacewalk-list-head-addons">
            <div className="spacewalk-list-filter">{t("Items {0} - {1} of {2}", fromItem, toItem, itemCount)}</div>
            <div className="spacewalk-list-head-addons-extra">{itemsPerPage} {t("items per page")}</div>
          </div>
        </div>
        <div className="table-responsive">
          <table className="table table-striped">
            <TableHeader headers={this.props.headers} />
            <tbody className="table-content">
              {this.props.rows
                .filter((element, i) => i >= firstItemIndex && i < firstItemIndex + itemsPerPage)
              }
              </tbody>
          </table>
        </div>
        <div className="panel-footer">
          <div className="spacewalk-list-bottom-addons">
            <div className="table-page-information">{t("Page {0} of {1}", currentPage, lastPage)}</div>
            {pagination}
          </div>
        </div>
      </div>
    );
  }
});

var PaginationButton = React.createClass({
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
      <tr className={this.props.className}>
        {this.props.columns.map(function(column){
          return (<td>{column}</td>);
        })}
      </tr>
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
