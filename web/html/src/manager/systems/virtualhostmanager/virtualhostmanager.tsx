import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { DropdownButton } from "components/buttons";
import { Messages, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";

import Network from "utils/network";

import { VirtualHostManagerDetails } from "./virtualhostmanager-details";
import { VirtualHostManagerEdit } from "./virtualhostmanager-edit";
import { VirtualHostManagerList } from "./virtualhostmanager-list";

const hashUrlRegex = /^#\/([^/]*)(?:\/(.+))?$/;

const msgModuleTypes = {
  file: t("File-based"),
  vmware: t("VMWare-based"),
  kubernetes: t("Kubernetes Cluster"),
  amazonec2: t("Amazon EC2"),
  googlece: t("Google Compute Engine"),
  azure: t("Azure"),
  nutanixahv: t("Nutanix AHV"),
  libvirt: t("Libvirt API"),
};

function getHashId() {
  const match = window.location.hash.match(hashUrlRegex);
  return match ? match[2] : undefined;
}

function getHashAction() {
  const match = window.location.hash.match(hashUrlRegex);
  return match ? match[1] : undefined;
}

type Props = Record<never, never>;

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
      this.getVhmDetails(id).then((data) => this.setState({ selected: data.data, action: action }));
    else if (!action) {
      this.getAvailableModules();
      this.getVhmList();
    } else {
      this.setState({ action: action, id: id });
    }
    this.clearMessages();
  }

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  clearMessages() {
    this.setState({
      messages: undefined,
    });
  }

  getVhmDetails(id) {
    return Network.get("/rhn/manager/api/vhms/" + id).catch(this.handleResponseError);
  }

  getVhmList() {
    return Network.get("/rhn/manager/api/vhms")
      .then((data) => this.setState({ action: undefined, selected: undefined, vhms: data.data }))
      .catch(this.handleResponseError);
  }

  getAvailableModules = () => {
    return Network.get("/rhn/manager/api/vhms/modules")
      .then((data) => this.setState({ availableModules: data }))
      .catch(this.handleResponseError);
  };

  deleteSelected = () => {
    this.deleteVhm(this.state.selected);
  };

  deleteVhm = (item) => {
    if (!item) return false;
    return Network.del("/rhn/manager/api/vhms/delete/" + item.id)
      .then(() => {
        this.handleBackAction();
        this.setState({
          messages: MessagesUtils.info("Virtual Host Manager has been deleted."),
        });
      })
      .catch(this.handleResponseError);
  };

  handleBackAction = () => {
    this.getVhmList().then(() => {
      const loc = window.location;
      window.history.pushState(null, "", loc.pathname + loc.search);
    });
    this.getAvailableModules();
  };

  handleDetailsAction = (row) => {
    this.getVhmDetails(row.id).then((data) => {
      this.setState({ selected: data.data, action: "details" });
      window.history.pushState(null, "", "#/details/" + row.id);
    });
  };

  handleEditAction = (row) => {
    this.getVhmDetails(row.id).then((data) => {
      this.setState({ selected: data.data, action: "edit" });
      window.history.pushState(null, "", "#/edit/" + row.id);
    });
  };

  getPanelTitle() {
    const action = this.state.action;

    if (action === "details") {
      return this.state.selected.label;
    } else if (action === "create") {
      return t("Add a {type} Virtual Host Manager", { type: this.getLocalizedModuleName(this.state.id) });
    } else {
      return t("Virtual Host Managers");
    }
  }

  getLocalizedModuleName(moduleId: string): string {
    return (
      // first use the localized name
      msgModuleTypes[moduleId] ??
      // then the module name as returned by the server
      this.state.availableModules.find((name) => name.toLocaleLowerCase() === moduleId) ??
      // if still undefined, fallback to the lowercase module id (execution should never reach here)
      moduleId
    );
  }

  render() {
    const panelButtons = (
      <div className="pull-right btn-group">
        {this.state.action !== "create" && this.state.action !== "edit" && this.state.action !== "details" && (
          <DropdownButton
            text={t("Create")}
            icon="fa-plus"
            title={t("Add a virtual host manager")}
            className="btn-primary"
            items={this.state.availableModules.map((name) => (
              <a key={name.toLocaleLowerCase()} data-senna-off href={"#/create/" + name.toLocaleLowerCase()}>
                {this.getLocalizedModuleName(name.toLocaleLowerCase())}
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
        {this.renderContent(this.state.action)}
      </TopPanel>
    );
  }

  renderContent(action: string): React.ReactNode {
    switch (action) {
      case "details":
        return (
          <VirtualHostManagerDetails
            data={this.state.selected}
            onCancel={this.handleBackAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteSelected}
          />
        );

      case "create":
        return <VirtualHostManagerEdit type={this.state.id} onCancel={this.handleBackAction} />;

      case "edit":
        return (
          <VirtualHostManagerEdit
            item={this.state.selected}
            type={this.state.selected.gathererModule}
            onCancel={this.handleBackAction}
          />
        );

      default:
        return (
          <VirtualHostManagerList
            data={this.state.vhms}
            onSelect={this.handleDetailsAction}
            onEdit={this.handleEditAction}
            onDelete={this.deleteVhm}
          />
        );
    }
  }
}

export const renderer = (id: string) =>
  SpaRenderer.renderNavigationReact(<VirtualHostManager />, document.getElementById(id));
