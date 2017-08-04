'use strict';

const React = require("react");
const Button = require("../components/buttons").Button;
const ModalButton = require("../components/dialogs").ModalButton;
const DeleteDialog = require("../components/dialogs").DeleteDialog;
const BootstrapPanel = require("../components/panel").BootstrapPanel;
const {Utils} = require("../utils/functions");
const Network = require("../utils/network");
const {Messages} = require("../components/messages");

class VirtualHostManagerDetails extends React.Component {

    constructor(props) {
        super(props);

        ["onRefresh"]
                .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            messages: []
        };

    }

    onRefresh() {
       return Network.post("/rhn/manager/api/vhms/" + this.props.data.id + "/refresh",
                JSON.stringify(this.props.data.id), "application/json").promise.then(data => {
            if (data.success) {
                this.setState({
                    messages: <Messages items={[{severity: "success",
                        text: "Refreshing the data for this Virtual Host Manager has been triggered."}]}/>
                });
            } else {
                this.setState({
                    messages: <Messages items={state.messages.map(msg => {
                        return {severity: "error", text: msg};
                    })}/>
                });
            }
        })
        .catch((err) => {
            console.log(err);
            this.setState({
                messages: errorMessages(["Server error"])
            });
        });
    }

    render() {
        return (
        <div>
            {this.state.messages}
            <BootstrapPanel title={t("Properties")}>
                <ConfigParams data={this.props.data}/>
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