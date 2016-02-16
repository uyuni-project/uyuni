'use strict';

var React = require("react");
var TableComponent = require("../components/table");
var TabContainerComponent = require("../components/tabContainer");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var TabContainer = TabContainerComponent.TabContainer;

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

    var tabContainer = data == null || !data.matcherDataAvailable ? null :
      <TabContainer
        labels={[t("Subscriptions"), t("Unmatched Systems"), t("Messages")]}
        panels={[
          <Subscriptions
            subscriptions={subscriptions}
            saveState={(state) => {this.state["subscriptionTableState"] = state;}}
            loadState={() => this.state["subscriptionTableState"]}
          />,
          <UnmatchedSystems
            unmatchedSystems={unmatchedSystems}
            saveState={(state) => {this.state["unmatchedSystemTableState"] = state;}}
            loadState={() => this.state["unmatchedSystemTableState"]}
          />,
          <Messages
            messages={messages}
            saveState={(state) => {this.state["messageTableState"] = state;}}
            loadState={() => this.state["messageTableState"]}
          />
        ]}
      />
    ;

    return (
      <div>
        <div className="spacewalk-toolbar-h1">
          <div className="spacewalk-toolbar">
            <a href="/rhn/manager/vhms">
              <i className="fa spacewalk-icon-virtual-host-manager"></i>
              {t("Edit Virtual Host Managers")}
            </a>
          </div>
          <h1><i className="fa spacewalk-icon-subscription-counting"></i>{t("Subscription Matching")}</h1>
        </div>
        {tabContainer}
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
      // no data available from the backend yet, avoid
      // a flash of unwanted content
      return null;
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Match data status")}</h2>
        <MatcherTaskDescription />
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
      return <div className="text-danger">{t("Could not start a matching run. Please contact your SUSE Manager administrator to make sure the task scheduler is running.")}</div>
    }

    if (this.props.latestStart == null) {
      return (<div>
        {t("No match data is currently available.")}<br/>
        {t("You can also trigger a first run now by clicking the button below.")}
      </div>);
    }

    if (this.props.latestEnd == null) {
      return <div>{t("Matching data is currently being recomputed, it was started {0}.", moment(this.props.latestStart).fromNow())}</div>;
    }

    return <div>{t("Latest successful match data was computed {0}, you can trigger a new run by clicking the button below.", moment(this.props.latestEnd).fromNow())}</div>;
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
        className={buttonClass}
        disabled={this.props.matcherRunning}
        onClick={this.onClick}
      >
        <i className="fa fa-refresh"></i>{t("Refresh matching data")}
      </button>
    );
  }
});

var StatePersistedMixin = {
  componentWillMount: function() {
    if (this.props.loadState) {
      if (this.props.loadState()) {
        this.state = this.props.loadState();
      }
    }
  },
  componentWillUnmount: function() {
    if (this.props.saveState) {
      this.props.saveState(this.state);
    }
  },
};

