#  pylint: disable=missing-module-docstring
from salt.exceptions import SaltInvocationError
import logging

log = logging.getLogger(__name__)

try:
    # pylint: disable-next=unused-import
    from kubernetes import client, config  # pylint: disable=import-self
    from kubernetes.config import new_client_from_config

    # pylint: disable-next=unused-import
    from kubernetes.client.rest import ApiException

    # pylint: disable-next=unused-import
    from urllib3.exceptions import HTTPError

    IS_VALID = True
except ImportError as ex:
    IS_VALID = False


# pylint: disable-next=invalid-name
def __virtual__():
    return IS_VALID


def get_all_containers(kubeconfig=None, context=None):
    """
    Retrieve information about all containers running in a Kubernetes cluster.

    :param kubeconfig: path to kubeconfig file
    :param context: context inside kubeconfig
    :return:
    .. code-block:: json
       {
            "containers": [
                {
                    "image_id": "(docker-pullable://)?some/image@sha256:hash....",
                    "image": "myregistry/some/image:v1",
                    "container_id": "(docker|cri-o)://...hash...",
                    "pod_name": "kubernetes-pod",
                    "pod_namespace": "pod-namespace"
                }
       }
    """
    if not kubeconfig:
        raise SaltInvocationError("kubeconfig is mandatory")

    if not context:
        raise SaltInvocationError("context is mandatory")

    api_client = new_client_from_config(kubeconfig, context)
    api = client.CoreV1Api(api_client)
    pods = api.list_pod_for_all_namespaces(watch=False)
    output = dict(containers=[])
    for pod in pods.items:
        if pod.status.container_statuses is not None:
            for container in pod.status.container_statuses:
                res_cont = dict()
                res_cont["container_id"] = container.container_id
                res_cont["image"] = container.image
                res_cont["image_id"] = container.image_id
                res_cont["pod_name"] = pod.metadata.name
                res_cont["pod_namespace"] = pod.metadata.namespace
                output["containers"].append(res_cont)
        else:
            log.error("Failed to parse pod container statuses")

    return output
