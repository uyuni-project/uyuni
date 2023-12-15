import * as React from "react";

import { ActionChain, ActionSchedule } from "components/action-schedule";
import { AsyncButton, Button } from "components/buttons";
import { ActionChainLink, ActionLink } from "components/links";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages";
import { InnerPanel } from "components/panels/InnerPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { localizedMoment } from "utils";
import { Utils } from "utils/functions";
import Network from "utils/network";

const SELECTION_KEY_SEPARATOR = "~*~";

type Props = {
  serverId: number;
  selectionSet: string;
  actionChains: Array<ActionChain>;
  icon: string;
  listDataAPI: string;
  scheduleActionAPI: string;
  actionType: string;
  listTitle: string;
  listSummary: string;
  listEmptyText: string;
  listActionLabel: string;
  listColumns: React.ReactNode[];
  confirmTitle: string;
};

type State = {
  messages: Array<MessageType>;
  selectedPackages: string[];
  confirmAction: boolean;
  earliest: moment.Moment;
  actionChain?: ActionChain;
};

export class PackageListActionScheduler extends React.Component<Props, State> {
  constructor(props) {
    super(props);

    this.state = {
      messages: [],
      selectedPackages: [],
      confirmAction: false,
      earliest: localizedMoment(),
    };
  }

  confirmAction = () => {
    this.setState({
      confirmAction: true,
      messages: [],
    });
  };

  backToList = () => {
    this.setState({
      confirmAction: false,
      messages: [],
    });
  };

  scheduleAction = () => {
    const requestBody = {
      actionType: this.props.actionType,
      earliest: this.state.earliest,
      actionChain: this.state.actionChain ? this.state.actionChain.text : null,
      selectedPackages: this.state.selectedPackages,
    };

    Network.post(this.props.scheduleActionAPI, requestBody)
      .then((data) => {
        // Notify the successful outcome
        const msg = MessagesUtils.info(
          this.state.actionChain ? (
            <span>
              {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
                name: this.state.actionChain.text,
                link: (str) => <ActionChainLink id={data}>{str}</ActionChainLink>,
              })}
            </span>
          ) : (
            <span>
              {t("The action has been <link>scheduled</link>.", {
                link: (str) => <ActionLink id={data}>{str}</ActionLink>,
              })}
            </span>
          )
        );

        // Clear the current selection
        Network.post(`/rhn/manager/api/sets/${this.props.selectionSet}/clear`).catch((err) => {
          this.setState({ messages: MessagesUtils.warning(t("Unable to clear selection")) });
        });

        this.setState({
          confirmAction: false,
          messages: msg,
        });
      })
      .catch((err) => {
        this.setState({ messages: MessagesUtils.error(t("Unable to perform action.")) });
      });
  };

  onDateTimeChanged = (date) => {
    this.setState({ earliest: date });
  };

  onActionChainChanged = (actionChain) => {
    this.setState({ actionChain: actionChain });
  };

  searchData = (item, criteria) => {
    if (criteria) {
      const value = this.packageNameFromSelectionKey(item);
      return value.toLocaleLowerCase().includes(criteria.toLocaleLowerCase());
    }

    return true;
  };

  packageNameLink = (item) => {
    if (item.packageId == null) {
      return item.nvre;
    }

    return <a href={`/rhn/software/packages/Details.do?pid=${item.packageId}`}>{item.nvre}</a>;
  };

  buildSelectionKey = (item) => {
    if (item.nvrea != null) {
      return item.idCombo + SELECTION_KEY_SEPARATOR + item.nvrea;
    }

    return item.idCombo + SELECTION_KEY_SEPARATOR + item.nvre;
  };

  isItemSelected = (item) => item.selectable;

  selectionKeyIdentifier = (selectionKey: string) => selectionKey;

  packageNameFromSelectionKey = (selectionKey: string) => {
    const components = selectionKey.split(SELECTION_KEY_SEPARATOR);
    if (components.length === 2) {
      return components[1];
    }

    return selectionKey;
  };

  handleSelection = (items: string[]) => {
    const removed = this.state.selectedPackages.filter((item) => !items.includes(item)).map((item) => [item, false]);
    const added = items.filter((item) => !this.state.selectedPackages.includes(item)).map((item) => [item, true]);
    const data = Object.assign({}, Object.fromEntries(added), Object.fromEntries(removed));
    Network.post(`/rhn/manager/api/sets/${this.props.selectionSet}`, data).catch(Network.showResponseErrorToastr);

    this.setState({ selectedPackages: items });
  };

  render() {
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;
    const listButtons = [
      <div key="list-right-button-1" className="btn-group pull-right">
        <AsyncButton
          disabled={this.state.selectedPackages.length === 0}
          defaultType="btn-danger"
          text={this.props.listActionLabel}
          action={this.confirmAction}
        />
      </div>,
    ];
    const confirmButtonsRight = [
      <div key="confirm-right-button-1" className="btn-group pull-right">
        <AsyncButton text={t("Confirm")} action={this.scheduleAction} />
      </div>,
    ];
    const confirmButtonsLeft = [
      <div key="confirm-left-button-1" className="btn-group pull-left">
        <Button
          id="back-btn"
          className="btn-default"
          icon="fa-chevron-left"
          text={t("Back to list")}
          handler={this.backToList}
        />
      </div>,
    ];
    const listPanel = this.state.confirmAction ? null : (
      <InnerPanel
        title={this.props.listTitle}
        icon={this.props.icon}
        buttons={listButtons}
        summary={this.props.listSummary}
      >
        <Table
          data={this.props.listDataAPI}
          identifier={this.buildSelectionKey}
          initialSortColumnKey="packageName"
          selectable={this.isItemSelected}
          selectedItems={this.state.selectedPackages}
          onSelect={this.handleSelection}
          searchField={<SearchField placeholder={t("Filter by package name")} />}
          emptyText={this.props.listEmptyText}
        >
          <Column
            columnKey="packageName"
            comparator={Utils.sortByText}
            header={t("Package Name")}
            cell={this.packageNameLink}
          />
          {this.props.listColumns}
        </Table>
      </InnerPanel>
    );

    const confirmPanel = this.state.confirmAction ? (
      <InnerPanel
        title={this.props.confirmTitle}
        icon="fa-check-square-o"
        buttons={confirmButtonsRight}
        buttonsLeft={confirmButtonsLeft}
      >
        <ActionSchedule
          earliest={this.state.earliest}
          actionChains={this.props.actionChains}
          onActionChainChanged={this.onActionChainChanged}
          onDateTimeChanged={this.onDateTimeChanged}
          systemIds={[this.props.serverId]}
          actionType={this.props.actionType}
        />
        <Table
          data={this.state.selectedPackages}
          identifier={this.selectionKeyIdentifier}
          initialSortColumnKey="packageName"
          searchField={<SearchField filter={this.searchData} placeholder={t("Filter by package name")} />}
        >
          <Column
            columnKey="packageName"
            comparator={Utils.sortByText}
            header={t("Package Name")}
            cell={this.packageNameFromSelectionKey}
          />
        </Table>
      </InnerPanel>
    ) : null;

    return (
      <div>
        {messages}
        {listPanel}
        {confirmPanel}
      </div>
    );
  }
}
