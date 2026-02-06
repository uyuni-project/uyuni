import { ChangeEvent, ReactNode, useEffect, useState } from "react";

import _partition from "lodash/partition";
import _sortBy from "lodash/sortBy";

import Network from "../utils/network";
import { AsyncButton } from "./buttons";
import { TextField } from "./fields";
import { Messages, MessageType, Utils as MessagesUtils } from "./messages/messages";

interface Policy {
  id: number;
  policyName: string;
  dataStreamName: string;
  description: string;
  assigned?: boolean;
  position?: number;
}

interface PoliciesPickerProps {
  matchUrl: (filter?: string) => string;
  saveRequest: (policies: Policy[]) => Promise<Policy[]>;
  messages?: (messages: MessageType[]) => void;
}

export const PoliciesPicker = ({
  matchUrl,
  saveRequest,
  messages: parentMessagesHandler,
}: PoliciesPickerProps): JSX.Element => {
  const [filter, setFilter] = useState("");
  const [searchResults, setSearchResults] = useState<Policy[]>([]);
  const [currentFilter, setCurrentFilter] = useState<string | null>(null);
  const [messages, setMessages] = useState<MessageType[]>([]);

  const handleMessages = (msgs: MessageType[]) => {
    setMessages(msgs);
    if (parentMessagesHandler) {
      parentMessagesHandler(msgs);
    }
  };

  const clearMessages = () => {
    handleMessages([]);
  };

  const getSortedList = (data: Policy[]) => {
    const [assigned, unassigned] = _partition(data, (d) => d.assigned);
    return _sortBy(assigned, "position").concat(_sortBy(unassigned, (p) => p.policyName.toLowerCase()));
  };

  useEffect(() => {
    // Initial load
    Network.get(matchUrl()).then((data: Policy[]) => {
      const sortedData = getSortedList(data);
      setSearchResults(sortedData);
      setCurrentFilter(""); // Initially filter is empty equivalent
    });
  }, []); // Only run on mount

  const save = async (currentAssigned: Policy[]) => {
    try {
      const data = await saveRequest(currentAssigned);

      const newSearchResults = searchResults.map((policy) => {
        return data.find((p) => p.id === policy.id) || policy;
      });

      setSearchResults(getSortedList(newSearchResults));
      handleMessages(MessagesUtils.info(t("Policy assignment has been saved.")));
    } catch (error) {
      handleMessages(MessagesUtils.error(t("An error occurred on save.")));
    }
  };

  const onSearchChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFilter(event.target.value);
  };

  const search = async () => {
    if (filter !== currentFilter) {
      try {
        const data = await Network.get(matchUrl(filter));
        setSearchResults(getSortedList(data));
        setCurrentFilter(filter);
        clearMessages();
      } catch (error) {
        // handle error if needed, usually Network handles disjointed
      }
    }
  };

  const handleSelectionChange = (policyId: number) => {
    const updatedPolicies = searchResults.map((policy) => ({
      ...policy,
      assigned: policy.id === policyId,
    }));

    setSearchResults(updatedPolicies);
    const assigned = updatedPolicies.filter((policy) => policy.assigned);
    save(assigned);
  };

  const renderTableBody = (): ReactNode => {
    if (searchResults.length === 0) {
      return (
        <tbody>
          <tr>
            <td colSpan={4}>
              <div>{t("No SCAP Policies Found")}</div>
            </td>
          </tr>
        </tbody>
      );
    }

    return (
      <tbody className="table-content">
        {searchResults.map((policy) => (
          <tr id={`${policy.id}-row`} key={policy.id}>
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
                  id={`${policy.id}-radio`}
                  type="radio"
                  name="policy-selection"
                  checked={policy.assigned || false}
                  value={policy.id}
                  onChange={() => handleSelectionChange(policy.id)}
                />
              </div>
            </td>
          </tr>
        ))}
      </tbody>
    );
  };

  return (
    <span>
      {!parentMessagesHandler && messages ? <Messages items={messages} /> : null}
      <div className="panel panel-default">
        <div className="panel-body">
          <div className="row" id="search-row">
            <div className="col-md-5">
              <div style={{ paddingBottom: "0.7em" }}>
                <div className="input-group">
                  <TextField
                    id="search-field"
                    value={filter}
                    placeholder={t("Search in SCAP policies")}
                    onChange={onSearchChange}
                    onPressEnter={search}
                  />
                  <span className="input-group-btn">
                    <AsyncButton id="search-policies" text={t("Search")} action={search} />
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
              {renderTableBody()}
            </table>
          </span>
        </div>
      </div>
    </span>
  );
};
