/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Network = require("utils/network");
const { TopPanel } = require('components/panels/TopPanel');
const {DropdownButton} = require("components/buttons");
const {Messages} = require("components/messages");
const {VirtualHostManagerList} = require("./virtualhostmanager-list");
const {VirtualHostManagerDetails} = require("./virtualhostmanager-details");
const {VirtualHostManagerEdit} = require("./virtualhostmanager-edit");
const MessagesUtils = require("components/messages").Utils;

const hashUrlRegex = /^#\/([^\/]*)(?:\/(.+))?$/;

const msgModuleTypes = {
    "file": t('File-based'),
    "vmware": t('VMWare-based'),
    "kubernetes": t('Kubernetes Cluster')
}

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

        ["deleteSelected", "deleteVhm", "handleBackAction", "handleDetailsAction",
            "handleEditAction", "handleResponseError", "getAvailableModules"]
                .forEach(method => this[method] = this[method].bind(this));
        this.state = {
            vhms: [],
            messages: [],
            availableModules: []
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
                .then(data => this.setState({selected: data.data, action: action}))
        else if (!action) {
            this.getAvailableModules();
            this.getVhmList();
        } else {
            this.setState({action: action, id: id});
        }
        this.clearMessages();
    }

    handleResponseError(jqXHR) {
        this.setState({
            messages: Network.responseErrorMessage(jqXHR)
        });
    }

    clearMessages() {
        this.setState({
            messages: undefined
        });
    }

    getVhmDetails(id, action) {
        return Network.get("/rhn/manager/api/vhms/" + id).promise
             .catch(this.handleResponseError);
    }

    getVhmList() {
        return Network.get("/rhn/manager/api/vhms").promise
            .then(data => this.setState({action: undefined, selected: undefined, vhms: data.data}))
            .catch(this.handleResponseError);
    }

    getAvailableModules() {
        return Network.get("/rhn/manager/api/vhms/modules").promise
            .then(data => this.setState({availableModules: data}))
            .catch(this.handleResponseError);
    }

    deleteSelected() {
        this.deleteVhm(this.state.selected);
    }

    deleteVhm(item) {
        if(!item) return false;
        return Network.del("/rhn/manager/api/vhms/delete/" + item.id)
            .promise.then(data => {
                this.handleBackAction();
                this.setState({
                    messages: MessagesUtils.info("Virtual Host Manager has been deleted.")
                });
            })
            .catch(this.handleResponseError);
    }

    getCreateType() {
        const types = ["file", "vmware", "kubernetes"];
        return types.includes(this.state.id) ? this.state.id : types[0];
    }

    handleBackAction() {
        this.getVhmList().then(data => {
            const loc = window.location;
            history.pushState(null, null, loc.pathname + loc.search);
        });
        this.getAvailableModules();
    }

    handleDetailsAction(row) {
        this.getVhmDetails(row.id).then(data => {
            this.setState({selected: data.data, action: "details"});
            history.pushState(null, null, "#/details/" + row.id);
        });
    }

    handleEditAction(row) {
        this.getVhmDetails(row.id).then(data => {
            this.setState({selected: data.data, action: "edit"});
            history.pushState(null, null, "#/edit/" + row.id);
        });
    }

    getPanelTitle() {
        const action = this.state.action;

        if (action === "details") {
            return this.state.selected.label;
        } else if (action === "create") {
            return t("Add a {0}", msgModuleTypes[this.state.id] + " " + t("Virtual Host Manager"));
        } else {
            return t("Virtual Host Managers");
        }
    }

    render() {
        const panelButtons = <div className="pull-right btn-group">
            { (this.state.action !== "create" && this.state.action !== "edit" && this.state.action !== "details") &&
                <DropdownButton
                    text={t("Create")}
                    icon="fa-plus"
                    title={t("Add a virtual host manager")}
                    className="btn-default"
                    items={this.state.availableModules.map(name =>
                        <a href={"#/create/" + name.toLocaleLowerCase()}>{msgModuleTypes[name.toLocaleLowerCase()]}</a>
                    )}
                />
            }
        </div>;
        return (
            <TopPanel
                title={this.getPanelTitle()}
                icon="spacewalk-icon-virtual-host-manager"
                button={panelButtons}
                helpUrl="/docs/reference/systems/virtual-host-managers.html"
            >
                { this.state.messages ?
                     <Messages items={this.state.messages}/> :
                     null
                }
                { this.state.action == 'details' ?
                    <VirtualHostManagerDetails data={this.state.selected} onCancel={this.handleBackAction} onEdit={this.handleEditAction} onDelete={this.deleteSelected}/>
                : this.state.action == 'create' ?
                    <VirtualHostManagerEdit type={this.getCreateType()} onCancel={this.handleBackAction}/>
                : this.state.action == 'edit' ?
                    <VirtualHostManagerEdit item={this.state.selected} type={this.state.selected.gathererModule} onCancel={this.handleBackAction}/>
                :
                    <VirtualHostManagerList data={this.state.vhms} onSelect={this.handleDetailsAction} onEdit={this.handleEditAction} onDelete={this.deleteVhm}/>
                }
            </TopPanel>
        );
    }
}

ReactDOM.render(<VirtualHostManager/>, document.getElementById('virtual-host-managers'));
