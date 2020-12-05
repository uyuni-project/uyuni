// @flow

import * as React from 'react';
import Network from 'utils/network';

type Props = {
  serverId: string,
  lastRefresh: number,
  type: string,
  children: Function,
};

export function VirtualizationListRefreshApi(props: Props) {
  const [data, setData] = React.useState(undefined);
  const [error, setError] = React.useState(undefined);

  React.useEffect(() => refreshServerData(), [props.lastRefresh]);

  const refreshServerData = () => {
    Network.get(`/rhn/manager/api/systems/details/virtualization/${props.type}/${props.serverId}/data`,
                'application/json').promise
      .then((data) => {
        setData(data);
        setError(undefined);
      })
      .catch(jqXHR => {
        setError(Network.responseErrorMessage(jqXHR));
      });
  }

  return props.children({
    data,
    error,
  });
}
