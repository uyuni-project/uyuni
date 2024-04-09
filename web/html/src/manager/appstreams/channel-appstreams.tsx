export const ChannelAppStreams = ({
  appStreams,
  moduleName,
  channelId,
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
      {appStreams[moduleName].map((appStream, streamIdx) => {
        const stream = `${appStream.name}:${appStream.stream}`;
        const changedStatus = appStream.enabled ? toDisable.includes(stream) : toEnable.includes(stream);
        const enabled = toEnable.includes(stream) || (appStream.enabled && !toDisable.includes(stream));
        return (
          <tr key={`${appStream.name}_${appStream.stream}`}>
            {channelIdx === 0 && (
              <td rowSpan={numberOfAppStreams > 0 ? numberOfAppStreams : 1}>
                <a href={`/rhn/channels/ChannelDetail.do?cid=${channelId}`}>{channelLabel}</a>
              </td>
            )}
            {streamIdx === 0 && <td rowSpan={appStreams[moduleName].length}>{moduleName}</td>}
            <td>
              <button className={"btn btn-link"} onClick={() => showPackages(stream)}>
                {appStream.name}:{appStream.stream}
              </button>
            </td>
            <td>
              <button className={"btn btn-sm btn-default"} onClick={() => onToggle(appStream)}>
                {(enabled ? t("Enabled") : t("Disabled")) + (changedStatus ? "*" : "")}
              </button>
            </td>
          </tr>
        );
      })}
    </>
  );
};
