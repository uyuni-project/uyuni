/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export function mapAddEnvironmentRequest(environment, environments, projectId) {
  const environmentRequest = {
    ...environment,
    projectLabel: projectId,
    label: environment.label,
    name: environment.name,
  };

  if (!environmentRequest.predecessorLabel) {
    const lastEnvironment = environments[environments.length - 1];
    if (lastEnvironment) {
      environmentRequest.predecessorLabel = environments[environments.length - 1].label;
    } else {
      delete environmentRequest.predecessorLabel;
    }
  } else {
    const envIndex = environments.findIndex((env) => env.label === environmentRequest.predecessorLabel);
    if (envIndex > 0) {
      environmentRequest.predecessorLabel = environments[envIndex - 1].label;
    } else {
      delete environmentRequest.predecessorLabel;
    }
  }

  return environmentRequest;
}

export function mapUpdateEnvironmentRequest(environment, projectId) {
  const environmentRequest = {
    ...environment,
    projectLabel: projectId,
  };

  return environmentRequest;
}
