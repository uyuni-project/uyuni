import _maxBy from "lodash/maxBy";

export function handlePropertiesChange (project, newProperties) {
  return {
    ...project,
    properties: newProperties
  }
}

export function handleSourcesChange (project, newSources) {

  const hasAnyEntryNotBuilt = project.properties.historyEntries.some((entry) => "draft" in entry && entry.draft);
  if(!hasAnyEntryNotBuilt) {
    const mostRecentVersion = _maxBy(project.properties.historyEntries, (entry) => entry.version);

    const newDraftVersion = {
      version: mostRecentVersion ? mostRecentVersion.version + 1 : 1,
      message: "(draft - not built) - Check the colors bellow for all the changes",
      draft: true
    };

    return {
      ...project,
      properties: {
        ...project.properties,
        historyEntries: [newDraftVersion, ...project.properties.historyEntries],
      },
      sources: newSources
    }
  }

  return {
    ...project,
    sources: newSources
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
