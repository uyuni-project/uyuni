import * as React from "react";
import Network from "utils/network";

type Props = {
  serverId: string;
  lastRefresh: number;
  type: string;
  children: Function;
};

export function VirtualizationListRefreshApi(props: Props) {
  const [data, setData] = React.useState<any>(undefined);
  const [error, setError] = React.useState<any>(undefined);

  const refreshServerData = React.useCallback(() => {
    Network.get(`/rhn/manager/api/systems/details/virtualization/${props.type}/${props.serverId}/data`)
      .then((data) => {
        setData(data);
        setError(undefined);
      })
      .catch((jqXHR) => {
        setError(Network.responseErrorMessage(jqXHR));
      });
  }, [props.serverId, props.type]);

  React.useEffect(() => refreshServerData(), [props.lastRefresh, refreshServerData]);

  return props.children({
    data,
    error,
  });
}
