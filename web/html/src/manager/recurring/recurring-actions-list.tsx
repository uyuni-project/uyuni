import * as React from "react";

import { pageSize } from "core/user-preferences";

import { Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { InnerPanel } from "components/panels/InnerPanel";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { Toggler } from "components/toggler";

import { targetNameLink, targetTypeToString } from "./recurring-actions-utils";

type Props = {
  data?: any;
  isFilteredList?: boolean;
  onActionChanged: (arg0: any) => any;
  onToggleActive: (arg0: any) => any;
  onSelect: (arg0: any) => any;
  onEdit: (arg0: any) => any;
  onDelete: (arg0: any, arg1: React.RefObject<any>) => any;
};

type State = {
  itemsToDelete: any[];
  itemToDelete?: any;
};

class RecurringActionsList extends React.Component<Props, State> {
  tableRef: React.RefObject<any>;
  constructor(props) {
    super(props);
    this.tableRef = React.createRef();

    this.state = {
      itemsToDelete: [],
    };
  }

  selectToDelete(item) {
    this.setState({
      itemToDelete: item,
    });
  }

  render() {
    const disableCreate = !this.props.isFilteredList;
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
      <InnerPanel
        title={t("Recurring Actions")}
        icon="spacewalk-icon-salt"
        buttons={disableCreate ? [] : buttons}
        summary={
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
        // We only want to display the help icon in the 'Schedule > Recurring Actions' page so we use disableCreate as
        // an indicator whether we currently render this page
        helpUrl={disableCreate ? "reference/schedule/recurring-actions.html" : ""}
      >
        <div className="panel panel-default">
          <div className="panel-heading">
            <div>
              <h3>Schedules</h3>
            </div>
          </div>
          <div>
            <Table
              selectable={false}
              data={this.props.isFilteredList ? this.props.data : "/rhn/manager/api/recurringactions"}
              identifier={(action) => action.recurringActionId}
              /* Using 0 to hide table header/footer */
              initialItemsPerPage={disableCreate ? pageSize : 0}
              emptyText={t("No schedules created." + (disableCreate ? "" : " Use Create to add a schedule."))}
              ref={this.tableRef}
            >
              <Column
                columnKey="active"
                header={t("Active")}
                cell={(row) => (
                  <Toggler value={row.active} className="btn" handler={() => this.props.onToggleActive(row)} />
                )}
              />
              <Column
                columnClass="text-center"
                headerClass="text-center"
                columnKey="scheduleName"
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
                columnKey="targetType"
                header={t("Target Type")}
                cell={(row) => targetTypeToString(row.targetType)}
              />
              <Column
                columnClass="text-center"
                headerClass="text-center"
                columnKey="targetName"
                header={t("Target Name")}
                cell={(row) => targetNameLink(row.targetName, row.targetType, row.targetId)}
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
                      icon="fa-edit"
                      handler={() => {
                        this.props.onEdit(row);
                      }}
                    />
                    <ModalButton
                      className="btn-default btn-sm"
                      title={t("Delete")}
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
              onConfirm={() => this.props.onDelete(this.state.itemToDelete, this.tableRef)}
              onClosePopUp={() => this.selectToDelete(null)}
            />
          </div>
        </div>
      </InnerPanel>
    );
  }
}

export { RecurringActionsList };
