import * as React from "react";
import { InnerPanel } from "components/panels/InnerPanel";
import { Button } from "components/buttons";
import { Toggler } from "components/toggler";
import { ModalButton } from "components/dialog/ModalButton";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { targetTypeToString } from "./recurring-states-utils";

type Props = {
  data?: any;
  disableCreate?: boolean;
  onActionChanged: (arg0: any) => any;
  onToggleActive: (arg0: any) => any;
  onSelect: (arg0: any) => any;
  onEdit: (arg0: any) => any;
  onDelete: (arg0: any) => any;
};

type State = {
  itemsToDelete: any[];
  itemToDelete?: any;
};

class RecurringStatesList extends React.Component<Props, State> {
  constructor(props) {
    super(props);

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
    const buttons = [
      <div className="btn-group pull-right">
        <Button
          className="btn-default"
          icon="fa-plus"
          text={t("Create")}
          title="Schedule a new Recurring States Action"
          handler={() => this.props.onActionChanged("create")}
        />
      </div>,
    ];

    return (
      <InnerPanel
        title={t("Recurring States")}
        icon="spacewalk-icon-salt"
        buttons={this.props.disableCreate ? [] : buttons}
      >
        <div className="panel panel-default">
          <div className="panel-heading">
            <div>
              <h3>Schedules</h3>
            </div>
          </div>
          <div>
            <Table
              data={this.props.data}
              identifier={(action) => action.recurringActionId}
              /* Using 0 to hide table header/footer */
              initialItemsPerPage={this.props.disableCreate ? window.userPrefPageSize : 0}
              emptyText={t(
                "No schedules created." + (this.props.disableCreate ? "" : " Use Create to add a schedule.")
              )}
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
              title={t("Delete Recurring State Schedule")}
              content={t("Are you sure you want to delete the selected item?")}
              onConfirm={() => this.props.onDelete(this.state.itemToDelete)}
              onClosePopUp={() => this.selectToDelete(null)}
            />
          </div>
        </div>
      </InnerPanel>
    );
  }
}

export { RecurringStatesList };
