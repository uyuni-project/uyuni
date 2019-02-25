// @flow

// $FlowFixMe  // upgrade flow
import {useState} from 'react';
import Network from '../../../../utils/network';

type Props = {
  projectId?: string,
  nestedResource?: string,
};

type returnUseProjectActionsApi = {
  onAction: Function,
  cancelAction: Function,
  isLoading: boolean,
}

const networkAction = {
  "get": Network.get,
  "create": Network.post,
  "update": Network.put,
  "delete": Network.del,
}

const getApiUrl = (projectId, nestedResource) => {
  if (!projectId) {
    return "/rhn/manager/contentmanagement/api/projects";
  } else {
    if(!nestedResource) {
      return `/rhn/manager/contentmanagement/api/projects/${projectId}`;
    } else {
      return `/rhn/manager/contentmanagement/api/projects/${projectId}/${nestedResource}`;
    }
  }
}

const useProjectActionsApi = (props:Props): returnUseProjectActionsApi => {
  const [isLoading, setIsLoading] = useState(false);
  const [onGoingNetworkRequest, setOnGoingNetworkRequest] = useState(null);

  const onAction = (actionBodyRequest: Object, action: string) => {
    if(!isLoading) {
      setIsLoading(true);

      const apiUrl = getApiUrl(props.projectId, props.nestedResource);
      const networkMethod = networkAction[action] || networkAction["get"];
      const networkRequest = networkMethod(apiUrl, JSON.stringify(actionBodyRequest), 'application/json');
      setOnGoingNetworkRequest(networkRequest);

      return networkRequest.promise
        .then((response) => {

          setIsLoading(false);

          if (!response.success) {
            throw response.messages[0];
          }

          return response.data;
        })
        .catch((xhr) => {
          const errMessages = xhr.status === 0
            ? t('Request interrupted or invalid response received from the server. Please try again.')
            : Network.errorMessageByStatus(xhr.status);
          setIsLoading(false);

          throw errMessages;
        });
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

export default useProjectActionsApi
