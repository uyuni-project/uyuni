type Props = {
  /** ID of the server the action is running on */
  serverId: string;

  /** ID of the action to display */
  actionId: string;

  /** Status name of the action. One of 'Queued', 'Failed', 'Completed' or 'Picked Up' */
  status: string;
};

/**
 * Render a clickable icon depending on the action status name.
 */
export function ActionStatus(props: Props) {
  const icons = {
    Queued: "fa-clock-o text-info",
    Failed: "fa-times-circle-o text-danger",
    Completed: "fa-check-circle text-success",
    "Picked Up": "fa-exchange text-info",
  };
  return (
    <a href={`/rhn/systems/details/history/Event.do?sid=${props.serverId}&aid=${props.actionId}`}>
      <i className={`fa ${icons[props.status]} fa-1-5x`} data-bs-toggle="tooltip" title={props.status} />
    </a>
  );
}
