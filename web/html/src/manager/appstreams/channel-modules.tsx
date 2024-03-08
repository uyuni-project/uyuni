import { getNSVCA } from "./appstreams-utils";

export const ChannelModules = ({
  modules,
  moduleName,
  channelLabel,
  channelIdx,
  numberOfAppStreams,
  showPackages,
  toEnable,
  toDisable,
  onToggle,
}) => {
  return (
    <>
      {modules[moduleName].map((appStream, streamIdx) => {
        const nsvca = getNSVCA(appStream);
        const changedStatus = appStream.enabled ? toDisable.includes(nsvca) : toEnable.includes(nsvca);
        const enabled = toEnable.includes(nsvca) || (appStream.enabled && !toDisable.includes(nsvca));
        return (
          <tr key={`${appStream.name}_${appStream.stream}`}>
            {channelIdx === 0 && <td rowSpan={numberOfAppStreams > 0 ? numberOfAppStreams : 1}>{channelLabel}</td>}
            {streamIdx === 0 && <td rowSpan={modules[moduleName].length}>{moduleName}</td>}
            <td>
              {appStream.name}:{appStream.stream}
            </td>
            <td>
              <button className="btn btn-link" onClick={() => showPackages(nsvca)}>
                {nsvca}
              </button>
            </td>
            <td>
              <button className="btn btn-sm" onClick={() => onToggle(appStream)}>
                {(enabled ? "Enabled" : "Disabled") + (changedStatus ? "*" : "")}
              </button>
            </td>
          </tr>
        );
      })}
    </>
  );
};
