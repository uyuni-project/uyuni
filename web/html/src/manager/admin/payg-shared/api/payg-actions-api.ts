import { useState } from "react";

import { Cancelable } from "utils/functions";
import Network from "utils/network";

type returnUsePaygActionsApi = {
  onAction: (actionBodyRequest: any, action: NetworkActionKey, id?: String | null) => Promise<any>;
  cancelAction: () => void;
  isLoading: boolean;
};

const networkAction = {
  get: Network.get,
  create: Network.post,
  action: Network.post,
  update: Network.put,
  delete: Network.del,
};

type NetworkActionKey = keyof typeof networkAction;

const getApiUrl = (id?: string) => {
  if (!id) {
    return `/rhn/manager/api/admin/config/payg`;
  } else {
    return `/rhn/manager/api/admin/config/payg/${id}`;
  }
};

const getErrorMessage = ({ messages = [], errors = {} }) => ({
  messages: messages.filter(Boolean),
  errors: errors,
});

const useLifecyclePaygActionsApi = (): returnUsePaygActionsApi => {
  const [isLoading, setIsLoading] = useState(false);
  const [onGoingNetworkRequest, setOnGoingNetworkRequest] = useState<Cancelable | null>(null);

  const onAction = (actionBodyRequest, action: NetworkActionKey, id) => {
    if (!isLoading) {
      setIsLoading(true);

      const apiUrl = getApiUrl(id);

      let networkRequest: Cancelable;
      if (action === "get" || !networkAction[action]) {
        networkRequest = networkAction.get(apiUrl);
      } else {
        networkRequest = networkAction[action](apiUrl, actionBodyRequest);
      }
      setOnGoingNetworkRequest(networkRequest);

      return networkRequest
        .then((response) => {
          setIsLoading(false);

          if (!response.success) {
            throw getErrorMessage(response);
          }

          return response.data;
        })
        .catch((xhr) => {
          let errMessages;
          if (xhr.status === 0) {
            errMessages = t("Request interrupted or invalid response received from the server. Please try again.");
          } else if (xhr.status === 400) {
            errMessages = getErrorMessage(xhr.responseJSON);
          } else {
            errMessages = Network.errorMessageByStatus(xhr.status);
          }

          setIsLoading(false);

          throw errMessages;
        });
    } else {
      return new Promise(() => {});
    }
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

export default useLifecyclePaygActionsApi;
