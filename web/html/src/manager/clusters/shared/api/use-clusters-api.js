// @flow
import * as React from 'react';
import * as Network from 'utils/network';
import {showInfoToastr, showSuccessToastr, showWarningToastr, showErrorToastr} from 'components/toastr/toastr';

import type {JsonResult} from "utils/network";
import type {MessageType} from 'components/messages';

// TODO move this to FormulaComponentGenerator when flowified
export type FormulaValuesType = {[string]: any};

export type FormulaContextType = {[string]: any};

export type ClusterProviderType = {
    label: string,
    name: string,
    description: string
}

export type ServerType = {
    id: number,
    name: string,
    messages: Array<MessageType>
}

export type ServerGroupType = {
    id: number,
    name: string
}

export type ClusterType = {
    id: number,
    name: string,
    label: string,
    description: string,
    provider: ClusterProviderType,
    managementNode: ServerType,
    group: ServerGroupType
}

export type ClusterNodeType = {
    hostname: string,
    server?: ServerType,
    details: {[string]: any}
}

export type EditableClusterPropsType = {
    name: string,
    description: string
}

type ClusterNodesResultType = {
    nodes: Array<ClusterNodeType>,
    fields: Array<string>
}

export type ErrorMessagesType = Error & {
    messages: Array<MessageType>
}

export class ErrorMessages extends Error {

    messages: Array<MessageType>;

    constructor(messagesIn: Array<MessageType>) {
        super(messagesIn.map(msg => msg.text).join(", "));
        this.messages = messagesIn;
    }
}

export const withErrorMessages = (PageComponent: React.AbstractComponent<any>) => {
    return class extends React.Component<{}, {}> {

        showMessages = (messages: Array<MessageType>) => {
            messages.forEach((msg) => {
                switch (msg.severity ) {
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
                        showErrorToastr(msg.text, {autoHide: false});
                        break;
                    default:
                        showInfoToastr(msg.text);
                }
            })
        }

        render() {
            return <React.Fragment>
                    <PageComponent setMessages={(messages) => this.showMessages(messages)} {...this.props}/>
                </React.Fragment>
        }
    };
}

const useClustersApi = ()  => {
    const handleResponseError = (jqXHR: Object, arg: string = "") => {
        throw new ErrorMessages(Network.responseErrorMessage(jqXHR));
    };

    const fetchClusterNodes = (clusterId: number): Promise<ClusterNodesResultType> => {
        return Network.get(`/rhn/manager/api/cluster/${clusterId}/nodes`).promise
            .then((data: JsonResult<ClusterNodesResultType>) => {
                return data.data;
            })
            .catch(handleResponseError);
    }

    const fetchManagementNodes = (provider: string): Promise<Array<ServerType>> => {
        return Network.get(`/rhn/manager/api/cluster/provider/${provider}/management-nodes`).promise
            .then((data: JsonResult<Array<ServerType>>) => {
                return data.data;
            })
            .catch(handleResponseError);
    }

    const fetchNodesToJoin = (clusterId: number): Promise<Array<ServerType>> => {
        return Network.get(`/rhn/manager/api/cluster/${clusterId}/nodes-to-join`).promise
            .then((data: JsonResult<Array<ServerType>>) => {
                return data.data;
            })
            .catch(handleResponseError);
    }

    const fetchProviderFormulaForm = (provider: string, formula: string, context: ?FormulaContextType): Promise<any> => {
        return Network.post(`/rhn/manager/api/cluster/provider/${provider}/formula/${formula}/form`,
            JSON.stringify(context),
            "application/json"
        ).promise
            .then((data: JsonResult<any>) => {
                return Promise.resolve({
                    "formula_name": provider,
                    "formula_list": [],
                    "metadata": {},
                    ...data.data
                    });
            })
            .catch(handleResponseError);
    }

    const fetchClusterFormulaData = (clusterId: number, formula: string): Promise<any> => {
        return Network.get(`/rhn/manager/api/cluster/${clusterId}/formula/${formula}/data`).promise
            .then((data: JsonResult<any>) => {
                return data.data;
            })
            .catch(handleResponseError);
    }

    const fetchClusterUpgradePlan = (clusterId: number): Promise<string> => {
        return Network.get(`/rhn/manager/api/cluster/${clusterId}/upgrade-plan`).promise
            .then((data: JsonResult<string>) => {
                return data.data;
            })
            .catch(handleResponseError);
    }

    const saveClusterFormulaData = (clusterId: number, formula: string, data: FormulaValuesType): Promise<any> => {
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}/formula/${formula}/data`,
            JSON.stringify(data),
            "application/json"
        ).promise
        .catch(handleResponseError);
    }

    const addCluster = (name: string, label: string, description: string, providerLabel: string, managementNodeId: number, managementSettings: FormulaValuesType) : Promise<number> => {
        return Network.post(
            "/rhn/manager/api/cluster/new/add",
            JSON.stringify({
                name: name,
                label: label,
                description: description,
                managementNodeId: managementNodeId,
                provider: providerLabel,
                managementSettings: managementSettings
            }),
            "application/json"
        ).promise
        .then((data: JsonResult<number>) => {
            return data.data
        })
        .catch(handleResponseError);
    }

    const scheduleJoinNode = (clusterId: number, serverIds: Array<number>, joinFormula: FormulaValuesType, earliest: Date, actionChain: ?string): Promise<number> => {
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}/join`,
            JSON.stringify({
                earliest: earliest,
                serverIds: serverIds,
                formula: joinFormula
            }),
            "application/json"
        ).promise
        .then((data: JsonResult<number>) => {
            return data.data
        })
        .catch(handleResponseError);
    }

    const scheduleRemoveNode = (clusterId: number, serverIds: Array<number>, removeFormula: FormulaValuesType, earliest: Date, actionChain: ?string): Promise<number> => {
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}/remove-node`,
            JSON.stringify({
                earliest: earliest,
                serverIds: serverIds,
                formula: removeFormula
            }),
            "application/json"
        ).promise
        .then((data: JsonResult<number>) => {
            return data.data
        })
        .catch(handleResponseError);
    }

    const scheduleUpgradeCluster = (clusterId: number, earliest: Date, actionChain: ?string): Promise<number> => {
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}/upgrade`,
            JSON.stringify({
                earliest: earliest
            }),
            "application/json"
        ).promise
        .then((data: JsonResult<number>) => {
            return data.data
        })
        .catch(handleResponseError);
    }

    const saveClusterProps = (clusterId: number, cluster: EditableClusterPropsType): Promise<any> => {
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}`,
            JSON.stringify(cluster),
            "application/json"
        ).promise
        .then((data: JsonResult<number>) => {
            return true;
        })
        .catch(handleResponseError);
    }

    const deleteCluster = (clusterId: number): Promise<any> => {
        return Network.del(
            `/rhn/manager/api/cluster/${clusterId}`,
            null,
            "application/json"
        ).promise
        .catch(handleResponseError);
    }

    const refreshGroupNodes = (clusterId: number): Promise<any> => {
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}/refresh-group-nodes`,
            null,
            "application/json"
        ).promise
        .catch(handleResponseError);
    }

    const fetchClusterProps = (clusterId: number): Promise<ClusterType> => {
        return Network.get(
            `/rhn/manager/api/cluster/${clusterId}`).promise
        .then((data: JsonResult<ClusterType>) => {
            return data.data;
        })
        .catch(handleResponseError);
    }

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
        scheduleUpgradeCluster
    }
}

export default useClustersApi;
