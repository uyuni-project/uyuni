// @flow

import * as React from 'react';
import * as Network from 'utils/network';

type Props = {
  serverId: string,
  lastRefresh: number,
  children: Function,
};

export function VirtualizationGuestsListRefreshApi(props: Props) {
  const [guests, setGuests] = React.useState([]);
  const [error, setError] = React.useState(undefined);

  React.useEffect(() => refreshServerData(), [props.lastRefresh]);

  const refreshServerData = () => {
    Network.get(`/rhn/manager/api/systems/details/virtualization/guests/${props.serverId}/data`, 'application/json').promise
      .then((data) => {
        setGuests(data);
        setError(undefined);
      })
      .catch((response) => {
        setError(Network.errorMessageByStatus(response.status))
      });
  }

  return props.children({
    guests,
    error,
  });
}
