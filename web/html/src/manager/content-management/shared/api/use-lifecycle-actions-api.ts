import { useState } from "react";
import { Cancelable } from "utils/functions";
import Network from "utils/network";

type Props = {
  resource: string;
  nestedResource?: string;
};

type returnUseProjectActionsApi = {
  onAction: (actionBodyRequest: any, action: NetworkActionKey, id?: string | null) => Promise<any>;
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
type NetworkMethod = typeof networkAction[NetworkActionKey]

const getApiUrl = (resource: string, nestedResource?: string, id?: string) => {
  if (!id) {
    return `/rhn/manager/api/contentmanagement/${resource}`;
  } else {
    if (!nestedResource) {
      return `/rhn/manager/api/contentmanagement/${resource}/${id}`;
    } else {
      return `/rhn/manager/api/contentmanagement/${resource}/${id}/${nestedResource}`;
    }
  }
};

const getErrorMessage = ({ messages = [], errors = {} }) => ({
  messages: messages.filter(Boolean),
  errors: errors,
});

const useLifecycleActionsApi = (props: Props): returnUseProjectActionsApi => {
  const [isLoading, setIsLoading] = useState(false);
  const [onGoingNetworkRequest, setOnGoingNetworkRequest] = useState<Cancelable | null>(null);

  const onAction = (actionBodyRequest, action: NetworkActionKey, id) => {
    if (!isLoading) {
      setIsLoading(true);

      const apiUrl = getApiUrl(props.resource, props.nestedResource, id);

      let networkRequest: ReturnType<NetworkMethod>;
      if (action === "get" || !networkAction[action]) {
        networkRequest = networkAction.get(apiUrl);
      } else {
        networkRequest = networkAction[action](apiUrl, JSON.stringify(actionBodyRequest));
      }
      setOnGoingNetworkRequest(networkRequest);

      return networkRequest
        .then(response => {
          setIsLoading(false);

          if (!response.success) {
            throw getErrorMessage(response);
          }

          return response.data;
        })
        .catch(xhr => {
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
  };

  return {
    onAction,
    cancelAction,
    isLoading,
  };
};

export default useLifecycleActionsApi;
