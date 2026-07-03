type CounterBadgeProps = {
  count: number;
  status?: "default" | "highlight";
};

export const CounterBadge = ({ count, status = "default" }: CounterBadgeProps) => {
  // Determine the class name based on the status prop
  const badgeClassName = `badge counter-badge badge--${status}`;
  const displayCount = count >= 999 ? "999+" : count;

  return <span className={badgeClassName}>{displayCount}</span>;
};
