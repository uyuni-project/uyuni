import { useState } from "react";

import { Cancelable } from "utils/functions";
import Network from "utils/network";

type NetworkActionKey = "get" | "create" | "update" | "delete" | "test" | "testUserLookup" | "testGroupResolution";

type ReturnUseLdapActionsApi = {
  onAction: (actionBodyRequest: any, action: NetworkActionKey, id?: string | number | null) => Promise<any>;
  cancelAction: () => void;
  isLoading: boolean;
};

const getApiUrl = (id?: string | number | null, action?: NetworkActionKey) => {
  if (!id) {
    return `/rhn/manager/api/admin/config/ldap`;
  }
  if (action === "test") {
    return `/rhn/manager/api/admin/config/ldap/${id}/test-connection`;
  }
  if (action === "testUserLookup") {
    return `/rhn/manager/api/admin/config/ldap/${id}/test-user-lookup`;
  }
  if (action === "testGroupResolution") {
    return `/rhn/manager/api/admin/config/ldap/${id}/test-group-resolution`;
  }
  return `/rhn/manager/api/admin/config/ldap/${id}`;
};

const getErrorMessage = ({ messages = [], errors = {} }) => ({
  messages: messages.filter(Boolean),
  errors: errors,
});

const useLdapActionsApi = (): ReturnUseLdapActionsApi => {
  const [isLoading, setIsLoading] = useState(false);
  const [onGoingNetworkRequest, setOnGoingNetworkRequest] = useState<Cancelable | null>(null);

  const onAction = (actionBodyRequest, action: NetworkActionKey, id?: string | number | null) => {
    if (isLoading) {
      return new Promise(() => {});
    }
    setIsLoading(true);
    const apiUrl = getApiUrl(id, action);

    let networkRequest: Cancelable;
    if (action === "get") {
      networkRequest = Network.get(apiUrl);
    } else if (
      action === "create" ||
      action === "test" ||
      action === "testUserLookup" ||
      action === "testGroupResolution"
    ) {
      networkRequest = Network.post(apiUrl, actionBodyRequest);
    } else if (action === "update") {
      networkRequest = Network.put(apiUrl, actionBodyRequest);
    } else if (action === "delete") {
      networkRequest = Network.del(apiUrl, actionBodyRequest);
    } else {
      networkRequest = Network.get(apiUrl);
    }
    setOnGoingNetworkRequest(networkRequest);

    return networkRequest
      .then((response) => {
        setIsLoading(false);
        if (!response.success) {
          throw getErrorMessage(response);
        }
        // Prefer data when present (lookup results); otherwise surface success messages
        if (response.data !== null && response.data !== undefined) {
          return response.data;
        }
        return response.messages;
      })
      .catch((xhr) => {
        let errMessages;
        if (xhr.status === 0) {
          errMessages = t("Request interrupted or invalid response received from the server. Please try again.");
        } else if (xhr.status === 400) {
          errMessages = getErrorMessage(xhr.responseJSON || {});
        } else {
          errMessages = Network.errorMessageByStatus(xhr.status);
        }
        setIsLoading(false);
        throw errMessages;
      });
  };

  const cancelAction = () => {
    onGoingNetworkRequest?.cancel({ status: 0 });
    setIsLoading(false);
  };

  return {
    onAction,
    cancelAction,
    isLoading,
  };
};

export default useLdapActionsApi;
