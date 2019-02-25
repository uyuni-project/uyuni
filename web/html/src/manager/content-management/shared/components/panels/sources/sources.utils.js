import type {sourcesType} from "./panels/sources/sources";

export function addSource(sources: Array<sourcesType>, sourceToAdd: sourcesType): Array<sourcesType> {
  return [...sources, sourceToAdd];
}

export function modifySource(sources: Array<sourcesType>, sourceToModify: sourcesType): Array<sourcesType> {
  return sources.map(source => source.id === sourceToModify.id ? sourceToModify : source);
}

export function deleteSource(sources: Array<sourcesType>, sourceToDelete: sourcesType): Array<sourcesType> {
  return sources.filter(source => source.id !== sourceToDelete.id);
}
