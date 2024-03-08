import { AppStreamModule } from "./appstreams.type";

export function getNSVCA(appStream: AppStreamModule) {
  return `${appStream.name}:${appStream.stream}:${appStream.version}:${appStream.context}:${appStream.arch}`;
}
