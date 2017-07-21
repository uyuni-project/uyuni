'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const DropdownButton = require("../components/buttons").DropdownButton;
const Network = require("../utils/network");
const VirtualHostManagerList = require("./virtualhostmanager-list").VirtualHostManagerList;
const VirtualHostManagerDetails = require("./virtualhostmanager-details").VirtualHostManagerDetails;
const VirtualHostManagerEdit = require("./virtualhostmanager-edit").VirtualHostManagerEdit;

const hashUrlRegex = /^#\/([^\/]*)(?:\/(.+))?$/;

function getHashId() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[2] : undefined;
}

function getHashAction() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[1] : undefined;
}

class VirtualHostManager extends React.Component {

    constructor(props) {
        super(props);

        ["deleteVhms", "deleteSingle", "handleBackAction", "handleDetailsAction",
            "handleEditAction"]
                .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            vhms: [],
            messages: []
        };
    }

    componentDidMount() {
        this.updateView(getHashAction(), getHashId());
        window.addEventListener("popstate", () => {
            this.updateView(getHashAction(), getHashId());
        });
    }

    updateView(action, id) {
        let async;
        if ((action === "edit" || action === "details") && id)
            this.getVhmDetails(id, action)
                .then(data => this.setState({selected: data, action: action}));
        else if (!action) {
            this.getVhmList();
        } else {
            this.setState({action: action, id: id});
        }
        this.clearMessages();
    }

    clearMessages() {
        this.setState({
            messages: undefined
        });
    }

    getVhmDetails(id, action) {
        return Network.get("/rhn/manager/api/vhms/" + id).promise;
    }

    getVhmList() {
        return Network.get("/rhn/manager/api/vhms").promise
            .then(data => this.setState({action: undefined, selected: undefined, vhms: data}));
    }

    deleteVhms(idList) {
        return Network.post("/rhn/manager/api/vhms/delete",
                JSON.stringify(idList), "application/json").promise.then(data => {
            if (data.success) {
                this.setState({
                    messages: <Messages items={[{severity: "success", text: msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"]}]}/>,
                    vhms: this.state.vhms.filter(vhm => !idList.includes(vhm.id))
                });
            } else {
                this.setState({
                    messages: <Messages items={state.messages.map(msg => {
                        return {severity: "error", text: msgMap[msg]};
                    })}/>
                });
            }
        }).promise;
    }

    getCreateType() {
        const types = ["file", "vmware", "kubernetes"];
        return types.includes(this.state.id) ? this.state.id : types[0];
    }

    deleteSingle() {
        if(!this.state.selected) return false;
        this.deleteVhms([this.state.selected.id]).then(() => this.handleBackAction());
    }

    handleBackAction() {
        this.getVhmList().then(data => {
            const loc = window.location;
            history.pushState(null, null, loc.pathname + loc.search);
        });
    }

    handleDetailsAction(row) {
        this.getVhmDetails(row.id).then(data => {
            this.setState({selected: data, action: "details"});
            history.pushState(null, null, "#/details/" + row.id);
        });
    }

    handleEditAction(row) {
        this.getVhmDetails(row.id).then(data => {
            this.setState({selected: data, action: "edit"});
            history.pushState(null, null, "#/edit/" + row.id);
        });
    }

    getPanelTitle() {
        const action = this.state.action;

        if (action === "details") {
            return this.state.selected.label;
        } else if (action === "create") {
            const types = {
                "file": t("File-based Virtual Host Manager"),
                "vmware": t("VMware-based Virtual Host Manager"),
                "kubernetes": t("Kubernetes Cluster")
            };

            return t("Add a {0}", types[this.state.id] || types[0]);
        } else {
            return t("Virtual Host Managers");
        }
    }

    render() {
        const panelButtons = <div className="pull-right btn-group">
            { this.state.action !== "create" &&
                <DropdownButton
                    text={t("Create")}
                    icon="fa-plus"
                    title={t("Add a virtual host manager")}
                    className="btn-default"
                    items={[
                        <a href="#/create/file">{t('File-based')}</a>,
                        <a href="#/create/vmware">{t('VMWare-based')}</a>,
                        <a href="#/create/kubernetes">{t('Kubernetes Cluster')}</a>
                    ]}
                />
            }
        </div>;
        return (
            <Panel
                title={this.getPanelTitle()}
                icon="spacewalk-icon-virtual-host-manager"
                button={panelButtons}
            >
                {this.state.messages}
                { this.state.action == 'details' ?
                    <VirtualHostManagerDetails data={this.state.selected} onCancel={this.handleBackAction} onEdit={this.handleEditAction} onDelete={this.deleteSingle}/>
                : this.state.action == 'create' ?
                    <VirtualHostManagerEdit type={this.getCreateType()}/>
                : this.state.action == 'edit' ?
                    <VirtualHostManagerEdit item={this.state.selected} type={this.state.selected.gathererModule}/>
                :
                    <VirtualHostManagerList data={this.state.vhms} onSelect={this.handleDetailsAction} onEdit={this.handleEditAction} onDelete={this.deleteVhms}/>
                }
            </Panel>
        );
    }
}

ReactDOM.render(<VirtualHostManager/>, document.getElementById('virtual-host-managers'));