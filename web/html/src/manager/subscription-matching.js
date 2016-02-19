"use strict";

var React = require("react");
var TableComponent = require("../components/table");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var TabContainer = require("../components/tab-container").TabContainer;
var Subscriptions =  require("./subscription-matching-subscriptions").Subscriptions;
var Pins =  require("./subscription-matching-pins").Pins;
var Messages =  require("./subscription-matching-messages").Messages;
var UnmatchedProducts =  require("./subscription-matching-unmatchedProducts").UnmatchedProducts;
var MatcherRunPanel =  require("./subscription-matching-matcherRunPanel").MatcherRunPanel;

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

React.render(
  <SubscriptionMatching refreshInterval={5000} />,
  document.getElementById("subscription-matching")
);
