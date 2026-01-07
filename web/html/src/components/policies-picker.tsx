import { type ReactNode, Component } from "react";

import _partition from "lodash/partition";
import _sortBy from "lodash/sortBy";

import { AsyncButton } from "./buttons";
import { TextField } from "./fields";
import { Messages, MessageType, Utils as MessagesUtils } from "./messages/messages";

import Network from "../utils/network";

type PoliciesPickerProps = {
  matchUrl: (filter?: string) => any;
  saveRequest: (policies: any[]) => any;
  messages?: (messages: MessageType[]) => any;
};

class PoliciesPickerState {
  filter = "";
  policies: any[] = [];
  search = {
    filter: null as string | null,
    results: [] as any[],
  };
  assigned: any[] = [];
  changed = new Map();
  messages: MessageType[] = [];
}

class PoliciesPicker extends Component<PoliciesPickerProps, PoliciesPickerState> {
  state = new PoliciesPickerState();

  constructor(props: PoliciesPickerProps) {
    super(props);
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
    const policies = this.state.assigned;
    const request = this.props.saveRequest(policies).then(
      (data) => {
        const newSearchResults = this.state.search.results.map((policy) => {
          return data.filter((p) => p.id === policy.id)[0] || policy;
        });

        this.setState({
          changed: new Map(),
          policies: data,
          search: {
            filter: this.state.search.filter,
            results: this.getSortedList(newSearchResults),
          },
        });
        this.setMessages(MessagesUtils.info(t("Policy assignment has been saved.")));
      },
      () => {
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
    return _sortBy(assigned, "position").concat(_sortBy(unassigned, (p) => p.policyName.toLowerCase()));
  };

  search = () => {
    return Promise.resolve().then(() => {
      if (this.state.filter !== this.state.search.filter) {
        Network.get(this.props.matchUrl(this.state.filter)).then((data) => {
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

  handleSelectionChange = (original) => {
    return (event) => {
      const selectedPolicyId = parseInt(event.target.value);
      const updatedPolicies = this.state.search.results.map((policy) => ({
        ...policy,
        assigned: policy.id === selectedPolicyId,
      }));

      this.setState(
        {
          search: {
            ...this.state.search,
            results: updatedPolicies,
          },
          assigned: updatedPolicies.filter((policy) => policy.assigned),
        },
        () => {
          this.save();
        }
      );
    };
  };

  tableBody = () => {
    const elements: ReactNode[] = [];
    const rows = this.state.search.results;

    for (const policy of rows) {
      elements.push(
        <tr id={policy.id + "-row"} key={policy.id}>
          <td>
            <i className="fa spacewalk-icon-manage-configuration-files" title={t("SCAP Policy")} />
            {policy.policyName}
          </td>
          <td>{policy.dataStreamName}</td>
          <td>
            <i
              data-bs-toggle="tooltip"
              className="fa fa-info-circle fa-1-5x text-primary"
              title={policy.description}
            />
          </td>
          <td>
            <div className="form-group">
              <input
                id={policy.id + "-radio"}
                type="radio"
                name="policy-selection"
                checked={policy.assigned || false}
                value={policy.id}
                onChange={this.handleSelectionChange(policy)}
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
            <td colSpan={4}>
              <div>{t("No SCAP Policies Found")}</div>
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

  render() {
    return (
      <span>
        {!this.props.messages && this.state.messages ? <Messages items={this.state.messages} /> : null}
        <div className="panel panel-default">
          <div className="panel-body">
            <div className={"row"} id={"search-row"}>
              <div className={"col-md-5"}>
                <div style={{ paddingBottom: 0.7 + "em" }}>
                  <div className="input-group">
                    <TextField
                      id="search-field"
                      value={this.state.filter}
                      placeholder={t("Search in SCAP policies")}
                      onChange={this.onSearchChange}
                      onPressEnter={this.search}
                    />
                    <span className="input-group-btn">
                      <AsyncButton id="search-policies" text={t("Search")} action={this.search} />
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

export { PoliciesPicker };
