/* eslint-disable */
'use strict';

const React = require("react");
const Utils = require("utils/functions").Utils;
const {Table, Column, SearchField} = require("components/table");
const Button = require("components/buttons").Button;
const ModalButton = require("components/dialog/ModalButton").ModalButton;
const DeleteDialog = require("components/dialog/DeleteDialog").DeleteDialog;

class VirtualHostManagerList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            itemsToDelete: []
        };
    }

    selectToDelete(item) {
        this.setState({
            itemToDelete: item
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
                emptyText={t('No Virtual Host Managers.')}
            >
                <Column
                    columnKey="label"
                    comparator={Utils.sortByText}
                    header={t('Label')}
                    cell={(row, criteria) => <a href={"#/details/" + row.id}><i className="fa spacewalk-icon-virtual-host-manager"/>{row.label}</a>}
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
                                    onClick={i => this.selectToDelete(i)}
                                />
                            </div>
                        );
                    }}
                />
            </Table>
            <DeleteDialog id="delete-modal"
                title={t("Delete Virtual Host Manager")}
                content={
                  <span>
                      {t("Are you sure you want to delete the selected item?")}
                  </span>
                }
                onConfirm={() => this.props.onDelete(this.state.itemToDelete)}
                onClosePopUp={() => this.selectToDelete(null)}
            />
        </div>
        );
    }
}

module.exports = {
    VirtualHostManagerList: VirtualHostManagerList
};
