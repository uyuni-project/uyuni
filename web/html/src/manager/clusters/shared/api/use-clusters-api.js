// @flow
import * as React from 'react';
import {useState} from 'react';
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

export type ErrorMessagesType = {
    messages: Array<MessageType>
}

export class ErrorMessages extends Error {

    msg: ?MessageType = null;

    constructor(message: MessageType) {
        super(message.text);
        this.msg = message;
    }
}

type Props = {}
type State = {}

export const withErrorMessages = (PageComponent: React.AbstractComponent<any>) => {
    return class extends React.Component<Props, State> {

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
    const [fetching, setFetching] = useState<boolean>(false);
    // const [fetchListeners] = useState<Array<(boolean) => void>>([]);

    const handleResponseError = (jqXHR: Object, arg: string = "") => {
        throw new ErrorMessages(Network.responseErrorMessage(jqXHR));
    };

    // const fetchClustersList = () : Promise<Array<ClusterType>> => {
    //     return Network.get("/rhn/manager/api/clusters").promise
    //         .then((data: JsonResult<ClustersListResultType>) => {
    //             setClusters(data.data.clusters);
    //             setClustersMessages(data.data.messages);
    //             return data.data.clusters;
    //         })
    //         .catch(handleResponseError)
    //         .finally(() => {
    //             setFetching(false);
    //         });
    // }

    const fetchClusterNodes = (clusterId: number): Promise<ClusterNodesResultType> => {
        setFetching(true);
        return Network.get(`/rhn/manager/api/cluster/${clusterId}/nodes`).promise
            .then((data: JsonResult<ClusterNodesResultType>) => {
                return data.data;
            })
            .catch(handleResponseError)
            .finally(() => {
                setFetching(false);
            });
    }

    const fetchManagementNodes = (provider: string): Promise<Array<ServerType>> => {
        setFetching(true);
        return Network.get(`/rhn/manager/api/cluster/provider/${provider}/management-nodes`).promise
            .then((data: JsonResult<Array<ServerType>>) => {
                return data.data;
            })
            .catch(handleResponseError)
            .finally(() => {
                setFetching(false);
            });
    }

    const fetchNodesToJoin = (clusterId: number): Promise<Array<ServerType>> => {
        setFetching(true);
        return Network.get(`/rhn/manager/api/cluster/${clusterId}/nodes-to-join`).promise
            .then((data: JsonResult<Array<ServerType>>) => {
                return data.data;
            })
            .catch(handleResponseError)
            .finally(() => {
                setFetching(false);
            });
    }

    const fetchProviderFormulaForm = (provider: string, formula: string, context: ?FormulaContextType): Promise<any> => {
        setFetching(true);
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
            .catch(handleResponseError)
            .finally(() => {
                setFetching(false);
            });
    }

    const fetchClusterFormulaData = (clusterId: number, formula: string): Promise<any> => {
        setFetching(true);
        return Network.get(`/rhn/manager/api/cluster/${clusterId}/formula/${formula}/data`).promise
            .then((data: JsonResult<any>) => {
                return data.data;
            })
            .catch(handleResponseError)
            .finally(() => {
                setFetching(false);
            });
    }

    const saveClusterFormulaData = (clusterId: number, formula: string, data: FormulaValuesType): Promise<any> => {
        setFetching(true);
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}/formula/${formula}/data`,
            JSON.stringify(data),
            "application/json"
        ).promise
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const addCluster = (name: string, label: string, description: string, providerLabel: string, managementNodeId: number, managementSettings: FormulaValuesType) : Promise<number> => {
        setFetching(true);
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
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const scheduleJoinNode = (clusterId: number, serverIds: Array<number>, joinFormula: FormulaValuesType, earliest: Date, actionChain: ?string): Promise<number> => {
        setFetching(true);
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
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const scheduleRemoveNode = (clusterId: number, serverIds: Array<number>, removeFormula: FormulaValuesType, earliest: Date, actionChain: ?string): Promise<number> => {
        setFetching(true);
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
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const scheduleUpgradeCluster = (clusterId: number, earliest: Date, actionChain: ?string): Promise<number> => {
        setFetching(true);
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
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const saveClusterProps = (clusterId: number, cluster: EditableClusterPropsType): Promise<any> => {
        setFetching(true);
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}`,
            JSON.stringify(cluster),
            "application/json"
        ).promise
        .then((data: JsonResult<number>) => {
            return true;
        })
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const deleteCluster = (clusterId: number): Promise<any> => {
        setFetching(true);
        return Network.del(
            `/rhn/manager/api/cluster/${clusterId}`,
            null,
            "application/json"
        ).promise
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const refreshGroupNodes = (clusterId: number): Promise<any> => {
        setFetching(true);
        return Network.post(
            `/rhn/manager/api/cluster/${clusterId}/refresh-group-nodes`,
            null,
            "application/json"
        ).promise
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
    }

    const fetchClusterProps = (clusterId: number): Promise<ClusterType> => {
        setFetching(true);
        return Network.get(
            `/rhn/manager/api/cluster/${clusterId}`).promise
        .then((data: JsonResult<ClusterType>) => {
            return data.data;
        })
        .catch(handleResponseError)
        .finally(() => {
            setFetching(false);
        });
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
        scheduleUpgradeCluster
    }
}

export default useClustersApi;
