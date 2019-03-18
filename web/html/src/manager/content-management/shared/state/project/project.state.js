export function handlePropertiesChange (project, newProperties) {
  return {
    ...project,
    properties: newProperties
  }
}

export function handleBuild (project, publishedVersion) {
  const newSources = project.sources
    .map((source) => ({...source, state: "1"}))
  const historyEntriesWithoutDrafts = project.properties.historyEntries.filter((entry) => !entry.draft)
  let newEnvironments = [...project.environments];
  newEnvironments[0] = {...newEnvironments[0], version: publishedVersion.version}

  return {
    ...project,
    properties: {
      ...project.properties,
      historyEntries: [publishedVersion, ...historyEntriesWithoutDrafts],
    },
    sources: newSources,
    environments: newEnvironments,
  }
}
