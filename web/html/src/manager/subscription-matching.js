"use strict";

var React = require("react");
var TableComponent = require("../components/table");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var TabContainer = require("../components/tab-container").TabContainer;
var StatePersistedMixin = require("../components/util").StatePersistedMixin;
var PopUp = require("../components/popup").PopUp;

var SubscriptionMatching = React.createClass({
  getInitialState: function() {
    return {activeTabHash: document.location.hash};
  },

  componentWillMount: function() {
    window.addEventListener("popstate", () => {
      this.setState({activeTabHash: document.location.hash});
    });

    this.refreshServerData();
    setInterval(this.refreshServerData, this.props.refreshInterval);
  },

  refreshServerData: function() {
    this.refreshRequest = $.get("/rhn/manager/subscription-matching/data", data => {
      this.setState({serverData: data});
    });
  },

  onPinChanged: function(pinnedMatches) {
    if (this.refreshRequest) {
      this.refreshRequest.abort();
    }
    this.state.serverData.pinnedMatches = pinnedMatches;
    this.setState(this.state);
  },

  onTabHashChange: function(hash) {
    history.pushState(null, null, hash);
    this.setState({activeTabHash: hash});
  },

  onMatcherRunSchedule: function() {
    if (this.refreshRequest) {
      this.refreshRequest.abort();
    }
  },

  render: function() {
    var data = this.state.serverData;
    var latestStart = data == null ? null : data.latestStart;
    var latestEnd = data == null ? null : data.latestEnd;
    var messages = data == null ? null : data.messages;
    var subscriptions = data == null ? null : data.subscriptions;
    var products = data == null ? null : data.products;
    var unmatchedProductIds = data == null ? null : data.unmatchedProductIds;
    var pinnedMatches = data == null ? null : data.pinnedMatches;
    var systems = data == null ? null : data.systems;

    var pinLabelIcon = data != null && data.pinnedMatches.filter((p) => p.status == "unsatisfied").length > 0 ?
      <i className="fa fa-exclamation-triangle text-warning"></i> : null;

    var messageLabelIcon = data != null && data.messages.length > 0 ?
      <i className="fa fa-exclamation-circle text-danger"></i> : null;

    var tabContainer = data == null || !data.matcherDataAvailable ? null :
      <TabContainer
        labels={[t("Subscriptions"), t("Unmatched Products"), <span>{t("Pins ")}{pinLabelIcon}</span>, <span>{t("Messages ")}{messageLabelIcon}</span>]}
        hashes={["#subscriptions", "#unmatched-products", "#pins", "#messages"]}
        tabs={[
          <Subscriptions
            subscriptions={subscriptions}
            saveState={(state) => {this.state["subscriptionTableState"] = state;}}
            loadState={() => this.state["subscriptionTableState"]}
          />,
          <UnmatchedProducts
            products={products}
            unmatchedProductIds={unmatchedProductIds}
            systems={systems}
            saveState={(state) => {this.state["unmatchedProductTableState"] = state;}}
            loadState={() => this.state["unmatchedProductTableState"]}
          />,
          <Pins
            pinnedMatches={pinnedMatches}
            systems={systems}
            subscriptions={subscriptions}
            onPinChanged={this.onPinChanged}
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
        initialActiveTabHash={this.state.activeTabHash}
        onTabHashChange={this.onTabHashChange}
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
        <MatcherRunPanel dataAvailable={data != null} initialLatestStart={latestStart} initialLatestEnd={latestEnd} onMatcherRunSchedule={this.onMatcherRunSchedule} />
      </div>
    );
  }
});

var MatcherRunPanel = React.createClass({
  getInitialState: function() {
    return {
      latestStart: this.props.initialLatestStart,
      latestEnd: this.props.initialLatestEnd,
      error: false,
    }
  },

  componentWillReceiveProps: function(nextProps) {
    if (this.state.latestStart == null || nextProps.initialLatestStart >= this.state.latestStart) {
      this.setState({
        latestStart: nextProps.initialLatestStart,
        latestEnd: nextProps.initialLatestEnd,
        error: false,
      });
    }
  },

  onScheduled: function() {
    this.setState({
        latestStart: new Date().toJSON(),
        latestEnd: null,
        error: false,
      }
    );
    this.props.onMatcherRunSchedule();
  },

  onError: function() {
    this.setState({error: true});
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

var MatcherRunDescription = (props) => {
  if (props.error) {
    return <div className="text-danger">{t("Could not start a matching run. Please contact your SUSE Manager administrator to make sure the task scheduler is running.")}</div>
  }

  if (props.latestStart == null) {
    return (<div>
      {t("No match data is currently available.")}<br/>
      {t("You can also trigger a first run now by clicking the button below.")}
    </div>);
  }

  if (props.latestEnd == null) {
    return <div>{t("Matching data is currently being recomputed, it was started {0}.", moment(props.latestStart).fromNow())}</div>;
  }

  return <div>{t("Latest successful match data was computed {0}, you can trigger a new run by clicking the button below.", moment(props.latestEnd).fromNow())}</div>;
}

var MatcherTaskDescription = () =>
  <div>
    {t("Match data is computed via a task schedule, nightly by default (you can ")}
    <a href="/rhn/admin/BunchDetail.do?label=gatherer-matcher-bunch">{t("change the task schedule from the administration page")}</a>
    {t("). ")}
  </div>
;

var MatcherScheduleButton = React.createClass({
  onClick: function() {
    $.post("/rhn/manager/subscription-matching/schedule-matcher-run")
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

var UnmatchedProducts = React.createClass({
  mixins: [StatePersistedMixin],

  rowComparator: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData = ["productName", "systemCount"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var result = 0;
    var aValue = a.props["rawData"][columnKey];
    var bValue = b.props["rawData"][columnKey];
    if (columnKey == "systemCount") {
      result = aValue > bValue ? 1 : (aValue < bValue ? -1 : 0);
    }
    else {
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }

    if (result == 0) {
      var aId = a.props["rawData"]["id"];
      var bId = b.props["rawData"]["id"];
      result = aId > bId ? 1 : (aId < bId ? -1 : 0);
    }

    return result * orderCondition;
  },

  systemsComparator: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData = ["systemName"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var result = 0;
    var aValue = a.props["rawData"][columnKey];
    var bValue = b.props["rawData"][columnKey];
    result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    return result * orderCondition;
  },

  getInitialState: function() {
    return {selectedProductId: null};
  },

  showPopUp: function(id) {
    this.setState({selectedProductId: id});
  },

  closePopUp: function() {
    this.setState({selectedProductId: null});
  },

  render: function() {
    var body;
    if (this.props.unmatchedProductIds != null) {
      if (this.props.unmatchedProductIds.length > 0) {
        var popUpContent = this.state.selectedProductId == null ?
          null :
          <Table
            headers={[t("System name")]}
            rows={unmatchedSystemsToRows(this.props.products[this.state.selectedProductId], this.props.systems)}
            rowComparator={this.systemsComparator}
            sortableColumnIndexes={[0]}
            rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["systemName"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
            filterPlaceholder={t("Filter by name")}
          />
        ;

        body = (
          <div>
            <Table headers={[t("Product name"), t("Unmatched system count"), ""]}
              rows={unmatchedProductsToRows(this)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              rowComparator={this.rowComparator}
              sortableColumnIndexes={[0, 1]}
            />
            <CsvLink name="unmatched_product_report.csv" />

            <PopUp title={t("Unmatched systems")}
              id="unmatchedProductsPopUp"
              content={popUpContent}
              onClosePopUp={this.closePopUp}
            />
          </div>
        );
      }
      else {
        body = <p>{t("No unmatching products are found.")}</p>
      }
    }
    else {
      body = <p>{t("Loading...")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Unmatched Products")}</h2>
        {body}
      </div>
    );
  }
});

function unmatchedSystemsToRows(product, systems) {
  return product.unmatchedSystemIds.map((sid) => {
    var systemName = systems[sid].name;
    var column = <TableCell content={systemName}/>;
    var rawData = {systemName: systemName};

    return <TableRow columns={[column]} rawData={rawData} />
  });
}

function unmatchedProductsToRows(myParent) {
  var products = myParent.props.products;
  return myParent.props.unmatchedProductIds.map((pid) => {
    var productName = products[pid].productName;
    var systemCount = products[pid].unmatchedSystemCount;
    var listButton =
        <button
          className="btn btn-default btn-cell"
          onClick={function() {myParent.showPopUp(pid);}}
          data-toggle="modal"
          data-target="#unmatchedProductsPopUp">
          {t("Show system list")}
        </button>;

    var columns = [
      <TableCell content={productName} />,
      <TableCell content={systemCount} />,
      <TableCell content={listButton} />,
    ];

    var rawData = {
      id: pid,
      productName: productName,
      systemCount: systemCount
    };

    return <TableRow columns={columns} rawData={rawData} />
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
          <div>
            <p>{t("Please review warning and information messages below.")}</p>
            <Table
              headers={[t("Message"), t("Additional information")]}
              rows={messagesToRows(this.props.messages, this.props.systems)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              rowComparator={this.rowComparator}
              sortableColumnIndexes={[0]}
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
    if (columnKey == "policy") {
      var aValue = humanReadablePolicy(aRaw[columnKey]);
      var bValue = humanReadablePolicy(bRaw[columnKey]);
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }
    else if (columnKey == "quantity") {
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
          <div>
            <Table headers={[t("Part number"), t("Description"), t("Policy"), t("Matched/Total"), t("Start date"), t("End date")]}
              rows={subscriptionsToRows(this.props.subscriptions)}
              loadState={this.props.loadState}
              saveState={this.props.saveState}
              rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["description"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
              filterPlaceholder={t("Filter by description")}
              rowComparator={this.rowComparator}
              sortableColumnIndexes={[0,1,2,3,4,5]}
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

var Pins = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {showPopUp: false};
  },

  rowComparator: function(a, b, columnIndex, ascending) {
    var aRaw = a.props["rawData"];
    var bRaw = b.props["rawData"];
    var columnKeyInRawData=["systemName", "subscriptionDescription", "subscriptionPolicy", "subscriptionEndDate", "subscriptionPartNumber", "status"];
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
    $.post("/rhn/manager/subscription-matching/pins/"+pinId+"/delete",
      data => {this.props.onPinChanged(data);}
    );
  },

  showPopUp: function() {
    this.setState({showPopUp: true});
  },

  closePopUp: function() {
    this.setState({showPopUp: false});
  },

  savePin: function(systemId, subscriptionId) {
    $.post("/rhn/manager/subscription-matching/pins",
      {system_id: systemId, subscription_id: subscriptionId},
      data => {this.props.onPinChanged(data);}
    );
    $("#addPinPopUp").modal("hide"); //to trigger popup close action
    this.closePopUp();
  },

  render: function() {
    var popUpContent = this.state.showPopUp ? <AddPinPopUp systems={this.props.systems} subscriptions={this.props.subscriptions} onSavePin={this.savePin} /> : null;
    return (
      <div className="row col-md-12">
        <h2>{t("Pins")}</h2>
        <p>
          {t("You can pin a subscription to a system to suggest a certain association to the matching algorithm. ")}
          <br />
          {t("Next time a matching is attempted, the algorithm will try to produce a result that applies the subscription to the system you specified. ")}
          <br />
          {t("Note that the algorithm might determine that a certain pin cannot be respected, ")}
          {t("depending on a subscription's availablility and applicability rules, in that case it will be shown as not satisfied. ")}
        </p>

        {this.props.pinnedMatches.length > 0 ?
          <Table headers={[t("System"), t("Subscription"), t("Policy"), t("End date"), t("Part number"), t("Status"), t("")]}
            rows={pinnedMatchesToRows(this.props.pinnedMatches, this.props.systems, this.props.subscriptions, this.onRemovePin)}
            loadState={() => this.state["table"]}
            saveState={(state) => {this.state["table"] = state;}}
            rowComparator={this.rowComparator}
            sortableColumnIndexes={[0, 1, 2, 3, 4, 5]}
          /> :
          <p>{t("No pins defined. You can create one with the button below.")}</p>}

        <button type="button" className="btn btn-primary" onClick={this.showPopUp} data-toggle="modal" data-target="#addPinPopUp">
          <i className="fa fa-plus"></i>{t("Add a Pin")}
        </button>
        <PopUp
          title={t("Add a Pin")}
          className="modal-lg"
          id="addPinPopUp"
          content={popUpContent}
          onClosePopUp={this.closePopUp}
        />
      </div>
    );
  }
});

function pinnedMatchesToRows(pins, systems, subscriptions, onClickAction) {
  return pins.map((p) => {
    var system = systems[p.systemId];
    var systemName = system == null ? "System " + p.systemId : system.name;
    var systemType = system == null ? null : system.type;
    var subscription = subscriptions[p.subscriptionId];
    var subscriptionDescription = subscription == null ? "Subscription " + p.subscriptionId : subscription.description;
    var subscriptionPolicy = subscription == null ? " " : subscription.policy;
    var subscriptionEndDate = subscription == null ? " " : subscription.endDate;
    var subscriptionPartNumber = subscription == null ? "" : subscription.partNumber;
    var columns = [
      <TableCell content={<SystemLabel id={p.systemId} name={systemName} type={systemType} />} />,
      <TableCell content={subscriptionDescription} />,
      <TableCell content={humanReadablePolicy(subscriptionPolicy)} />,
      <TableCell content={
        <ToolTip content={moment(subscriptionEndDate).fromNow()}
          title={moment(subscriptionEndDate).format("LL")} />}
      />,
      <TableCell content={subscriptionPartNumber} />,
      <TableCell content={<PinStatus status={p.status} />} />,
      <TableCell content={
        <PinButton
          onClick={() => onClickAction(p.id)}
          content={<span><i className="fa fa-trash-o"></i>{t("Delete Pin")}</span>}
         />
        }
      />
    ];
    var rawData = {
      id: p.id,
      systemName: systemName,
      subscriptionDescription: subscriptionDescription,
      subscriptionPolicy: subscriptionPolicy,
      subscriptionEndDate: subscriptionEndDate,
      subscriptionPartNumber: subscriptionPartNumber,
      status: p.status
   };
    return <TableRow columns={columns} rawData={rawData} />
  });
}

var SystemLabel = (props) => {
  var icon;
  if (props.type == "nonVirtual") {
    icon = <i className="fa fa-desktop"></i>;
  }
  else if (props.type == "virtualHost") {
    icon = <i className="fa spacewalk-icon-virtual-host"></i>;
  }
  else if (props.type == "virtualGuest") {
    icon = <i className="fa spacewalk-icon-virtual-guest"></i>;
  }
  else {
    icon = null;
  }

  return <span>{icon} {props.name}</span>;
}

var PinStatus = (props) => {
  if (props.status == "pending") {
    return <span><i className="fa fa-hourglass-start pin-report-icon"></i><em>{t("pending next run")}</em></span>;
  }
  if (props.status == "satisfied") {
    return <span><i className="fa fa-check text-success pin-report-icon"></i>{t("satisfied")}</span>;
  }
  return <span><i className="fa fa-exclamation-triangle text-warning pin-report-icon"></i>{t("not satisfied")}</span>;
}

var PinButton = (props) =>
  <button className="btn btn-default btn-cell" onClick={props.onClick}>
    {props.content}
  </button>
;

var AddPinPopUp = React.createClass({
  getInitialState:function() {
    return {systemId: null};
  },

  onBackClicked: function () {
    this.setState({systemId: null});
  },

  onSystemSelected:function(systemId) {
    this.setState({systemId: systemId});
  },

  onSubscriptionSelected: function(subscriptionId) {
    this.props.onSavePin(this.state.systemId, subscriptionId);
  },

  render:function() {
    var popUpContent;
    if (this.state.systemId) {
      var system = this.props.systems[this.state.systemId];
      popUpContent = (
        <div>
          <p>{t("Step 2/2: pick a subscription for system ")}<strong>{system.name}</strong></p>
          <PinSubscriptionSelector onSubscriptionSelected={this.onSubscriptionSelected}
            subscriptions={system.possibleSubscriptionIds.map(
            p => this.props.subscriptions[p]
          )} />
          <p>
            <button className="btn btn-default" onClick={this.onBackClicked}>
              <i className="fa fa-arrow-left"></i>
              {t("Back to sytem selection")}
            </button>
          </p>
        </div>
      );
    }
    else {
      popUpContent = (
        <div>
          <p>{t("Step 1/2: select the system to pin from the table below.")}</p>
          <Table headers={[t("System"), t("Socket/IFL count"), t("")]} rows={systemsForPinningToRow(this.props.systems, this.onSystemSelected)}
            rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["name"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
            filterPlaceholder={t("Filter by name")}
          />
        </div>
      );
    }
    return (popUpContent);
  }
});

var PinSubscriptionSelector = (props) => {
  if (props.subscriptions.length > 0) {
    return <Table headers={[t("Part number"),t("Description"), t("Policy"), t("End date"), t("")]}
        rows={
          possibleSubscriptionToRow(
            props.subscriptions,
            props.onSubscriptionSelected)
          }
      />;
  }
  else {
    return <p>{t("No matching subscriptions for this systems have been found.")}</p>
  }
}

function systemsForPinningToRow(systems, onClickAction) {
  return Object.keys(systems).map((k) => {
    var s = systems[k];
    var columns = [
      <TableCell content={<SystemLabel id={k} name={s.name} type={s.type} />} />,
      <TableCell content={s.cpuCount} />,
      <TableCell content={
        <PinButton
          onClick={() => onClickAction(k)}
          content={<span>{t("Select")} <i className="fa fa-arrow-right fa-right"></i></span>}
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
        <ToolTip content={moment(s.endDate).fromNow()}
          title={moment(s.endDate).format("LL")} />}
      />,
      <TableCell content={
        <PinButton
          onClick={() => onClickAction(s.id)}
          content={<span><i className="fa fa-map-pin"></i>{t("Save Pin")}</span>}
        />}
      />
    ];
    return <TableRow columns={columns} rawData={s} />
  });
}

var QuantityCell = (props) => {
  var matched = props.matched;
  var total = props.total;
  var content = matched + "/" + total;

  if (matched == total) {
    return <TableCell content={<StrongText className="bg-danger" content={content} />} />;
  }
  return <TableCell content={content} />;
}

var StrongText = (props) =>
  <strong className={props.className}>
    {props.content}
  </strong>
;

var ToolTip = (props) =>
  <span title={props.title}>
    {props.content}
  </span>
;

var CsvLink = (props) =>
  <div className="spacewalk-csv-download">
    <a className="btn btn-link" href={"/rhn/manager/subscription-matching/" + props.name}>
      <i className="fa spacewalk-icon-download-csv"></i>
      {t("Download CSV")}
    </a>
  </div>
;

React.render(
  <SubscriptionMatching refreshInterval={5000} />,
  document.getElementById("subscription-matching")
);
