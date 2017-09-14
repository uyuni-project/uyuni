'use strict';

const React = require("react");
const {Button} = require("../components/buttons");
const { ModalButton, DeleteDialog} = require("../components/dialogs");
const BootstrapPanel = require("../components/panel").BootstrapPanel;
const {Utils} = require("../utils/functions");
const Network = require("../utils/network");
const {Messages} = require("../components/messages");
const MessagesUtils = require("../components/messages").Utils;
const {Table, Column, SearchField} = require("../components/table");

class VirtualHostManagerDetails extends React.Component {

    constructor(props) {
        super(props);

        ["onRefresh", "handleResponseError"]
                .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: []
        };

    }

    handleResponseError(jqXHR) {
        this.setState({
            messages: Network.responseErrorMessage(jqXHR)
        });
    }

    componentWillMount() {
        Network.get("/rhn/manager/api/vhms/" + this.props.data.id + "/nodes")
            .promise.then(data => {
                this.setState({nodes: data.data});
            })
            .catch(this.handleResponseError);
    }

    onRefresh() {
       return Network.post("/rhn/manager/api/vhms/" + this.props.data.id + "/refresh",
                JSON.stringify(this.props.data.id), "application/json").promise.then(data => {
            if (data.success) {
                this.setState({
                    messages: MessagesUtils.info(t("Refreshing the data for this Virtual Host Manager has been triggered."))
                });
            } else {
                this.setState({
                    messages: MessagesUtils.error(data.messages)
                });
            }
        })
        .catch(this.handleResponseError);
    }

    render() {
        return (
        <div>
            { this.state.messages ?
                <Messages items={this.state.messages}/> : null }
            <BootstrapPanel title={t("Properties")}>
                <ConfigParams data={this.props.data}/>
            </BootstrapPanel>
            <BootstrapPanel title={t("Nodes")}>
            { this.state.nodes ?
                <Table
                    data={this.state.nodes}
                    identifier={node => node.type + "_" + node.id}
                    initialSortColumnKey="name"
                    initialItemsPerPage={userPrefPageSize}
                >
                    <Column
                        columnKey="name"
                        comparator={Utils.sortByText}
                        header={t('Name')}
                        cell={(row, criteria) => row.type == "server" ? <a href={"/rhn/systems/details/Overview.do?sid=" + row.id}>{row.name}</a> : row.name }
                    />
                    <Column
                        columnKey="os"
                        comparator={Utils.sortByText}
                        header={t('OS')}
                        cell={(row, criteria) => row.os }
                    />
                    <Column
                        columnKey="arch"
                        comparator={Utils.sortByText}
                        header={t('CPU Arch')}
                        cell={(row, criteria) => row.arch }
                    />
                    <Column
                        columnKey="cpuSockets"
                        comparator={Utils.sortByText}
                        header={t('CPU Sockets')}
                        cell={(row, criteria) => row.cpuSockets }
                    />
                    <Column
                        columnKey="memory"
                        comparator={Utils.sortByText}
                        header={t('RAM (Mb)')}
                        cell={(row, criteria) => row.memory }
                    />
                </Table>
                : null
            }
            </BootstrapPanel>
            <div className="btn-group">
                <Button
                    text={t("Back")}
                    icon="fa-chevron-left"
                    title={t("Back")}
                    className="btn-default"
                    handler={this.props.onCancel}
                />
               <Button
                    text={t("Schedule refresh data")}
                    icon="fa-refresh"
                    title={t("Refresh data from this Virtual Host Manager")}
                    className="btn-default"
                    handler={() => this.onRefresh(this.props.data)}
                />
                <Button
                    text={t("Edit")}
                    icon="fa-edit"
                    title={t("Edit")}
                    className="btn-default"
                    handler={() => this.props.onEdit(this.props.data)}
                />
                <ModalButton
                    text={t("Delete")}
                    icon="fa-trash"
                    title={t("Delete")}
                    target="delete-single-modal"
                    className="btn-default"
                />
            </div>
            <DeleteDialog id="delete-single-modal"
                title={t("Delete Virtual Host Manager")}
                content={
                  <span>
                      {t("Are you sure you want to delete this virtual host manager?")}
                  </span>
                }
                onConfirm={this.props.onDelete}
            />
        </div>
        );
    }
}

function ConfigParams(props) {
    const data = props.data;

    const items = Object.keys(data.config).map(key => {
        return (
            <tr>
                <td>{Utils.capitalize(key)}:</td>
                <td>{data.config[key]}</td>
            </tr>
        );
    });
    if (data.credentials && data.credentials.username) {
        items.push((
           <tr>
               <td>{t("Username")}:</td>
               <td>{data.credentials.username}</td>
           </tr>
       ));
    }

    return (
        <div className="table-responsive">
            <table className="table">
                <tr>
                    <td>{t("Organization")}:</td>
                    <td>{data.orgName}</td>
                </tr>
                <tr>
                    <td>{t("Gatherer module")}:</td>
                    <td>{data.gathererModule}</td>
                </tr>
                {items}
            </table>
        </div>
    );
}

module.exports = {
    VirtualHostManagerDetails: VirtualHostManagerDetails
};
