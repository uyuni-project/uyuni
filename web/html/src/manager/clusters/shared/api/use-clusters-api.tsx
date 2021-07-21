import * as React from "react";
import Network from "utils/network";
import { showInfoToastr, showSuccessToastr, showWarningToastr, showErrorToastr } from "components/toastr/toastr";

import { JsonResult } from "utils/network";
import { MessageType } from "components/messages";

// TODO move this to FormulaComponentGenerator when flowified
export type FormulaValuesType = {
  [key: string]: any;
};

export type FormulaContextType = {
  [key: string]: any;
};

export type ClusterProviderType = {
  label: string;
  name: string;
  description: string;
};

export type ServerType = {
  id: number;
  name: string;
  messages: MessageType[];
};

export type ServerGroupType = {
  id: number;
  name: string;
};

export type ClusterType = {
  id: number;
  name: string;
  label: string;
  description: string;
  provider: ClusterProviderType;
  managementNode: ServerType;
  group: ServerGroupType;
};

export type ClusterNodeType = {
  hostname: string;
  server?: ServerType;
  details: {
    [key: string]: any;
  };
};

export type EditableClusterPropsType = {
  name: string;
  description: string;
};

type ClusterNodesResultType = {
  nodes: Array<ClusterNodeType>;
  fields: Array<string>;
};

export type ErrorMessagesType = Error & {
  messages: MessageType[];
};

export class ErrorMessages extends Error {
  messages: MessageType[];

  constructor(messagesIn: MessageType[]) {
    super(messagesIn.map(msg => msg.text).join(", "));
    this.messages = messagesIn;
  }
}

/**
 * We expect a component that consumes a prop `setMessages`
 * The wrapper provides it so whoever uses the wrapper doesn't have to provide it
 */
type WithMessages = {
  setMessages: (messages: MessageType[]) => void;
};
// TS can't fully infer the wrapper types on its own, see https://stackoverflow.com/a/65917007/1470607
type WrapperProps<T> = Omit<T, keyof WithMessages>;

export function withErrorMessages<T extends WithMessages>(
  PageComponent: (props: T | (WithMessages & WrapperProps<T>)) => JSX.Element
) {
  return (props: WrapperProps<T>) => {
    const showMessages = (messages: MessageType[]) => {
      messages.forEach(msg => {
        switch (msg.severity) {
          case "info":
            showInfoToastr(msg.text);
            break;
          case "success":
            showSuccessToastr(msg.text);
            break;
          case "warning":
            showWarningToastr(msg.text);
            break;
          case "error":
            showErrorToastr(msg.text, { autoHide: false });
            break;
          default:
            showInfoToastr(msg.text);
        }
      });
    };

    return (
      <React.Fragment>
        <PageComponent setMessages={(messages: MessageType[]) => showMessages(messages)} {...props} />
      </React.Fragment>
    );
  };
}

