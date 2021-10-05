import * as React from "react";
import { Utils } from "utils/functions";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { Button } from "components/buttons";
import { ModalButton } from "components/dialog/ModalButton";
import { DeleteDialog } from "components/dialog/DeleteDialog";

type Props = {
  data?: any;
  onSelect: (row: any) => any;
  onEdit: (row: any) => any;
  onDelete: (item: any) => any;
};

type State = {
  itemToDelete?: any;
};

class VirtualHostManagerList extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  selectToDelete(item) {
    this.setState({
      itemToDelete: item,
    });
  }

  render() {
    return (
      <div>
        <Table
          data={this.props.data}
          identifier={(vhm) => vhm.id}
          initialSortColumnKey="label"
          initialItemsPerPage={window.userPrefPageSize}
          emptyText={t("No Virtual Host Managers.")}
        >
          <Column
            columnKey="label"
            comparator={Utils.sortByText}
            header={t("Label")}
            cell={(row, criteria) => (
              <a data-senna-off href={"#/details/" + row.id}>
                <i className="fa spacewalk-icon-virtual-host-manager" />
                {row.label}
              </a>
            )}
          />
          <Column
            columnKey="gathererModule"
            comparator={Utils.sortByText}
            header={t("Gatherer module")}
            cell={(row, criteria) => row.gathererModule}
          />
          <Column
            columnKey="org"
            comparator={Utils.sortByText}
            header={t("Organization")}
            cell={(row, criteria) => row.orgName}
          />
          <Column
            width="10%"
            columnClass="text-right"
            headerClass="text-right"
            header={t("Actions")}
            cell={(row, criteria) => {
              return (
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
              );
            }}
          />
        </Table>
        <DeleteDialog
          id="delete-modal"
          title={t("Delete Virtual Host Manager")}
          content={<span>{t("Are you sure you want to delete the selected item?")}</span>}
          onConfirm={() => this.props.onDelete(this.state.itemToDelete)}
          onClosePopUp={() => this.selectToDelete(null)}
        />
      </div>
    );
  }
}

export { VirtualHostManagerList };
