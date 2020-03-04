/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const { InnerPanel } = require('components/panels/InnerPanel');
const Button = require("components/buttons").Button;
const {Toggler} = require("components/toggler");
const ModalButton = require("components/dialog/ModalButton").ModalButton;
const DeleteDialog = require("components/dialog/DeleteDialog").DeleteDialog;

class RecurringStatesList extends React.Component {

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

    tableBody = () => {
        const elements = [];
        const data = this.props.data;
        if (data !== undefined) {
            data.map(row => {
                elements.push(
                    <tr>
                        <td>
                            <Toggler value={row.active} className="btn" handler={() => {this.props.onToggleActive(row)}} />
                        </td>
                        <td className="text-center">{row.scheduleName}</td>
                        <td className="text-center">{row.cron}</td>
                        {/*<td className="text-center">{row.createdAt + " " + timezone}</td>*/}
                        {this.props.disableCreate ? <td className="text-center">{row.targetType}</td> : null}
                        {/*<td className="text-center">
                         <input type="checkbox" className="checkbox btn-group" checked={row.skipNext === "true"} onChange={() => {this.props.onSkip(row)}}/>
                         </td> } */ }
                        <td className="text-right">
                            <div className="btn-group">
                                <Button
                                    className="btn-default btn-sm"
                                    title={t("Details")}
                                    icon="fa-list"
                                    handler={() => {
                                        this.props.onSelect(row)
                                    }}
                                />
                                <Button
                                    className="btn-default btn-sm"
                                    title={t("Edit")}
                                    icon="fa-edit"
                                    handler={() => {
                                        this.props.onEdit(row)
                                    }}
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
                        </td>
                    </tr>
                );
            });
        }

        return (
            <tbody className="table-content">
            {elements.length > 0 ? elements :
                <tr>
                    <td colSpan="4">
                        <div>{t("No schedules created." + (this.props.disableCreate ? "" : " Use Create to add a schedule."))}</div>
                    </td>
                </tr>
            }
            </tbody>
        );
    };

    render() {
        const createButton = [
            <div className="btn-group pull-right">
                <Button
                    className="btn-default"
                    icon="fa-plus"
                    text={t("Create")}
                    title="Schedule a new Recurring States Action"
                    handler={() => {this.props.onActionChanged("create")}}
                />
            </div>
        ];
        const targetType = this.props.disableCreate ? <th className="text-center">{t("Target Type")}</th> : null;

        return (
            <div>
                <InnerPanel title={t("Recurring States")} icon="spacewalk-icon-salt" buttons={this.props.disableCreate ? null : createButton}>
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <div>
                                <h3>Schedules</h3>
                            </div>
                        </div>
                        <div className="panel-body">
                            <table className="table table-striped">
                                <thead>
                                <tr>
                                    <th>{t("Active")}</th>
                                    <th className="text-center">{t("Schedule Name")}</th>
                                    <th className="text-center">{t("Frequency")}</th>
                                    { /* <th className="text-center">{t("Created at")}</th> */}
                                    {targetType}
                                    { /* <th className="text-center">{t("Skip Next")}</th> */ }
                                    <th className="text-right">{t("Actions")}</th>
                                </tr>
                                </thead>
                                {this.tableBody()}
                            </table>
                            <DeleteDialog
                                id="delete-modal"
                                title={t("Delete Recurring State Schedule")}
                                content={<span>{t("Are you sure you want to delete the selected item?")}</span>}
                                onConfirm={() => this.props.onDelete(this.state.itemToDelete)}
                                onClosePopUp={() => this.selectToDelete(null)}
                            />
                        </div>
                    </div>
                </InnerPanel>
            </div>
        );
    }
}

module.exports = {
    RecurringStatesList: RecurringStatesList
};
