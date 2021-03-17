import * as React from "react";
import { useState, useEffect } from "react";
import Network from "utils/network";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { AsyncButton } from "components/buttons";

interface StateSource {
  id: number;
  name: string;
  type: "STATE" | "CONFIG" | "FORMULA" | "INTERNAL";
  sourceId: number;
  sourceName: string;
  sourceType: "SYSTEM" | "GROUP" | "ORG";
}

const typeMap = {
  "STATE": t("State channel"),
  "CONFIG": t("Config channel"),
  "FORMULA": t("Formula"),
  "INTERNAL": window._IS_UYUNI ? "Uyuni " : "SUSE Manager " + t("internal")
};

export default function HighstateSummary({ minionId }) {
  const [summary, setSummary] = useState<StateSource[]>([]);
  const [isLoading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    Network.get(`/rhn/manager/api/states/summary?sid=${minionId}`).promise
      .then(setSummary)
      .then(() => setLoading(false));
  }, [minionId]);

  if (isLoading) {
    return (
      <div className="row">
        <span>{t("Retrieving highstate summary...")}</span>
      </div>
    );
  }

  return (
    <>
      <Table identifier={state => state.state} data={summary} initialItemsPerPage={0}>
        <Column header={t("State")} columnKey="state" cell={source => <State minionId={minionId} state={source} />} />
        <Column header={t("Type")} columnKey="type" cell={source => typeMap[source.type]} />
        <Column header={t("Inherited From")} columnKey="source" cell={source => <Source source={source} />} />
      </Table>
      <HighstateOutput minionId={minionId} />
    </>
  );
}

function HighstateOutput({ minionId }) {
  const [highstate, setHighstate] = useState("");

  function requestHighstate(id: number) {
    return Network.get(`/rhn/manager/api/states/highstate?sid=${id}`).promise;
  }

  if (!highstate) {
    return (
      <AsyncButton
        icon="fa-file-text-o"
        text={t("Show full highstate output")}
        action={() => requestHighstate(minionId).then(setHighstate)}
        defaultType="btn-link" />
    );
  }

  return (
    <div className="row">
      <pre>{highstate}</pre>
    </div >
  );
}

function State({ minionId, state }: { minionId: number, state: StateSource }) {
  if (state.type === "STATE") {
    return (
      <>
        <i className="spacewalk-icon-software-channels" title={typeMap[state.type]} />
        <strong><a href={`/rhn/configuration/ChannelOverview.do?ccid=${state.id}`}>{state.name}</a></strong>
      </>
    );
  } else if (state.type === "CONFIG") {
    return (
      <>
        <i className="spacewalk-icon-software-channels" title={typeMap[state.type]} />
        <strong><a href={`/rhn/configuration/ChannelOverview.do?ccid=${state.id}`}>{state.name}</a></strong>
      </>
    );
  } else if (state.type === "FORMULA") {
    return (
      <>
        <i className="spacewalk-icon-salt" title={typeMap[state.type]} />
        <strong><a href={`/rhn/manager/systems/details/formula/${state.id}?sid=${minionId}`}>{state.name}</a></strong>
      </>
    );
  } else if (state.type === "INTERNAL") {
    return (
      <>
        <i className="spacewalk-icon-salt" title={typeMap[state.type]} />
        <i>{t("Internal states")}</i>
      </>
    );
  }
  return null;
}

function Source({ source }: { source: StateSource }) {
  const srcType = source.type === "FORMULA" ? "formulas" : "custom";

  if (source.type === "INTERNAL" || source.sourceType === "SYSTEM") {
    return (<span>-</span>);
  } else if (source.sourceType === "GROUP") {
    return (
      <>
        <i className="spacewalk-icon-system-groups" title={t("System Group")} />
        <a href={`/rhn/manager/groups/details/${srcType}?sgid=${source.sourceId}`}>
          {source.sourceName}
        </a>
      </>
    );
  } else if (source.sourceType === "ORG") {
    return (
      <>
        <i className="fa fa-group" title={t("Organization")} />
        <a href={`/rhn/manager/multiorg/details/custom?oid=${source.sourceId}`}>
          {source.sourceName}
        </a>
      </>
    );
  }
  return null;
}
