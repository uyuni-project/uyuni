import * as React from "react";

import _partition from "lodash/partition";
import _sortBy from "lodash/sortBy";
import _unionBy from "lodash/unionBy";

import { inferEntityParams } from "manager/recurring/recurring-actions-utils";

import { pageSize } from "core/user-preferences";

import { DangerDialog } from "components/dialog/DangerDialog";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Column } from "components/table/Column";
import { TableFilter } from "components/table/TableFilter";

import { Utils } from "utils/functions";

import Network from "../../../utils/network"
import { AsyncButton } from "../../../components/buttons";
import { TextField } from "../../../components/fields";
import { Messages, MessageType } from "../../../components/messages/messages";
import { Utils as MessagesUtils } from "../../../components/messages/messages";
import { RankingTable } from "../../../components/ranking-table";
import { SaltStatePopup } from "../../../components/salt-state-popup";
import { Table } from "../../../components/table/Table";

function channelKey(channel) {
  return channel.label;
}

function channelIcon(channel) {
  let iconClass, iconTitle, iconStyle;
  if (channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else if (channel.type === "internal_state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("Internal State");
    iconStyle = { border: "1px solid black" };
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return <i className={iconClass} title={iconTitle} style={iconStyle} />;
}

type PolicyPickerProps = {
  type?: string;
  matchUrl: (filter?: string) => any;
  applyRequest?: (systems: any[]) => any;
  saveRequest: (policies: any[]) => any;
  messages?: (messages: MessageType[] | any) => any;
};

class PolicyPickerState {
  filter = "";
  policies: any[] = [];
  search = {
    filter: null as string | null,
    results: [] as any[],
  };
  assigned: any[] = [];
  changed = new Map();
  showSaltState?: any | null = undefined;
  rank?: boolean = undefined;
  messages: MessageType[] = [];
}

class PolicyPicker extends React.Component<PolicyPickerProps, PolicyPickerState> {
  policy = new PolicyPickerState();

  constructor(props: PolicyPickerProps) {
    super(props);
    this.state = {
      filter: "",
      policies: [],
      search: { filter: null, results: [] },
      assigned: [],
      changed: new Map(),
      showSaltState: null,
      rank: undefined,
      messages: [],
    };
    this.init();
  }

  init = () => {
    Network.get(this.props.matchUrl()).then((data) => {
      data = this.getSortedList(data);
      this.setState({
        policies: data,
        search: {
          filter: this.state.filter,
          results: data,
        },
      });
    });
  };



  save = () => {
    let messages: MessageType[] = [];
    const policies = this.state.assigned;
    if (this.props.type === "policy" && !policies.length) {
      this.setMessages(MessagesUtils.error(t("Policy configuration must not be empty")));
      this.setState({ changed: new Map() });
      return;
    }
    const request = this.props.saveRequest(policies).then(
      (data, textStatus, jqXHR) => {
        const newSearchResults = this.state.search.results.map((policy) => {
          const changed = this.state.changed.get(channelKey(policy));
          // We want to make sure the search results are updated with the changes. If there was a change
          // pick the updated value from the response if not we keep the original.
          if (changed !== undefined) {
            return data.filter((c) => c.label === changed.value.label)[0] || changed.value;
          } else {
            return data.filter((c) => c.label === policy.policyName)[0] || policy;
          }
        });

        messages = messages.concat(MessagesUtils.info(t("Policy assignments have been saved.")));
        this.setState({
          changed: new Map(), // clear changed
          // Update the policies with the new data
          policies: _unionBy(
            data,
            this.state.policies.map((c) => Object.assign(c, { assigned: false, position: undefined })),
            "name"
          ),
          search: {
            filter: this.state.search.filter,
            results: this.getSortedList(newSearchResults),
          },
        });
        this.setMessages(messages);
      },
      (jqXHR, textStatus, errorThrown) => {
        this.setMessages(MessagesUtils.error(t("An error occurred on save.")));
      }
    );
    return request;
  };

  onSearchChange = (event) => {
    this.setState({
      filter: event.target.value,
    });
  };

  getSortedList = (data) => {
    const [assigned, unassigned] = _partition(data, (d) => d.assigned);
    return _sortBy(assigned, "position").concat(_sortBy(unassigned, (n) => n.policyName.toLowerCase()));
  };

  search = () => {
    return Promise.resolve().then(() => {
      if (this.state.filter !== this.state.search.filter) {
        // Since we don't commit our changes to the backend in case of state type we perform a local search
        this.props.type === "state"
          ? this.stateTypeSearch()
          : Network.get(this.props.matchUrl(this.state.filter)).then((data) => {
            this.setState({
              search: {
                filter: this.state.filter,
                results: this.getSortedList(data),
              },
            });
            this.clearMessages();
          });
      }
    });
  };

  stateTypeSearch = () => {
    this.setState({
      search: {
        filter: this.state.filter,
        results: this.state.policies.filter((c) => c.policyName.includes(this.state.filter)),
      },
    });
    this.clearMessages();
  };

  addChanged = (original, key, selected) => {
    const currentChannel = this.state.changed.get(key);
    if (selected === currentChannel?.original?.assigned) {
      this.state.changed.delete(key);
    } else {
      this.state.changed.set(key, {
        original: original,
        value: Object.assign({}, original, { assigned: selected }),
      });
    }
    this.setState({
      changed: this.state.changed,
    });
  };

  handleSelectionChange = (original) => {
    return (event) => {
      const selectedPolicyName = event.target.value;
      const updatedPolicies = this.state.search.results.map((policy) => ({
        ...policy,
        assigned: policy.policyName === selectedPolicyName, // Assign true to the selected policy only
      }));

      this.setState(
        {
          search: {
            ...this.state.search,
            results: updatedPolicies, // Update the search results with the updated policy assignments
          },
          assigned: updatedPolicies.filter((policy) => policy.assigned), // Update assigned state
        },
        () => {
          this.save(); // Call save after state is updated
        }
      );
    };
  };

  tableBody = () => {
    const elements: React.ReactNode[] = [];
    let rows: any[] = [];
    rows = this.state.search.results.map((policy) => {
      const changed = this.state.changed.get(channelKey(policy));
      if (changed !== undefined) {
        return changed;
      } else {
        return {
          original: policy,
        };
      }
    });

    for (var row of rows) {
      const changed = row.value;
      const currentPolicy = changed === undefined ? row.original : changed;

      elements.push(
        <tr
          id={currentPolicy.policyName + "-row"}
          key={currentPolicy.policyName}
          className={changed !== undefined ? "changed" : ""}
        >
          <td>
            {channelIcon(currentPolicy)}
            {currentPolicy.policyName}
          </td>
          <td>{currentPolicy.dataStreamName}</td>
          <td>
            <i className="fa fa-info-circle fa-1-5x text-primary" title={currentPolicy.description} />
          </td>
          <td>
            <div className="form-group">
              <input
                id={currentPolicy.policyName + "-radio"}
                type="radio"
                checked={currentPolicy.assigned}
                value={currentPolicy.policyName}
                onChange={this.handleSelectionChange(row.original)}
              />
            </div>
          </td>
        </tr>
      );
    }

    return (
      <tbody className="table-content">
        {elements.length > 0 ? (
          elements
        ) : (
          <tr>
            <td colSpan={3}>
              <div>{t("No Policies Found")}</div>
            </td>
          </tr>
        )}
      </tbody>
    );
  };


  setMessages = (message) => {
    this.setState({
      messages: message,
    });
    if (this.props.messages) {
      return this.props.messages(message);
    }
  };

  clearMessages() {
    this.setMessages([]);
  }

  getCurrentAssignment = () => {
    const unchanged = this.state.policies.filter((c) => !this.state.changed.has(channelKey(c)));
    const changed = Array.from(this.state.changed.values()).map((c) => c.value);

    return unchanged.concat(changed).filter((c) => c.assigned);
  };

  render() {
    const currentAssignment = this.getCurrentAssignment();

    let buttons;

    return (
      <span>
        {!this.props.messages && this.state.messages ? <Messages items={this.state.messages} /> : null}
        <SectionToolbar>
          <div className="action-button-wrapper">
            <div className="btn-group">{buttons}</div>
          </div>
        </SectionToolbar>
        <div className="panel panel-default">
          <div className="panel-body">
            <div className={"row"} id={"search-row"}>
              <div className={"col-md-5"}>
                <div style={{ paddingBottom: 0.7 + "em" }}>
                  <div className="input-group">
                    <TextField
                      id="search-field"
                      value={this.state.filter}
                      placeholder={
                        t("Search in SCAP policies")
                      }
                      onChange={this.onSearchChange}
                      onPressEnter={this.search}
                    />
                    <span className="input-group-btn">
                      <AsyncButton id="search-states" text={t("Search")} action={this.search} />
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <span>
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>{t("Policy Name")}</th>
                    <th>{t("Data Stream")}</th>
                    <th>{t("Description")}</th>
                    <th>{t("Assign")}</th>
                  </tr>
                </thead>
                {this.tableBody()}
              </table>
            </span>

          </div>
        </div>
      </span>
    );
  }
}



export { PolicyPicker };
