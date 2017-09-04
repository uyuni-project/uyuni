from salt.exceptions import SaltInvocationError

try:
    from kubernetes import client, config # pylint: disable=import-self
    from kubernetes.config import new_client_from_config
    from kubernetes.client.rest import ApiException
    from urllib3.exceptions import HTTPError
    IS_VALID = True
except ImportError as ex:
    IS_VALID = False


def __virtual__():
    return IS_VALID


def get_all_containers(kubeconfig=None, context=None):
    '''
    Retrieve information about all containers running in a Kubernetes cluster.

    :param kubeconfig: path to kubeconfig file
    :param context: context inside kubeconfig
    :return:
    .. code-block:: json
       {
            "containers": [
                {
                    "image_id": "docker-pullable://some/image@sha256:hash....",
                    "image": "myregistry/some/image:v1",
                    "container_id": "docker://...hash...",
                    "pod_name": "kubernetes-pod"
                }
       }
    '''
    if not kubeconfig:
        raise SaltInvocationError('kubeconfig is mandatory')

    if not context:
        raise SaltInvocationError('context is mandatory')

    api_client = new_client_from_config(kubeconfig, context)
    api = client.CoreV1Api(api_client)
    pods = api.list_pod_for_all_namespaces(watch=False)
    output = dict(containers=[])
    for pod in pods.items:
        for container in pod.status.container_statuses:
            res_cont = dict()
            res_cont['container_id'] = container.container_id
            res_cont['image'] = container.image
            res_cont['image_id'] = container.image_id
            res_cont['pod_name'] = pod.metadata.name
            output['containers'].append(res_cont)

    return output
