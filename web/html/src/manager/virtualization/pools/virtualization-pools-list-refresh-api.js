// @flow
import * as React from 'react';
import * as Network from 'utils/network';
import { Utils as MessagesUtils } from 'components/messages';

type Props = {
  serverId: string,
  lastRefresh: number,
  children: Function,
};

export function VirtualizationPoolsListRefreshApi(props: Props) {
  const [pools, setPools] = React.useState(undefined);
  const [errors, setErrors] = React.useState([]);

  React.useEffect(() => refreshServerData(), [props.lastRefresh]);

  const refreshServerData = () => {
    Network.get(`/rhn/manager/api/systems/details/virtualization/pools/${props.serverId}/data`, 'application/json').promise
      .then((data) => {
        setPools(data);
        setErrors([]);
      })
      .catch((response) => {
        const errorMessage = Network.errorMessageByStatus(response.status);
        setErrors(errorMessage !== '' ? MessagesUtils.error(errorMessage) : []);
      });
  }

  return props.children({
    pools,
    errors,
  });
}
