import * as React from "react";

import { Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

type Props = {
  data: any;
  onEdit: (...args: any[]) => any;
  onCancel: (...args: any[]) => any;
  onDelete: (...args: any[]) => any;
};

type State = {
  messages: any[];
  nodes?: any;
};

class VirtualHostManagerDetails extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      messages: [],
    };
  }

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  UNSAFE_componentWillMount() {
    Network.get("/rhn/manager/api/vhms/" + this.props.data.id + "/nodes")
      .then((data) => {
        this.setState({ nodes: data.data });
      })
      .catch(this.handleResponseError);
  }

  onRefresh = () => {
    return Network.post("/rhn/manager/api/vhms/" + this.props.data.id + "/refresh", this.props.data.id)
      .then((data) => {
        if (data.success) {
          this.setState({
            messages: MessagesUtils.info(t("Refreshing the data for this Virtual Host Manager has been triggered.")),
          });
        } else {
          this.setState({
            messages: MessagesUtils.error(data.messages),
          });
        }
      })
      .catch(this.handleResponseError);
  };

  render() {
    return (
      <div>
        {this.state.messages ? <Messages items={this.state.messages} /> : null}
        <BootstrapPanel title={t("Properties")}>
          <ConfigParams data={this.props.data} />
        </BootstrapPanel>
        {this.state.nodes && this.state.nodes.length > 0 && (
          <BootstrapPanel title={t("Nodes")}>
            <Table data={this.state.nodes} identifier={(node) => node.type + "_" + node.id} initialSortColumnKey="name">
              <Column
                columnKey="name"
                comparator={Utils.sortByText}
                header={t("Name")}
                cell={(row, criteria) =>
                  row.type === "server" ? (
                    <a href={"/rhn/systems/details/Overview.do?sid=" + row.id}>{row.name}</a>
                  ) : (
                    row.name
                  )
                }
              />
              <Column columnKey="os" comparator={Utils.sortByText} header={t("OS")} cell={(row, criteria) => row.os} />
              <Column
                columnKey="arch"
                comparator={Utils.sortByText}
                header={t("CPU Arch")}
                cell={(row, criteria) => row.arch}
              />
              <Column
                columnKey="cpuSockets"
                comparator={Utils.sortByText}
                header={t("CPU Sockets")}
                cell={(row, criteria) => (row.cpuSockets !== 0 ? row.cpuSockets : t("N/A"))}
              />
              <Column
                columnKey="memory"
                comparator={Utils.sortByText}
                header={t("RAM (Mb)")}
                cell={(row, criteria) => (row.memory !== 0 ? row.memory : t("N/A"))}
              />
            </Table>
          </BootstrapPanel>
        )}
        <div className="btn-group">
          <Button
            text={t("Back")}
            icon="fa-chevron-left"
            title={t("Back")}
            className="btn-default"
            handler={this.props.onCancel}
          />
          <Button
            text={t("Refresh Data")}
            icon="fa-refresh"
            title={t("Refresh data from this Virtual Host Manager")}
            className="btn-default"
            handler={() => this.onRefresh()}
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
        <DeleteDialog
          id="delete-single-modal"
          title={t("Delete Virtual Host Manager")}
          content={<span>{t("Are you sure you want to delete this virtual host manager?")}</span>}
          onConfirm={this.props.onDelete}
        />
      </div>
    );
  }
}

function ConfigParams(props) {
  const data = props.data;

  if (data.config.kubeconfig && !data.config.context) {
    data.config.context = "<default>";
  }

  const items = Object.keys(data.config)
    .filter((key) => !key.includes("secret"))
    .map((key) => {
      return (
        <tr>
          <td>{Utils.capitalize(key)}:</td>
          <td>{data.config[key]}</td>
        </tr>
      );
    });
  if (data.credentials && data.credentials.username) {
    items.push(
      <tr>
        <td>{t("Username")}:</td>
        <td>{data.credentials.username}</td>
      </tr>
    );
  }

  return (
    <div className="table-responsive">
      <table className="table">
        <tbody>
          <tr>
            <td>{t("Organization")}:</td>
            <td>{data.orgName}</td>
          </tr>
          <tr>
            <td>{t("Gatherer module")}:</td>
            <td>{data.gathererModule}</td>
          </tr>
          {items}
        </tbody>
      </table>
    </div>
  );
}

export { VirtualHostManagerDetails };
