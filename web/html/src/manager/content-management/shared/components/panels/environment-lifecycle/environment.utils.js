export function mapAddEnvironmentRequest(environment, environments, projectId) {
  let environmentRequest = {
    ...environment,
    projectLabel: projectId,
    label: environment.name
  }

  if (!environmentRequest.predecessorLabel) {
    const lastEnvironment = environments[environments.length - 1]
    if (lastEnvironment) {
      environmentRequest.predecessorLabel = environments[environments.length - 1].label
    } else {
      delete environmentRequest.predecessorLabel;
    }
  } else {
    const envIndex = environments.findIndex((env) => env.label === environmentRequest.predecessorLabel);
    if (envIndex > 0) {
      environmentRequest.predecessorLabel = environments[envIndex - 1].label
    } else {
      delete environmentRequest.predecessorLabel;
    }
  }

  return environmentRequest;
}

export function mapUpdateEnvironmentRequest(environment, projectId) {
  let environmentRequest = {
    ...environment,
    projectLabel: projectId,
  }

  return environmentRequest;
}


export function addEnvironmentAction() {

}
