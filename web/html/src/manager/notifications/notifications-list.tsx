import * as React from "react";

import { AsyncButton, LinkButton } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { DEPRECATED_Select, Form } from "components/input";
import { Messages as MessageContainer, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { stringToReact } from "utils";
import { Utils } from "utils/functions";
import Network from "utils/network";

import { NotificationComparators } from "./comparators";
import { Notification, NotificationType, Severity } from "./types";

// Defines the type of data displayed in the table
enum DataType {
  // Show only unread notifications
  Unread = "data-unread",

  // Show all notifications
  All = "data-all",
}

type Props = {
  notificationTypes: NotificationType[];
};

type State = {
  serverData: Notification[] | undefined;
  dataType: DataType;
  loading: boolean;
  messages: MessageType[];
  selectedItems: number[];
  popupItem: Notification | undefined;
  typeCriteria: string[];
};

export class NotificationList extends React.Component<Props, State> {
  private readonly typeMap: Map<string, string>;

  public constructor(props: Props) {
    super(props);

    // Map to convert from label to description
    this.typeMap = this.props.notificationTypes.reduce(
      (map, item) => map.set(item.label, item.description),
      new Map<string, string>()
    );

    this.state = {
      serverData: undefined,
      dataType: window.location.hash === "#data-all" ? DataType.All : DataType.Unread,
      loading: true,
      messages: [],
      selectedItems: [],
      popupItem: undefined,
      typeCriteria: [],
    };
  }

  public componentDidMount(): void {
    this.refreshServerData();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>): void {
    if (prevState.dataType !== this.state.dataType) {
      this.refreshServerData();
    }
  }

  public render(): React.ReactNode {
    return (
      <TopPanel title={t("Notification Messages")} icon="fa-envelope">
        <MessageContainer items={this.state.messages} />

        {this.state.serverData && (
          <>
            <p>{t("The server has collected the following notification messages.")}</p>

            {this.renderTabs()}

            <Table
              data={this.filterDataByType(this.state.serverData)}
              identifier={(row: Notification) => row.id}
              initialSortColumnKey="created"
              initialSortDirection={-1}
              loading={this.state.loading}
              selectable
              selectedItems={this.state.selectedItems}
              onSelect={(items) => this.setState({ selectedItems: items })}
              searchField={
                <SearchField
                  filter={(datum, criteria) => this.searchData(datum, criteria)}
                  placeholder={t("Filter by summary")}
                />
              }
              additionalFilters={this.renderFilters()}
              titleButtons={[
                <div className="btn-group" key="notification-msg-btn">
                  <AsyncButton
                    key="refresh"
                    id="reload"
                    icon="fa-refresh"
                    text={t("Refresh")}
                    action={() => this.refreshServerData()}
                  />
                  <AsyncButton
                    key="delete-messages"
                    id="delete-selected-messages"
                    icon="fa-trash"
                    text={t("Delete")}
                    action={() => this.deleteNotifications(this.state.selectedItems)}
                    disabled={this.state.selectedItems.length === 0}
                  />
                  <AsyncButton
                    key="mark-as-read"
                    id="mark-as-read"
                    icon="fa-check-circle"
                    text={t("Mark as Read")}
                    action={() => this.updateReadStatus(this.state.selectedItems, true)}
                    disabled={this.state.selectedItems.length === 0}
                  />
                </div>,
              ]}
            >
              <Column
                columnKey="severity"
                comparator={NotificationComparators.sortBySeverity}
                header={t("Severity")}
                cell={(row) => this.decodeIconBySeverity(row["severity"])}
              />
              <Column
                columnKey="type"
                comparator={NotificationComparators.sortByType(this.typeMap)}
                header={t("Type")}
                cell={(row: Notification) => this.typeMap.get(row.type)}
              />
              <Column
                columnKey="summary"
                comparator={Utils.sortByText}
                header={t("Summary")}
                cell={(row: Notification) => this.renderSummary(row)}
              />
              <Column
                columnKey="created"
                comparator={Utils.sortByDate}
                header={t("Created")}
                cell={(row) => row["created"]}
              />
              <Column columnKey="action" header={t("Action")} cell={(row) => this.renderReaction(row)} />
              <Column
                columnKey="read"
                comparator={NotificationComparators.sortByStatus}
                header={t("Read|Delete")}
                cell={(row) => this.renderItemActions(row)}
              />
            </Table>
            {this.state.popupItem !== undefined && (
              <Dialog
                id="notifications-popup-dialog"
                isOpen={true}
                onClose={() => this.setState({ popupItem: undefined })}
                title={stringToReact(this.state.popupItem.summary)}
                content={stringToReact(this.state.popupItem.details || "")}
              />
            )}
          </>
        )}
      </TopPanel>
    );
  }

  private renderFilters(): React.ReactNode[] {
    return [
      <div key="typeFilter" className="multiple-select-wrapper table-input-search">
        {/* TODO: Remove this <Form> wrapper once https://github.com/SUSE/spacewalk/issues/14250 is implemented */}
        <Form>
          <DEPRECATED_Select
            name="type-criteria"
            placeholder={t("Filter by type")}
            options={this.props.notificationTypes}
            getOptionLabel={(item: NotificationType) => item.description}
            getOptionValue={(item: NotificationType) => item.label}
            isMulti
            onChange={(_name, typeCriteria) => this.setState({ typeCriteria })}
          />
        </Form>
      </div>,
    ];
  }

  private renderTabs(): React.ReactNode {
    return (
      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          <li className={this.state.dataType === DataType.Unread ? "active" : ""}>
            <a href="#data-unread" onClick={() => this.setState({ dataType: DataType.Unread })}>
              {t("Unread Messages")}
            </a>
          </li>
          <li className={this.state.dataType === DataType.All ? "active" : ""}>
            <a href="#data-all" onClick={() => this.setState({ dataType: DataType.All })}>
              {t("All Messages")}
            </a>
          </li>
        </ul>
      </div>
    );
  }

  private renderSummary(data: Notification): React.ReactNode {
    return (
      <span className="align-middle" style={{ whiteSpace: "pre" }}>
        {stringToReact(data.summary)}
        {data.details && (
          <LinkButton
            className="btn-link px-2 py-0"
            handler={() => this.setState({ popupItem: data })}
            text={"[" + t("show details") + "]"}
          />
        )}
      </span>
    );
  }

  private renderReaction(data: Notification): React.ReactNode {
    if (!data.actionable) {
      return <></>;
    }

    return (
      <AsyncButton
        id="reaction"
        icon="fa-rotate-right fa-1-5x"
        text={t("Retry")}
        action={() => this.onRetry(data.id)}
      />
    );
  }

  private renderItemActions(row: Notification): React.ReactNode {
    return (
      <div className="btn-group">
        <AsyncButton
          id="updateReadStatus"
          icon={(row.read ? "spacewalk-icon-envelope-open-o" : "fa-envelope text-primary") + " fa-1-5x"}
          title={row.read ? t("Flag as Unread") : t("Flag as Read")}
          action={() => this.updateReadStatus([row.id], !row.read)}
        />
        <AsyncButton
          id="delete"
          icon="fa-trash fa-1-5x"
          title={t("Delete Notification")}
          action={() => this.deleteNotifications([row.id])}
        />
      </div>
    );
  }

  private async refreshServerData(): Promise<void> {
    const dataType = this.state.dataType;
    this.setState({ loading: true });

    try {
      const response = await Network.get(`/rhn/manager/notification-messages/${dataType}`);

      this.setState({
        serverData: response.data,
        loading: false,
        messages: [],
        selectedItems: [],
      });
    } catch (err: any) {
      this.setState({ loading: false, messages: Network.responseErrorMessage(err) });
    }
  }

  // Filter the notifications with the current type filter
  private filterDataByType(data: Notification[]): Notification[] {
    if (this.state.typeCriteria.length === 0) {
      return data;
    }

    return data.filter((p) => this.state.typeCriteria.includes(p.type));
  }

  // Set the notification read status to the specified value
  private async updateReadStatus(messageIds: number[], flagAsRead: boolean): Promise<void> {
    try {
      await Network.post("/rhn/manager/notification-messages/update-messages-status", { messageIds, flagAsRead });

      this.setState((prevState) => ({
        // serverData = prev serverData without those are changed + those changed with the changes
        serverData: prevState.serverData
          ?.filter((m) => !messageIds.includes(m.id))
          .concat(
            prevState.serverData.filter((m) => messageIds.includes(m.id)).map((m) => ({ ...m, read: flagAsRead }))
          ),
        messages: MessagesUtils.success(
          messageIds.length > 1
            ? t("{number} messages status updated successfully", { number: messageIds.length })
            : t("Message read status updated successfully")
        ),
      }));
    } catch (err: any) {
      this.setState({ messages: Network.responseErrorMessage(err) });
    }
  }

  private async deleteNotifications(messageIds: number[]): Promise<void> {
    try {
      await Network.post("/rhn/manager/notification-messages/delete", messageIds);

      this.setState((prevState) => ({
        serverData: prevState.serverData?.filter((m) => !messageIds.includes(m.id)),
        selectedItems: prevState.selectedItems.filter((m) => !messageIds.includes(m)),
        messages: MessagesUtils.success(
          messageIds.length > 1
            ? t("{number} messages deleted successfully", { number: messageIds.length })
            : t("Message deleted successfully")
        ),
      }));
    } catch (err: any) {
      this.setState({ messages: Network.responseErrorMessage(err) });
    }
  }

  private decodeIconBySeverity(severity: Severity): React.ReactNode {
    switch (severity) {
      case Severity.Info:
        return (
          <span>
            <i className="fa fa-info-circle" title={t("Info")}></i>
            {t("Info")}
          </span>
        );
      case Severity.Warning:
        return (
          <span>
            <i className="fa fa-exclamation-triangle text-warning" title={t("Warning")}></i>
            {t("Warning")}
          </span>
        );
      case Severity.Error:
        return (
          <span>
            <i className="fa fa-times-circle-o text-danger" title={t("Error")}></i>
            {t("Error")}
          </span>
        );
    }
  }

  private searchData(datum: Notification, criteria?: string): boolean {
    if (criteria) {
      return datum.summary.toLowerCase().includes(criteria.toLowerCase());
    }

    return true;
  }

  private async onRetry(notificationId: number): Promise<void> {
    try {
      const response = await Network.post(`/rhn/manager/notification-messages/retry/${notificationId}`);
      this.setState({ messages: MessagesUtils.success(response.messages) });
    } catch (err: any) {
      this.setState({ messages: Network.responseErrorMessage(err) });
    }
  }
}
