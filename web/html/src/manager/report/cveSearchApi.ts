import { useState } from "react";

import { Cancelable } from "utils/functions";
import Network from "utils/network";

type returnUseCveSearchApi = {
  onAction: (query: String) => Promise<any>;
  isLoading: boolean;
};


const useLifecycleCveSearchApi = (): returnUseCveSearchApi => {
  const [isLoading, setIsLoading] = useState(false);
  const [onGoingNetworkRequest, setOnGoingNetworkRequest] = useState<Cancelable | null>(null);

  const onAction = (actionBodyRequest, action: NetworkActionKey, id) => {
    if (!isLoading) {
      setIsLoading(true);

      const apiUrl = `/rhn/manager/api/reporting/cveSearch`;

      let networkRequest: Cancelable;
      networkRequest = networkAction.get(apiUrl);
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

  return {
    onAction,
    isLoading,
  };
};

export default useLifecycleCveSearchApi;