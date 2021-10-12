import * as React from "react";
import { useEffect, useState, useRef } from "react";
import useClustersApi from "../shared/api/use-clusters-api";
import { AsyncButton, LinkButton, Button } from "components/buttons";
import { SystemLink, SystemGroupLink } from "components/links";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Panel } from "components/panels/Panel";
import { PanelRow } from "components/panels/PanelRow";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Label } from "components/input/Label";
import { Messages } from "components/messages";
import { ModalLink } from "components/dialog/ModalLink";
import { closeDialog, Dialog } from "../../../components/dialog/LegacyDialog";
import { Form } from "components/input/Form";
import { Text } from "components/input/Text";
import { Loading } from "components/utils/Loading";
import { Utils } from "utils/functions";

import {
  ClusterType,
  ClusterNodeType,
  ErrorMessagesType,
  EditableClusterPropsType,
} from "../shared/api/use-clusters-api";
import { MessageType } from "components/messages";

const { capitalize } = Utils;

const NodeField = (props: { value: any }) => {
  if (props.value !== null && props.value !== undefined) {
    if (typeof props.value === "boolean") {
      return props.value ? <span>{t("Yes")}</span> : <span>{t("No")}</span>;
    } else {
      return <span>{props.value}</span>;
    }
  } else {
    return <span>{t("(none)")}</span>;
  }
};

type Props = {
  cluster: ClusterType;
  onUpdateName: (arg0: string) => void;
  setMessages: (arg0: Array<MessageType>) => void;
  hasEditingPermissions: boolean;
};

