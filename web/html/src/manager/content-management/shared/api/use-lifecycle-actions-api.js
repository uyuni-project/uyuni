// @flow

import {useState} from 'react';
import Network from '../../../../utils/network';

type Props = {
  resource: string,
  nestedResource?: string,
};

type returnUseProjectActionsApi = {
  onAction: (Object, string, ?string) => Promise<Object>,
  cancelAction: () => void,
  isLoading: boolean,
}

const networkAction = {
  "get": Network.get,
  "create": Network.post,
  "action": Network.post,
  "update": Network.put,
  "delete": Network.del,
}

const getApiUrl = (resource, nestedResource, id) => {
  if (!id) {
    return `/rhn/manager/contentmanagement/api/${resource}`;
  } else {
    if(!nestedResource) {
      return `/rhn/manager/contentmanagement/api/${resource}/${id}`;
    } else {
      return `/rhn/manager/contentmanagement/api/${resource}/${id}/${nestedResource}`;
    }
  }
}

const getErrorMessage = ({messages = [], errors}) => messages.filter(Boolean).concat(Object.values(errors)).join("</br>");


const useLifecycleActionsApi = (props:Props): returnUseProjectActionsApi => {
  const [isLoading, setIsLoading] = useState(false);
  const [onGoingNetworkRequest, setOnGoingNetworkRequest] = useState(null);

  const onAction = (actionBodyRequest, action, id) => {
    if(!isLoading) {
      setIsLoading(true);

      const apiUrl = getApiUrl(props.resource, props.nestedResource, id);
      const networkMethod = networkAction[action] || networkAction["get"];
      const networkRequest = networkMethod(apiUrl, JSON.stringify(actionBodyRequest), 'application/json');
      setOnGoingNetworkRequest(networkRequest);

      return networkRequest.promise
        .then((response) => {

          setIsLoading(false);

          if (!response.success) {
            throw getErrorMessage(response);
          }

          return response.data;
        })
        .catch((xhr) => {
          let errMessages;
          if(xhr.status === 0) {
            errMessages = t('Request interrupted or invalid response received from the server. Please try again.');
          } else if(xhr.status === 400) {
            errMessages = getErrorMessage(xhr.responseJSON)
          } else {
            errMessages = Network.errorMessageByStatus(xhr.status)
          }

          setIsLoading(false);

          throw errMessages;
        });
    } else {
      return new Promise(() => {});
    }
  }

  const cancelAction = () => {
    onGoingNetworkRequest && onGoingNetworkRequest.cancel({status: 0});
  };

  return {
    onAction,
    cancelAction,
    isLoading,
  };
}

export default useLifecycleActionsApi
