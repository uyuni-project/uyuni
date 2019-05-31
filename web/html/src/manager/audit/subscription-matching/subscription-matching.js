/* eslint-disable */
"use strict";

const React = require("react");
const ReactDOM = require("react-dom");
const TabContainer = require("components/tab-container").TabContainer;
const Subscriptions =  require("./subscription-matching-subscriptions").Subscriptions;
const Pins =  require("./subscription-matching-pins").Pins;
const Messages =  require("./subscription-matching-messages").Messages;
const UnmatchedProducts =  require("./subscription-matching-unmatched-products").UnmatchedProducts;
const MatcherRunPanel =  require("./subscription-matching-matcher-run-panel").MatcherRunPanel;
const WarningIcon =  require("./subscription-matching-util").WarningIcon;
const MessageContainer = require("components/messages").Messages;
const { TopPanel } = require('components/panels/TopPanel');
const MessagesUtils = require("components/messages").Utils;
const Network = require("utils/network");
const SpaRenderer  = require("core/spa/spa-renderer").default;

class SubscriptionMatching extends React.Component {
  state = {
    serverData: null,
    error: null
  };

  UNSAFE_componentWillMount() {
    this.refreshServerData();
    setInterval(this.refreshServerData, this.props.refreshInterval);
  }

  refreshServerData = () => {
    this.refreshRequest = Network.get("/rhn/manager/api/subscription-matching/data", "application/json");
    this.refreshRequest.promise
      .then(data => {
        this.setState({
          serverData: data,
          error: null
        });
      })
      .catch(response => {
        this.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null
        });
      });
  };

  onPinChanged = (pinnedMatches) => {
    if (this.refreshRequest) {
      this.refreshRequest.cancel();
    }
    const serverData = this.state.serverData;
    serverData.pinnedMatches = pinnedMatches;
    this.setState({serverData: serverData});
  };

  onMatcherRunSchedule = () => {
    if (this.refreshRequest) {
      this.refreshRequest.cancel();
    }
  };

  render() {
    const data = this.state.serverData;

    return (
      <div>
        <div className="spacewalk-toolbar">
          <a href="/rhn/manager/vhms">
            <i className="fa spacewalk-icon-virtual-host-manager"></i>
              {t("Edit Virtual Host Managers")}
          </a>
        </div>
        <TopPanel title={t("Subscription Matching")} icon="spacewalk-icon-subscription-counting" helpUrl="/docs/reference/audit/audit-subscription-matching.html" />
        <ErrorMessage error={this.state.error} />
        <SubscriptionMatchingTabContainer data={data} onPinChanged={this.onPinChanged} />
        <MatcherRunPanel
          dataAvailable={data != null}
          initialLatestStart={data == null ? null : data.latestStart}
          initialLatestEnd={data == null ? null : data.latestEnd}
          onMatcherRunSchedule={this.onMatcherRunSchedule}
        />
      </div>
    );
  }
}

const ErrorMessage = (props) => <MessageContainer items={
    props.error == "authentication" ?
      MessagesUtils.warning(t("Session expired, please reload the page to see up-to-date data.")) :
    props.error == "general" ?
      MessagesUtils.warning(t("Server error, please check log files.")) :
    []
  } />
;

class SubscriptionMatchingTabContainer extends React.Component {
  state = {activeTabHash: document.location.hash};

  UNSAFE_componentWillMount() {
    window.addEventListener("popstate", () => {
      this.setState({activeTabHash: document.location.hash});
    });
  }

  onTabHashChange = (hash) => {
    history.pushState(null, null, hash);
    this.setState({activeTabHash: hash});
  };

  render() {
    const data = this.props.data;

    if (data == null || !data.matcherDataAvailable) {
      return null;
    }

    const pinLabelIcon = data.pinnedMatches.filter((p) => p.status == "unsatisfied").length > 0 ?
      <WarningIcon iconOnRight={true} /> : null;

    const messageLabelIcon = data.messages.length > 0 ?
      <WarningIcon iconOnRight={true} /> : null;

    return (
      <TabContainer
        labels={[t("Subscriptions"), t("Unmatched Products"), <span>{t("Pins ")}{pinLabelIcon}</span>, <span>{t("Messages ")}{messageLabelIcon}</span>]}
        hashes={["#subscriptions", "#unmatched-products", "#pins", "#messages"]}
        tabs={[
          <Subscriptions
            subscriptions={data.subscriptions}
            saveState={(state) => {this.state["subscriptionTableState"] = state;}}
            loadState={() => this.state["subscriptionTableState"]}
          />,
          <UnmatchedProducts
            products={data.products}
            unmatchedProductIds={data.unmatchedProductIds}
            systems={data.systems}
            saveState={(state) => {this.state["unmatchedProductTableState"] = state;}}
            loadState={() => this.state["unmatchedProductTableState"]}
          />,
          <Pins
            pinnedMatches={data.pinnedMatches}
            products={data.products}
            systems={data.systems}
            subscriptions={data.subscriptions}
            onPinChanged={this.props.onPinChanged}
            saveState={(state) => {this.state["pinnedMatchesState"] = state;}}
            loadState={() => this.state["pinnedMatchesState"]}
          />,
          <Messages
            messages={data.messages}
            systems={data.systems}
            subscriptions={data.subscriptions}
            saveState={(state) => {this.state["messageTableState"] = state;}}
            loadState={() => this.state["messageTableState"]}
          />
        ]}
        initialActiveTabHash={this.state.activeTabHash}
        onTabHashChange={this.onTabHashChange}
      />
    );
  }
}

SpaRenderer.renderNavigationReact(
  <SubscriptionMatching refreshInterval={60 * 1000} />,
  document.getElementById("subscription-matching")
);
