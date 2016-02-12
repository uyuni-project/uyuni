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
    var pinnedMatches = data == null ? null : data.pinnedMatches;
    var systems = data == null ? null : data.systems;

    var tabContainer = data == null || !data.matcherDataAvailable ? null :
      <TabContainer
        labels={[t("Subscriptions"), t("Unmatched Systems"), t("Pin Status"), t("Messages")]}
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
          <PinnedMatches
            initialPinnedMatches={pinnedMatches}
            systems={systems}
            subscriptions={subscriptions}
            saveState={(state) => {this.state["pinnedMatchesState"] = state;}}
            loadState={() => this.state["pinnedMatchesState"]}
          />,
          <Messages
            messages={messages}
            systems={systems}
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

  rowComparator: function(a, b, columnIndex, ascending) {
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
              rowComparator={this.rowComparator}
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

  rowComparator: function(a, b, columnIndex, ascending) {
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
              rows={messagesToRows(this.props.messages, this.props.systems)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              rowComparator={this.rowComparator}
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

function messagesToRows(rawMessages, systems) {
  var result= rawMessages.map(function(rawMessage) {
    var data = rawMessage["data"];
    var message;
    var additionalInformation;
    switch(rawMessage["type"]) {
      case "unknownPartNumber" :
        message = t("Unsupported part number detected");
        additionalInformation = data["partNumber"];
        break;
      case "physicalGuest" :
        message = t("Physical system is reported as virtual guest, please check hardware data");
        additionalInformation = systems[data["id"]].name;
        break;
      case "guestWithUnknownHost" :
        message = t("Virtual guest has unknown host, assuming it is a physical system");
        additionalInformation = systems[data["id"]].name;
        break;
      case "unknownCpuCount" :
        message = t("System has an unknown number of sockets, assuming 16");
        additionalInformation = systems[data["id"]].name;
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

  rowComparator: function(a, b, columnIndex, ascending) {
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
      if (Object.keys(this.props.subscriptions).length > 0) {
        body = (
          <div className="spacewalk-list">
            <Table headers={[t("Part number"), t("Description"), t("Policy"), t("Matched/Total"), t("Start date"), t("End date")]}
              rows={subscriptionsToRows(this.props.subscriptions)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              dataFilter={(tableRow, searchValue) => tableRow.props["rawData"]["description"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
              searchPlaceholder={t("Filter by description")}
              rowComparator={this.rowComparator}
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
  return Object.keys(subscriptions).map((k) => {
    var s = subscriptions[k];
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

var PinnedMatches = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {"showPopUp": false, "pinnedMatches": this.props.initialPinnedMatches};
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({"pinnedMatches": nextProps.initialPinnedMatches});
  },

  rowComparator: function(a, b, columnIndex, ascending) {
    var aRaw = a.props["rawData"];
    var bRaw = b.props["rawData"];
    var columnKeyInRawData=["systemName", "subscriptionDescription", "status"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;

    var result = 0;
    var aValue = aRaw[columnKey];
    var bValue = bRaw[columnKey];
    result = aValue.localeCompare(bValue);

    if (result == 0) {
      var aId = aRaw["id"];
      var bId = bRaw["id"];
      result = aId > bId ? 1 : (aId < bId ? -1 : 0);
    }

    return result * orderCondition;
  },

  onRemovePin: function(pinId) {
    $.post("/rhn/manager/subscription_matching/pins/"+pinId+"/delete",
      data => {this.setState({"pinnedMatches" : data});}
    );
  },

  showPopUp: function() {
    this.setState({"showPopUp" : true});
  },

  closePopUp: function() {
    this.setState({"showPopUp" : false});
  },

  savePin: function(systemId, subscriptionId) {
    $.post("/rhn/manager/subscription_matching/pins",
      {"system_id": systemId, "subscription_id": subscriptionId},
      data => {this.setState({"pinnedMatches" : data});}
    );
    $("#addPinPopUp").modal('hide'); //to trigger popup close action
    this.closePopUp();
  },

  render: function() {
    var popUpContent = this.state.showPopUp ? <AddPinPopUp systems={this.props.systems} subscriptions={this.props.subscriptions} onSavePin={this.savePin} /> : null;
    return (
      <div className="row col-md-12">
        <div className="spacewalk-toolbar">
          <button type="button" className="btn btn-primary" onClick={this.showPopUp} data-toggle="modal" data-target="#addPinPopUp">
            <i className="fa fa-map-pin"></i>{t("Add a Pin")}
          </button>
          <PopUp
            title={t("Pin a Systems to a Subscription")}
            className="modal-lg"
            id="addPinPopUp"
            content={
              <div className="spacewalk-list">
                {popUpContent}
              </div>
            }
            onClosePopUp={this.closePopUp}
          />
        </div>
        <h2>{t("Pin Status")}</h2>
        <div className="spacewalk-list">
          {this.state.pinnedMatches.length > 0 ?
            <Table headers={[t("System"), t("Subscription"), t("Pin Status"), t("Unpin")]}
              rows={pinnedMatchesToRows(this.state.pinnedMatches, this.props.systems, this.props.subscriptions, this.onRemovePin)}
              loadState={() => this.state["table"]}
              saveState={(state) => {this.state["table"] = state;}}
              rowComparator={this.rowComparator}
              sortableColumns={[0, 1, 2]}
            /> :
            t("No saved pin at the moment.")}
        </div>
      </div>
    );
  }
});

function pinnedMatchesToRows(pins, systems, subscriptions, onClickAction) {
  return pins.map((p) => {
    var system = systems[p.systemId];
    var systemName = system == null ? "System " + p.systemId : system.name;
    var subscription = subscriptions[p.subscriptionId];
    var subscriptionDescription = subscription == null ? "Subscription " + p.subscriptionId : subscription.description;
    var columns = [
      <TableCell content={systemName} />,
      <TableCell content={subscriptionDescription} />,
      <TableCell content={<PinStatus status={p.status} />} />,
      <TableCell content={
        <PinButton
          onClick={onClickAction}
          elementId={p.id}
          content={<span><i className="fa fa-trash-o"></i>{t("remove")}</span>}
         />
        }
      />
    ];
    return <TableRow columns={columns} rawData={{"id": p.id,"systemName": systemName, "subscriptionDescription": subscriptionDescription,"status": p.status}} />
  });
}

var PinStatus = React.createClass({
  render: function() {
    return (
      this.props.status == "pending" ?
      <span><i className="fa fa-hourglass-start pin-report-icon"></i><em>{t("pending for the next run")}</em></span> :
        this.props.status == "satisfied" ?
        <span><i className="fa fa-check text-success pin-report-icon"></i>{t("satisfied in the last run")}</span> :
        <span><i className="fa fa-times text-danger pin-report-icon"></i><StrongText content={t("unsatisfied in the last run!!")} /></span>
    );
  }
});

var PinButton = React.createClass({
  onClick: function() {
    this.props.onClick(this.props.elementId);
  },

  render: function() {
    return (
      <button className="btn btn-default btn-cell" onClick={this.onClick}>
        {this.props.content}
      </button>
    );
  }
})

var AddPinPopUp = React.createClass({
  getInitialState:function() {
    return {"systemId":null};
  },

  onSystemSelected:function(systemId) {
    this.setState({"systemId": systemId});
  },

  onSubscriptionSelected: function(subscriptionId) {
    this.props.onSavePin(this.state.systemId, subscriptionId);
  },

  render:function() {
    var popUpContent;
    if (this.state.systemId) {
      popUpContent = (
        <Table headers={[t("Part number"),t("Description"), t("Policy"), t("Start date"), t("End date"), t("")]}
          rows={
            possibleSubscriptionToRow(
              this.props.systems[this.state.systemId]
                .possibleSubscriptionIds.map((p) => {
                  return this.props.subscriptions[p]}),
              this.onSubscriptionSelected)
            }
        />
      );
    }
    else {
      popUpContent = (
        <Table headers={[t("System"), t("")]} rows={systemsForPinningToRow(this.props.systems, this.onSystemSelected)}
          dataFilter={(tableRow, searchValue) => tableRow.props["rawData"]["name"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
          searchPlaceholder={t("Filter by name")}
        />
      );
    }
    return (popUpContent);
  }
});

function systemsForPinningToRow(systems, onClickAction) {
  return Object.keys(systems).map((k) => {
    var s = systems[k];
    var columns = [
      <TableCell content={s.name} />,
      <TableCell content={
        <PinButton
          onClick={onClickAction}
          elementId={k}
          content={<span><i className="fa fa-map-pin"></i>{t("Pin this system")}</span>}
        />}
      />
    ];
    return <TableRow columns={columns} rawData={s} />
  });
}

function possibleSubscriptionToRow(possibleSubscriptions, onClickAction) {
  return possibleSubscriptions.map((s) => {
    var columns = [
      <TableCell content={s.partNumber} />,
      <TableCell content={s.description} />,
      <TableCell content={humanReadablePolicy(s.policy)} />,
      <TableCell content={
        <ToolTip content={moment(s.startDate).fromNow()}
          title={moment(s.startDate).format("LL")} />}
      />,
      <TableCell content={
        <ToolTip content={moment(s.endDate).fromNow()}
          title={moment(s.endDate).format("LL")} />}
      />,
      <TableCell content={
        <PinButton
          onClick={onClickAction}
          elementId={s.id}
          content={<span><i className="fa fa-map-pin"></i>{t("Pin this subscription")}</span>}
        />}
      />
    ];
    return <TableRow columns={columns} rawData={s} />
  });
}

var PopUp = React.createClass({
  onClose: function() {
    this.props.onClosePopUp();
  },

  render: function() {
    return (
      <div className="modal fade" tabindex="-1" role="dialog" aria-labelledby="addPinPopUpLabel" id={this.props.id} >
        <div className={"modal-dialog " + this.props.className}>
          <div className="modal-content">
            <div className="modal-header">
              <button type="button" className="close" data-dismiss="modal" aria-label="Close" onClick={this.onClose}>
                <span aria-hidden="true">&times;</span>
              </button>
              <h4 className="modal-title">{this.props.title}</h4>
            </div>
            <div className="modal-body">{this.props.content}</div>
            {this.props.footer ? <div className="modal-footer">{this.props.footer}</div> : null}
          </div>
        </div>
      </div>
    );
  }
});

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
