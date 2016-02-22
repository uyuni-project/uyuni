"use strict";

var React = require("react");
var TableComponent = require("../components/table");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var StatePersistedMixin = require("../components/util").StatePersistedMixin;
var PopUp = require("../components/popup").PopUp;
var UtilComponent =  require("./subscription-matching-util");
var StrongText = UtilComponent.StrongText;
var SystemLabel = UtilComponent.SystemLabel;
var ToolTip = UtilComponent.ToolTip;
var humanReadablePolicy = UtilComponent.humanReadablePolicy;

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

  buildRows: function(pins, systems, subscriptions, onClickAction) {
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
            rows={this.buildRows(this.props.pinnedMatches, this.props.systems, this.props.subscriptions, this.onRemovePin)}
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

  buildRows: function(systems, onClickAction) {
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

  render: function() {
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
          <Table headers={[t("System"), t("Socket/IFL count"), t("")]} rows={this.buildRows(this.props.systems, this.onSystemSelected)}
            rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["name"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
            filterPlaceholder={t("Filter by name")}
          />
        </div>
      );
    }
    return (popUpContent);
  }
});

var PinSubscriptionSelector = React.createClass({
  buildRows: function(possibleSubscriptions, onClickAction) {
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
  },

  render: function() {
    if (this.props.subscriptions.length > 0) {
      return <Table headers={[t("Part number"),t("Description"), t("Policy"), t("End date"), t("")]}
          rows={
            this.buildRows(
              this.props.subscriptions,
              this.props.onSubscriptionSelected)
            }
        />;
    }
    else {
      return <p>{t("No matching subscriptions for this systems have been found.")}</p>
    }
  },
});

module.exports = {
  Pins: Pins,
}
