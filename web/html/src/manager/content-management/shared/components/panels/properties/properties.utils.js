//@flow
import type {ProjectHistoryEntry} from "../../../type/project.type";

export function getVersionMessage(historyEntry: ProjectHistoryEntry): string {
  return `Version ${historyEntry.version}: ${historyEntry.message || ""}`
}


export function getVersionMessageByNumber(version: number, historyEntries: Array<ProjectHistoryEntry>): string {
  let versionMessage = "";
  if(version) {
    const matchedVersion = historyEntries.find(
      historyEntry => historyEntry.version === version
    );
    if(matchedVersion) {
      versionMessage = getVersionMessage(matchedVersion)
    }
  }

  return versionMessage;
}