const ClusterOverview = (props: Props) => {
  const [cluster, setCluster] = useState<ClusterType>(props.cluster);
  const [selections, setSelections] = useState<Set<string>>(new Set());
  const [nodes, setNodes] = useState<Array<ClusterNodeType>>([]);
  const [nodeDetailFields, setNodeDetailFields] = useState<Array<string>>([]);
  const [fetching, setFetching] = useState<boolean>(false);
  const [fetchingProps, setFetchingProps] = useState<boolean>(false);
  const [editModel, setEditModel] = useState<EditableClusterPropsType>({ name: "", description: "" });
  const nodesForm = useRef<HTMLFormElement>(null);

  const { fetchClusterNodes, refreshGroupNodes, saveClusterProps, fetchClusterProps } = useClustersApi();

  const fetchData = () => {
    setFetching(true);
    fetchClusterNodes(props.cluster.id)
      .then((clusterNodes) => {
        setNodes(clusterNodes.nodes);
        setNodeDetailFields(clusterNodes.fields);
        setSelections(new Set());
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      })
      .finally(() => setFetching(false));
  };

  useEffect(() => {
    fetchData();
  }, []);

  const onRemove = () => {
    if (selections.size > 0 && nodesForm.current) {
      nodesForm.current.submit();
    }
  };

  const onRefreshGroupNodes = () => {
    refreshGroupNodes(props.cluster.id)
      .then((actionId) =>
        props.setMessages([Messages.success(t("System group refresh action scheduled successfully"))])
      )
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      });
  };

  const onFetchClusterProps = () => {
    setFetchingProps(true);
    fetchClusterProps(props.cluster.id)
      .then((data: ClusterType) => {
        setCluster(data);
        props.onUpdateName(data.name);
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      })
      .finally(() => {
        setFetchingProps(false);
      });
  };

  const onSaveClusterProps = () => {
    return saveClusterProps(props.cluster.id, editModel)
      .then((_) => {
        closeDialog("cluster-edit-dialog");
        props.setMessages([Messages.success(t("Cluster properties updated successfully"))]);
        onFetchClusterProps();
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      });
  };

  const filterFunc = (row, criteria) => {
    const keysToSearch = ["hostname"];
    if (criteria) {
      return keysToSearch
        .map((key) => row[key])
        .join()
        .toLowerCase()
        .includes(criteria.toLowerCase());
    }
    return true;
  };

  const onSelectNode = (event, hostname) => {
    const newSelections = new Set(selections);
    if (event.target.checked) {
      newSelections.add(hostname);
    } else {
      newSelections.delete(hostname);
    }
    setSelections(newSelections);
  };

  const editContent = (
    <Form
      model={editModel}
      onChange={(model) => {
        setEditModel(model);
      }}
      formDirection="form-horizontal"
    >
      <Text name="name" label={t("Name")} required labelClass="col-md-3" divClass="col-md-6" />
      <Text name="description" label={t("Description")} required labelClass="col-md-3" divClass="col-md-6" />
    </Form>
  );

  const editButtons = (
    <React.Fragment>
      <div className="btn-group col-lg-6"></div>
      <div className="col-lg-6">
        <div className="pull-right btn-group">
          <Button
            id="cancel-btn"
            text={t("Cancel")}
            className="gap-right btn-default"
            handler={() => {
              closeDialog("cluster-edit-dialog");
            }}
          />
          <AsyncButton
            id="save-btn"
            defaultType="btn-primary"
            icon="fa-save"
            text={t("Save")}
            className="gap-right"
            action={() => onSaveClusterProps()}
          />
        </div>
      </div>
    </React.Fragment>
  );

  return (
    <>
      <Dialog
        id="cluster-edit-dialog"
        title={t("Edit cluster details")}
        closableModal={true}
        className="modal-lg"
        content={editContent}
        buttons={editButtons}
      />

      <SectionToolbar>
        <div className="pull-right btn-group">
          {props.hasEditingPermissions && (
            <LinkButton
              id="join-btn"
              icon="fa-plus"
              text={t("Join node")}
              className="gap-right btn-default"
              href={`/rhn/manager/cluster/${props.cluster.id}/join`}
            />
          )}
          {props.hasEditingPermissions && (
            <Button
              id="remove-btn"
              disabled={selections.size === 0}
              icon="fa-minus"
              text={t("Remove node")}
              className="gap-right btn-default"
              handler={onRemove}
            />
          )}
          <AsyncButton
            id="refresh-btn"
            defaultType="btn-default"
            icon="fa-refresh"
            text={t("Refresh")}
            className="gap-right"
            action={() => fetchData()}
          />
          {props.hasEditingPermissions && (
            <LinkButton
              id="upgrade"
              icon="spacewalk-icon-package-upgrade"
              text={t("Upgrade cluster")}
              className="btn-default gap-right"
              href={`/rhn/manager/cluster/${props.cluster.id}/upgrade`}
            />
          )}
          {props.hasEditingPermissions && (
            <AsyncButton
              id="refresh-group-btn"
              defaultType="btn-default"
              icon="fa-refresh"
              text={t("Refresh system group")}
              className="gap-right"
              action={() => onRefreshGroupNodes()}
            />
          )}
        </div>
      </SectionToolbar>

      <Panel
        headingLevel="h3"
        title={t("Cluster Properties")}
        buttons={
          props.hasEditingPermissions ? (
            <ModalLink
              target="cluster-edit-dialog"
              icon="fa-plus"
              text={t("Edit properties")}
              title={t("Edit cluster details")}
              onClick={() => setEditModel({ name: cluster.name, description: cluster.description })}
            />
          ) : null
        }
      >
        {fetchingProps ? (
          <Loading />
        ) : (
          <>
            <PanelRow>
              <Label name={t("Label")} className="col-md-3" />
              <div className="col-md-6">{cluster.label}</div>
            </PanelRow>
            <PanelRow>
              <Label name={t("Name")} className="col-md-3" />
              <div className="col-md-6">{cluster.name}</div>
            </PanelRow>
            <PanelRow>
              <Label name={t("Description")} className="col-md-3" />
              <div className="col-md-6">{cluster.description}</div>
            </PanelRow>
            <PanelRow>
              <Label name={t("Cluster provider")} className="col-md-3" />
              <div className="col-md-6">{cluster.provider.name}</div>
            </PanelRow>
            <PanelRow>
              <Label name={t("Management node")} className="col-md-3" />
              <SystemLink id={cluster.managementNode.id} className="col-md-6">
                {cluster.managementNode.name}
              </SystemLink>
            </PanelRow>
            <PanelRow>
              <Label name={t("System group")} className="col-md-3" />
              <SystemGroupLink id={cluster.group.id} className="col-md-6">
                {cluster.group.name}
              </SystemGroupLink>
            </PanelRow>
          </>
        )}
      </Panel>

      <form ref={nodesForm} action={`/rhn/manager/cluster/${props.cluster.id}/remove`} method="post">
        <input type="hidden" name="csrf_token" value={window.csrfToken} />
        <Table
          data={nodes}
          loading={fetching}
          identifier={(row) => row.hostname}
          initialSortColumnKey="hostname"
          searchField={<SearchField filter={filterFunc} placeholder={t("Filter by any value")} />}
        >
          <Column
            columnKey="select"
            width="2%"
            header={""}
            cell={(row: ClusterNodeType) => (
              <input
                type="checkbox"
                value={row.hostname}
                name="nodes"
                checked={selections.has(row.hostname)}
                onChange={(ev: React.SyntheticEvent<HTMLInputElement>) => onSelectNode(ev, row.hostname)}
              />
            )}
          />
          <Column
            columnKey="hostname"
            width="15%"
            comparator={Utils.sortByText}
            header={t("Node Hostname")}
            cell={(row: ClusterNodeType) => row.hostname}
          />
          <Column
            columnKey="server"
            width="15%"
            header={t("System")}
            cell={(row: ClusterNodeType) =>
              row.server ? <SystemLink id={row.server.id}>{row.server.name}</SystemLink> : t("(none)")
            }
          />
          {nodeDetailFields.map((field) => (
            <Column
              columnKey={field}
              header={capitalize(field)}
              cell={(row: ClusterNodeType) => <NodeField value={row.details[field]} />}
            />
          ))}
        </Table>
      </form>
    </>
  );
};

export default ClusterOverview;