const useClustersApi = () => {
  const handleResponseError = (jqXHR: JQueryXHR, arg: string = "") => {
    throw new ErrorMessages(Network.responseErrorMessage(jqXHR));
  };

  const fetchClusterNodes = (clusterId: number): Promise<ClusterNodesResultType> => {
    return Network.get(`/rhn/manager/api/cluster/${clusterId}/nodes`)
      .then((data: JsonResult<ClusterNodesResultType>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const fetchManagementNodes = (provider: string): Promise<Array<ServerType>> => {
    return Network.get(`/rhn/manager/api/cluster/provider/${provider}/management-nodes`)
      .then((data: JsonResult<Array<ServerType>>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const fetchNodesToJoin = (clusterId: number): Promise<Array<ServerType>> => {
    return Network.get(`/rhn/manager/api/cluster/${clusterId}/nodes-to-join`)
      .then((data: JsonResult<Array<ServerType>>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const fetchProviderFormulaForm = (
    provider: string,
    formula: string,
    context?: FormulaContextType | null
  ): Promise<any> => {
    return Network.post(
      `/rhn/manager/api/cluster/provider/${provider}/formula/${formula}/form`,
      context
    )
      .then((data: JsonResult<any>) => {
        return Promise.resolve({
          formula_name: provider,
          formula_list: [],
          metadata: {},
          ...data.data,
        });
      })
      .catch(handleResponseError);
  };

  const fetchClusterFormulaData = (clusterId: number, formula: string): Promise<any> => {
    return Network.get(`/rhn/manager/api/cluster/${clusterId}/formula/${formula}/data`)
      .then((data: JsonResult<any>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const fetchClusterUpgradePlan = (clusterId: number): Promise<string> => {
    return Network.get(`/rhn/manager/api/cluster/${clusterId}/upgrade-plan`)
      .then((data: JsonResult<string>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const saveClusterFormulaData = (clusterId: number, formula: string, data: FormulaValuesType): Promise<any> => {
    return Network.post(`/rhn/manager/api/cluster/${clusterId}/formula/${formula}/data`, data).catch(
      handleResponseError
    );
  };

  const addCluster = (
    name: string,
    label: string,
    description: string,
    providerLabel: string,
    managementNodeId: number,
    managementSettings: FormulaValuesType
  ): Promise<number> => {
    return Network.post(
      "/rhn/manager/api/cluster/new/add",
      {
        name: name,
        label: label,
        description: description,
        managementNodeId: managementNodeId,
        provider: providerLabel,
        managementSettings: managementSettings,
      }
    )
      .then((data: JsonResult<number>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const scheduleJoinNode = (
    clusterId: number,
    serverIds: Array<number>,
    joinFormula: FormulaValuesType,
    earliest: Date,
    actionChain?: string | null
  ): Promise<number> => {
    return Network.post(
      `/rhn/manager/api/cluster/${clusterId}/join`,
      {
        earliest: earliest,
        serverIds: serverIds,
        formula: joinFormula,
      }
    )
      .then((data: JsonResult<number>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const scheduleRemoveNode = (
    clusterId: number,
    serverIds: Array<number>,
    removeFormula: FormulaValuesType,
    earliest: Date,
    actionChain?: string | null
  ): Promise<number> => {
    return Network.post(
      `/rhn/manager/api/cluster/${clusterId}/remove-node`,
      {
        earliest: earliest,
        serverIds: serverIds,
        formula: removeFormula,
      }
    )
      .then((data: JsonResult<number>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const scheduleUpgradeCluster = (clusterId: number, earliest: Date, actionChain?: string | null): Promise<number> => {
    return Network.post(
      `/rhn/manager/api/cluster/${clusterId}/upgrade`,
      {
        earliest: earliest,
      }
    )
      .then((data: JsonResult<number>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  const saveClusterProps = (clusterId: number, cluster: EditableClusterPropsType): Promise<any> => {
    return Network.post(`/rhn/manager/api/cluster/${clusterId}`, cluster)
      .then((data: JsonResult<number>) => {
        return true;
      })
      .catch(handleResponseError);
  };

  const deleteCluster = (clusterId: number): Promise<any> => {
    return Network.del(`/rhn/manager/api/cluster/${clusterId}`, null).catch(handleResponseError);
  };

  const refreshGroupNodes = (clusterId: number): Promise<any> => {
    return Network.post(`/rhn/manager/api/cluster/${clusterId}/refresh-group-nodes`, null).catch(handleResponseError);
  };

  const fetchClusterProps = (clusterId: number): Promise<ClusterType> => {
    return Network.get(`/rhn/manager/api/cluster/${clusterId}`)
      .then((data: JsonResult<ClusterType>) => {
        return data.data;
      })
      .catch(handleResponseError);
  };

  return {
    fetchClusterNodes,
    fetchProviderFormulaForm,
    fetchClusterFormulaData,
    fetchClusterProps,
    saveClusterFormulaData,
    saveClusterProps,
    fetchManagementNodes,
    fetchNodesToJoin,
    addCluster,
    scheduleJoinNode,
    scheduleRemoveNode,
    deleteCluster,
    refreshGroupNodes,
    fetchClusterUpgradePlan,
    scheduleUpgradeCluster,
  };
};

export default useClustersApi;
