export type projectPropertiesType = {
  label?: string,
  name?: string,
  description?: string,
  historyEntries?: string,
}

export type projectSourceType = {
  id: string,
  name: string,
  type: string,
  level: string,
  state: string
}

export type projectEnvironmentType = {
  projectLabel: string,
  label: string,
  name: string,
  description: string,
}


export type projectType = {
  properties: projectProperties,
  sources: Array<projectSource>,
  filters: Array<any>,
  environments: Array<projectEnvironment>

}