var UnmatchedSystems = React.createClass({
  mixins: [StatePersistedMixin],

  rowFilter: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData=["name"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var result = 0;
    var aValue = a.props["rawData"][columnKey];
    var bValue = b.props["rawData"][columnKey];
    result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return result * orderCondition;
  },

  render: function() {
    if (this.props.unmatchedSystems != null && this.props.unmatchedSystems.length > 0) {
      return (
        <div className="row col-md-12">
          <h2>{t("Unmatched Systems")}</h2>
          <div className="spacewalk-list">
            <Table headers={[t("Name"), t("Socket/IFL count"), t("Products")]}
              rows={unmatchedSystemsToRows(this.props.unmatchedSystems)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              rowComparator={this.rowFilter}
              sortableColumns={[0]}
            />
            <CsvLink name="unmatched_system_report.csv" />
          </div>
        </div>
      );
    }
    return null;
  }
});

function unmatchedSystemsToRows(systems) {
  return systems.map((s) => {
    var columns = [
      <TableCell content={s.name} />,
      <TableCell content={s.cpuCount} />,
      <TableCell content={s.products.reduce((a,b) => a+", "+b)} />,
    ];
    return <TableRow columns={columns} rawData={s} />
  });
}

var Messages = React.createClass({
  mixins: [StatePersistedMixin],

  rowFilter: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData=["type"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var result = 0;
    var aValue = a.props["rawData"][columnKey];
    var bValue = b.props["rawData"][columnKey];
    result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return result * orderCondition;
  },

  render: function() {
    var body;
    if (this.props.messages != null) {
      if (this.props.messages.length > 0) {
        body = (
          <div className="spacewalk-list">
            <p>{t("Please review warning and information messages below.")}</p>
            <Table
              headers={[t("Message"), t("Additional information")]}
              rows={messagesToRows(this.props.messages)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              rowComparator={this.rowFilter}
              sortableColumns={[0]}
            />
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

function messagesToRows(rawMessages) {
  var result= rawMessages.map(function(rawMessage) {
    var data = rawMessage["data"];
    var message;
    var additionalInformation;
    switch(rawMessage["type"]) {
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
        message = rawMessage["type"];
        additionalInformation = data;
    }
    var columns = [
      <TableCell content={message} />,
      <TableCell content={additionalInformation} />
    ];
    return <TableRow columns={columns}  rawData={rawMessage}/>;
  });
  return result;
}

var Subscriptions = React.createClass({
  mixins: [StatePersistedMixin],

  rowFilter: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData=["partNumber", "description", "policy", "quantity", "startDate", "endDate"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var aRaw = a.props["rawData"];
    var bRaw = b.props["rawData"];
    var result = 0;
    if (columnKey == "quantity") {
      var aMatched = aRaw["matchedQuantity"];
      var aTotal = aRaw["totalQuantity"];
      var bMatched = bRaw["matchedQuantity"];
      var bTotal = bRaw["totalQuantity"];
      var aValue =  aMatched / aTotal;
      var bValue =  bMatched / bTotal;
      result = aValue > bValue ? 1 : (aValue < bValue ? -1 : 0);
    }
    else {
      var aValue = aRaw[columnKey];
      var bValue = bRaw[columnKey];
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }

    if (result == 0) {
      var aId = aRaw["id"];
      var bId = bRaw["id"];
      result = aId > bId ? 1 : (aId < bId ? -1 : 0);
    }
    return result * orderCondition;
  },

  render: function() {
    var body;
    if (this.props.subscriptions != null) {
      if (this.props.subscriptions.length > 0) {
        body = (
          <div className="spacewalk-list">
            <Table headers={[t("Part number"), t("Description"), t("Policy"), t("Matched/Total"), t("Start date"), t("End date")]}
              rows={subscriptionsToRows(this.props.subscriptions)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              dataFilter={(tableRow, searchValue) => tableRow.props["rawData"]["description"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
              searchPlaceholder={t("Filter by description")}
              rowComparator={this.rowFilter}
              sortableColumns={[0,1,2,3,4,5]}
            />
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

function subscriptionsToRows(subscriptions) {
  return subscriptions.map((s) => {
    var now = moment();
    var className = moment(s.endDate).isBefore(now) ?
      "text-muted" :
        moment(s.endDate).isBefore(now.add(3, "months")) ?
        "text-danger" :
        null;

    var columns = [
      <TableCell content={s.partNumber} />,
      <TableCell content={s.description} />,
      <TableCell content={humanReadablePolicy(s.policy)} />,
      <QuantityCell matched={s.matchedQuantity} total={s.totalQuantity} />,
      <TableCell content={
        <ToolTip content={moment(s.startDate).fromNow()}
          title={moment(s.startDate).format("LL")} />}
      />,
      <TableCell content={
        <ToolTip content={moment(s.endDate).fromNow()}
          title={moment(s.endDate).format("LL")} />}
      />,
    ];

    return <TableRow className={className} columns={columns} rawData={s} />
  });
}

function humanReadablePolicy(rawPolicy) {
  var message;
  switch(rawPolicy) {
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
      message = rawPolicy;
  }
  return message;
}

var QuantityCell = React.createClass({
  render: function() {
    var matched = this.props.matched;
    var total = this.props.total;
    var content = matched + "/" + total;

    return (
      matched == total ?
        <TableCell content={<StrongText className="bg-danger" content={content} />} /> :
        <TableCell content={content} />
    );
  }
});

var StrongText = React.createClass({
  render: function() {
    return (
      <strong className={this.props.className}>
        {this.props.content}
      </strong>
    );
  }
});

var ToolTip = React.createClass({
  render: function() {
    return (
      <span title={this.props.title}>
        {this.props.content}
      </span>
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
