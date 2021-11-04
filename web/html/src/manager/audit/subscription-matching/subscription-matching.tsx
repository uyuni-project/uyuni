import * as React from "react";
import { TabContainer } from "components/tab-container";
import { Subscriptions } from "./subscription-matching-subscriptions";
import { Pins } from "./subscription-matching-pins";
import { Messages } from "./subscription-matching-messages";
import { UnmatchedProducts } from "./subscription-matching-unmatched-products";
import { MatcherRunPanel } from "./subscription-matching-matcher-run-panel";
import { WarningIcon } from "./subscription-matching-util";
import { Messages as MessageContainer } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Utils as MessagesUtils } from "components/messages";
import Network from "utils/network";
import SpaRenderer from "core/spa/spa-renderer";
import { Cancelable } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

type SubscriptionMatchingProps = {
  refreshInterval: number;
};

class SubscriptionMatching extends React.Component<SubscriptionMatchingProps> {
  timerId?: number;
  refreshRequest?: Cancelable;
  state = {
    serverData: null as any | null,
    error: null as any | null,
  };

  UNSAFE_componentWillMount() {
    this.refreshServerData();
    this.timerId = window.setInterval(this.refreshServerData, this.props.refreshInterval);
  }

  componentWillUnmount() {
    clearInterval(this.timerId);
  }

  refreshServerData = () => {
    this.refreshRequest = Network.get("/rhn/manager/api/subscription-matching/data");
    this.refreshRequest
      .then((data) => {
        this.setState({
          serverData: data,
          error: null,
        });
      })
      .catch((response) => {
        this.setState({
          error: DEPRECATED_unsafeEquals(response.status, 401)
            ? "authentication"
            : response.status >= 500
            ? "general"
            : null,
        });
      });
  };

  onPinChanged = (pinnedMatches) => {
    if (this.refreshRequest) {
      this.refreshRequest.cancel();
    }
    const serverData = this.state.serverData;
    serverData.pinnedMatches = pinnedMatches;
    this.setState({ serverData: serverData });
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
        <TopPanel
          title={t("Subscription Matching")}
          icon="spacewalk-icon-subscription-counting"
          helpUrl="reference/audit/audit-subscription-matching.html"
        />
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

const ErrorMessage = (props) => (
  <MessageContainer
    items={
      props.error === "authentication"
        ? MessagesUtils.warning(t("Session expired, please reload the page to see up-to-date data."))
        : props.error === "general"
        ? MessagesUtils.warning(t("Server error, please check log files."))
        : []
    }
  />
);

type SubscriptionMatchingTabContainerProps = {
  data: any;
  onPinChanged: (...args: any[]) => any;
};

class SubscriptionMatchingTabContainer extends React.Component<SubscriptionMatchingTabContainerProps> {
  state = { activeTabHash: document.location.hash };

  UNSAFE_componentWillMount() {
    window.addEventListener("popstate", () => {
      this.setState({ activeTabHash: document.location.hash });
    });
  }

  onTabHashChange = (hash) => {
    window.history.pushState(null, "", hash);
    this.setState({ activeTabHash: hash });
  };

  render() {
    const data = this.props.data;

    if (data == null || !data.matcherDataAvailable) {
      return null;
    }

    const pinLabelIcon =
      data.pinnedMatches.filter((p) => p.status === "unsatisfied").length > 0 ? (
        <WarningIcon iconOnRight={true} />
      ) : null;

    const messageLabelIcon = data.messages.length > 0 ? <WarningIcon iconOnRight={true} /> : null;

    return (
      <TabContainer
        labels={[
          t("Subscriptions"),
          t("Unmatched Products"),
          <span>
            {t("Pins ")}
            {pinLabelIcon}
          </span>,
          <span>
            {t("Messages ")}
            {messageLabelIcon}
          </span>,
        ]}
        hashes={["#subscriptions", "#unmatched-products", "#pins", "#messages"]}
        tabs={[
          <Subscriptions subscriptions={data.subscriptions} />,
          <UnmatchedProducts
            products={data.products}
            unmatchedProductIds={data.unmatchedProductIds}
            systems={data.systems}
          />,
          <Pins
            pinnedMatches={data.pinnedMatches}
            products={data.products}
            systems={data.systems}
            subscriptions={data.subscriptions}
            onPinChanged={this.props.onPinChanged}
          />,
          <Messages messages={data.messages} systems={data.systems} subscriptions={data.subscriptions} />,
        ]}
        initialActiveTabHash={this.state.activeTabHash}
        onTabHashChange={this.onTabHashChange}
      />
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(
    <SubscriptionMatching refreshInterval={60 * 1000} />,
    document.getElementById("subscription-matching")
  );
