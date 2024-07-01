import * as React from "react";

import { pageSize } from "core/user-preferences";

import { Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { IconTag } from "components/icontag";
import { Utils as MessagesUtils } from "components/messages";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { Toggler } from "components/toggler";
import { HelpLink } from "components/utils";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { inferEntityParams, isReadOnly, targetNameLink, targetTypeToString } from "./recurring-actions-utils";
import { RecurringActionsSearch } from "./search/recurring-actions-search";

type Props = {
  onSetMessages: (arg0: any) => any;
  isFilteredList?: boolean;
  onActionChanged: (arg0: any) => any;
  onSelect: (arg0: any) => any;
  onEdit: (arg0: any) => any;
  onError: (arg0: any) => any;
  onDeleteError: (arg0: any) => any;
};

type State = {
  itemsToDelete: any[];
  itemToDelete?: any;
  schedules: any[];
};

class RecurringActionsList extends React.Component<Props, State> {
  tableRef: React.RefObject<any>;
  constructor(props) {
    super(props);
    this.tableRef = React.createRef();
    this.state = {
      schedules: [],
      itemsToDelete: [],
    };
  }

  componentDidMount = () => {
    const { isFilteredList } = this.props;
    // only fetch data if it is a filtered list, otherwise the Table component will fetch the data.
    if (isFilteredList) {
      this.getRecurringScheduleList();
    }
  };

  getRecurringScheduleList = () => {
    const entityParams = inferEntityParams();
    const endpoint = "/rhn/manager/api/recurringactions" + entityParams;
    return Network.get(endpoint)
      .then((schedules) => {
        this.setState({
          schedules: schedules,
        });
      })
      .catch(this.props.onError);
  };

  deleteSchedule = (item, tableRef) => {
    return Network.del("/rhn/manager/api/recurringactions/" + item.recurringActionId + "/delete")
      .then((_) => {
        this.props.onSetMessages(MessagesUtils.info("Schedule '" + item.scheduleName + "' has been deleted."));
        this.getRecurringScheduleList();
        if (tableRef) {
          tableRef.current.refresh();
        }
      })
      .catch(this.props.onDeleteError);
  };

  toggleActive = (schedule) => {
    Object.assign(schedule, { active: !schedule.active });
    return Network.post("/rhn/manager/api/recurringactions/save", schedule)
      .then((_) => {
        this.props.onSetMessages(MessagesUtils.info(t("Schedule successfully updated.")));
      })
      .catch(this.props.onError);
  };

  selectToDelete(item) {
    this.setState({
      itemToDelete: item,
    });
  }

  render() {
    const { isFilteredList } = this.props;
    const disableCreate = !isFilteredList;
    const emptyListText = `No schedules created.${disableCreate ? "" : " Use Create to add a schedule."}`;
    const buttons = [
      <div className="btn-group pull-right">
        <Button
          className="btn-default"
          icon="fa-plus"
          text={t("Create")}
          title="Schedule a new Recurring Action"
          handler={() => this.props.onActionChanged("create")}
        />
      </div>,
    ];

    return (
      <>
        <h1>
          <IconTag type="spacewalk-icon-salt" />
          {t(" Recurring Actions ")}
          <HelpLink url="reference/schedule/recurring-actions.html" />
        </h1>
        <p>
          {
            <>
              <p>{t("The following recurring actions have been created.")}</p>
              {disableCreate ? (
                <p>
                  {t(
                    "To create new recurring actions head to the system, group or organization you want to create the action for."
                  )}
                </p>
              ) : null}
            </>
          }
        </p>
        {/* We only want to display the help icon in the 'Schedule > Recurring Actions' page so we use disableCreate as */}
        {/* an indicator whether we currently render this page */}
        <div className="pull-right btn-group">{disableCreate ? [] : buttons}</div>
        <h3>Schedules</h3>
        <Table
          selectable={false}
          data={isFilteredList ? this.state.schedules : "/rhn/manager/api/recurringactions"}
          identifier={(action) => action.recurringActionId}
          /* Using 0 to hide table header/footer */
          initialItemsPerPage={disableCreate ? pageSize : 0}
          emptyText={t(emptyListText)}
          searchField={<RecurringActionsSearch />}
          ref={this.tableRef}
        >
          <Column
            columnKey="active"
            header={t("Active")}
            cell={(row) => (
              <Toggler
                value={row.active}
                disabled={isReadOnly(row)}
                className="btn"
                handler={() => (isReadOnly(row) ? null : this.toggleActive(row))}
              />
            )}
          />
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey={isFilteredList ? "scheduleName" : "schedule_name"}
            comparator={Utils.sortByText}
            header={t("Schedule Name")}
            cell={(row) => row.scheduleName}
          />
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey="frequency"
            header={t("Frequency")}
            cell={(row) => row.cron}
          />
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey={isFilteredList ? "targetType" : "target_type"}
            comparator={Utils.sortByText}
            header={t("Target Type")}
            cell={(row) => targetTypeToString(row.targetType)}
          />
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey={isFilteredList ? "targetName" : "target_name"}
            comparator={Utils.sortByText}
            header={t("Target Name")}
            cell={(row) => targetNameLink(row.targetName, row.targetType, row.targetId, row.targetAccessible)}
          />
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey={isFilteredList ? "actionType" : "action_type"}
            comparator={Utils.sortByText}
            header={t("Action Type")}
            cell={(row) => row.actionTypeDescription}
          />
          <Column
            columnClass="text-right"
            headerClass="text-right"
            header={t("Actions")}
            cell={(row) => (
              <div className="btn-group">
                <Button
                  className="btn-default btn-sm"
                  title={t("Details")}
                  icon="fa-list"
                  handler={() => {
                    this.props.onSelect(row);
                  }}
                />
                <Button
                  className="btn-default btn-sm"
                  title={t("Edit")}
                  disabled={isReadOnly(row)}
                  icon="fa-edit"
                  handler={() => {
                    this.props.onEdit(row);
                  }}
                />
                <ModalButton
                  className="btn-default btn-sm"
                  title={t("Delete")}
                  disabled={isReadOnly(row)}
                  icon="fa-trash"
                  target="delete-modal"
                  item={row}
                  onClick={(i) => this.selectToDelete(i)}
                />
              </div>
            )}
          />
        </Table>
        <DeleteDialog
          id="delete-modal"
          title={t("Delete Recurring Action Schedule")}
          content={t("Are you sure you want to delete the selected item?")}
          onConfirm={() => this.deleteSchedule(this.state.itemToDelete, this.tableRef)}
          onClosePopUp={() => this.selectToDelete(null)}
        />
      </>
    );
  }
}

export { RecurringActionsList };
