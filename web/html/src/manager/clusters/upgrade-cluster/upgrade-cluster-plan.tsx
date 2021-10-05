import * as React from "react";
import { useState, useEffect } from "react";
import { Panel } from "components/panels/Panel";
import { Button } from "components/buttons";
import { Loading } from "components/utils/Loading";
import useClustersApi, { withErrorMessages } from "../shared/api/use-clusters-api";

import { ClusterType, ErrorMessagesType } from "../shared/api/use-clusters-api";
import { MessageType } from "components/messages";

type Props = {
  cluster: ClusterType;
  onNext: () => void;
  setMessages: (arg0: Array<MessageType>) => void;
};

const UpgradeClusterPlan = (props: Props) => {
  const [plan, setPlan] = useState<string | null>(null);
  const [fetching, setFetching] = useState(false);

  const { fetchClusterUpgradePlan } = useClustersApi();

  useEffect(() => {
    setFetching(true);
    fetchClusterUpgradePlan(props.cluster.id)
      .then((data) => {
        setPlan(data);
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      })
      .finally(() => {
        setFetching(false);
      });
  }, []);

  return (
    <Panel
      headingLevel="h4"
      title={t("Upgrade plan")}
      footer={
        <div className="btn-group">
          <Button
            id="btn-next"
            text={t("Next")}
            className="btn-success"
            icon="fa-arrow-right"
            handler={() => props.onNext()}
          />
        </div>
      }
    >
      {fetching ? <Loading /> : <pre>{plan}</pre>}
    </Panel>
  );
};

export default withErrorMessages(UpgradeClusterPlan);
