'use strict';

const React = require("react");
const Utils = require("../utils/functions").Utils;
const {Table, Column, SearchField} = require("../components/table");
const Button = require("../components/buttons").Button;
const ModalButton = require("../components/dialogs").ModalButton;
const DeleteDialog = require("../components/dialogs").DeleteDialog;

class VirtualHostManagerList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            itemsToDelete: []
        };
    }

    selectToDelete(items) {
        this.setState({
            itemsToDelete: items
        });
    }

    render() {
        return (
        <div>
            <Table
                data={this.props.data}
                identifier={vhm => vhm.id}
                initialSortColumnKey="label"
                initialItemsPerPage={userPrefPageSize}
            >
                <Column
                    columnKey="label"
                    comparator={Utils.sortByText}
                    header={t('Label')}
                    cell={(row, criteria) => <a href={"#/details/" + row.id}><i className="fa spacewalk-icon-virtual-host-manager"/> {row.label}</a>}
                />
                <Column
                    columnKey="gathererModule"
                    comparator={Utils.sortByText}
                    header={t('Gatherer module')}
                    cell={(row, criteria) => row.gathererModule}
                />
                <Column
                    columnKey="org"
                    comparator={Utils.sortByText}
                    header={t('Organization')}
                    cell={(row, criteria) => row.orgName}
                />
                <Column
                    width="10%"
                    columnClass="text-right"
                    headerClass="text-right"
                    header={t('Actions')}
                    cell={(row, criteria) => {
                        return (
                            <div className="btn-group">
                                <Button
                                    className="btn-default btn-sm"
                                    title={t("Details")}
                                    icon="fa-list"
                                    handler={() => {this.props.onSelect(row)}}
                                />
                                <Button
                                    className="btn-default btn-sm"
                                    title={t("Edit")}
                                    icon="fa-edit"
                                    handler={() => {this.props.onEdit(row)}}
                                />
                                <ModalButton
                                    className="btn-default btn-sm"
                                    title={t("Delete")}
                                    icon="fa-trash"
                                    target="delete-modal"
                                    item={row}
                                    onClick={i => this.selectToDelete([i])}
                                />
                            </div>
                        );
                    }}
                />
            </Table>
            <DeleteDialog id="delete-modal"
                title={this.state.itemsToDelete.length == 1 ? t("Delete Virtual Host Manager") : t("Delete Virtual Host Managers")}
                content={
                  <span>
                      {this.state.itemsToDelete.length == 1 ? t("Are you sure you want to delete the selected item?") : t("Are you sure you want to delete selected items? ({0} items selected)", this.state.itemsToDelete.length)}
                  </span>
                }
                onConfirm={(item) => this.props.onDelete(this.state.itemsToDelete.map((item) => item.id))}
                onClosePopUp={() => this.selectToDelete([])}
            />
        </div>
        );
    }
}

module.exports = {
    VirtualHostManagerList: VirtualHostManagerList
};