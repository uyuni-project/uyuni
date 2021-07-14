import * as React from "react";
import Network from "utils/network";
import { TopPanel } from "components/panels/TopPanel";
import { DropdownButton } from "components/buttons";
import { Messages } from "components/messages";
import { VirtualHostManagerList } from "./virtualhostmanager-list";
import { VirtualHostManagerDetails } from "./virtualhostmanager-details";
import { VirtualHostManagerEdit } from "./virtualhostmanager-edit";
import { Utils as MessagesUtils } from "components/messages";
import SpaRenderer from "core/spa/spa-renderer";

const hashUrlRegex = /^#\/([^\/]*)(?:\/(.+))?$/;

const msgModuleTypes = {
  file: t("File-based"),
  vmware: t("VMWare-based"),
  kubernetes: t("Kubernetes Cluster"),
  amazonec2: t("Amazon EC2"),
  googlece: t("Google Compute Engine"),
  azure: t("Azure"),
  nutanixahv: t("Nutanix AHV"),
};

function getHashId() {
  const match = window.location.hash.match(hashUrlRegex);
  return match ? match[2] : undefined;
}

function getHashAction() {
  const match = window.location.hash.match(hashUrlRegex);
  return match ? match[1] : undefined;
}

type Props = {};

type State = {
  vhms: any[];
  messages?: any[];
  availableModules: any[];
  selected?: any;
  action?: any;
  id?: any;
};

class VirtualHostManager extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    [
      "deleteSelected",
      "deleteVhm",
      "handleBackAction",
      "handleDetailsAction",
      "handleEditAction",
      "handleResponseError",
      "getAvailableModules",
    ].forEach(method => (this[method] = this[method].bind(this)));
    this.state = {
      vhms: [],
      messages: [],
      availableModules: [],
    };
  }

  componentDidMount() {
    this.updateView(getHashAction(), getHashId());
    window.addEventListener("popstate", () => {
      this.updateView(getHashAction(), getHashId());
    });
  }

  updateView(action, id) {
    if ((action === "edit" || action === "details") && id)
      this.getVhmDetails(id, action).then(data => this.setState({ selected: data.data, action: action }));
    else if (!action) {
      this.getAvailableModules();
      this.getVhmList();
    } else {
      this.setState({ action: action, id: id });
    }
    this.clearMessages();
  }

  handleResponseError(jqXHR) {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  }

  clearMessages() {
    this.setState({
      messages: undefined,
    });
  }

  getVhmDetails(id, action?: any) {
    return Network.get("/rhn/manager/api/vhms/" + id).catch(this.handleResponseError);
  }

  getVhmList() {
    return Network.get("/rhn/manager/api/vhms")
      .then(data => this.setState({ action: undefined, selected: undefined, vhms: data.data }))
      .catch(this.handleResponseError);
  }

  getAvailableModules() {
    return Network.get("/rhn/manager/api/vhms/modules")
      .then(data => this.setState({ availableModules: data }))
      .catch(this.handleResponseError);
  }

  deleteSelected() {
    this.deleteVhm(this.state.selected);
  }

  deleteVhm(item) {
    if (!item) return false;
    return Network.del("/rhn/manager/api/vhms/delete/" + item.id)
      .then(data => {
        this.handleBackAction();
        this.setState({
          messages: MessagesUtils.info("Virtual Host Manager has been deleted."),
        });
      })
      .catch(this.handleResponseError);
  }

  getCreateType() {
    const types = ["file", "vmware", "kubernetes", "amazonec2", "googlece", "azure", "nutanixahv"];
    return types.includes(this.state.id) ? this.state.id : types[0];
  }

  handleBackAction() {
    this.getVhmList().then(data => {
      const loc = window.location;
      window.history.pushState(null, "", loc.pathname + loc.search);
    });
    this.getAvailableModules();
  }

  handleDetailsAction(row) {
    this.getVhmDetails(row.id).then(data => {
      this.setState({ selected: data.data, action: "details" });
      window.history.pushState(null, "", "#/details/" + row.id);
    });
  }

  handleEditAction(row) {
    this.getVhmDetails(row.id).then(data => {
      this.setState({ selected: data.data, action: "edit" });
      window.history.pushState(null, "", "#/edit/" + row.id);
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
    const panelButtons = (
      <div className="pull-right btn-group">
        {this.state.action !== "create" && this.state.action !== "edit" && this.state.action !== "details" && (
          <DropdownButton
            text={t("Create")}
            icon="fa-plus"
            title={t("Add a virtual host manager")}
            className="btn-default"
            items={this.state.availableModules.map(name => (
              <a data-senna-off href={"#/create/" + name.toLocaleLowerCase()}>
                {msgModuleTypes[name.toLocaleLowerCase()]}
              </a>
            ))}
          />
        )}
      </div>
    );
    return (
      <TopPanel
        title={this.getPanelTitle()}
        icon="spacewalk-icon-virtual-host-manager"
        button={panelButtons}
        helpUrl="reference/systems/virtual-host-managers.html"
      >
        {this.state.messages ? <Messages items={this.state.messages} /> : null}
        {this.state.action === "details" ? (
          <VirtualHostManagerDetails
            data={this.state.selected}
            onCancel={this.handleBackAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteSelected}
          />
        ) : this.state.action === "create" ? (
          <VirtualHostManagerEdit type={this.getCreateType()} onCancel={this.handleBackAction} />
        ) : this.state.action === "edit" ? (
          <VirtualHostManagerEdit
            item={this.state.selected}
            type={this.state.selected.gathererModule}
            onCancel={this.handleBackAction}
          />
        ) : (
          <VirtualHostManagerList
            data={this.state.vhms}
            onSelect={this.handleDetailsAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteVhm}
          />
        )}
      </TopPanel>
    );
  }
}

export const renderer = (id: string) =>
  SpaRenderer.renderNavigationReact(<VirtualHostManager />, document.getElementById(id));
