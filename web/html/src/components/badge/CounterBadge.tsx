type CounterBadgeProps = {
  count: number;
  status?: "default" | "highlight";
};

export const CounterBadge = ({ count, status = "default" }: CounterBadgeProps) => {
  // Determine the class name based on the status prop
  const badgeClassName = `badge counter-badge badge--${status}`;
  const safeCount = Math.max(0, count);
  const displayCount = safeCount >= 1000 ? "999+" : safeCount;

  return <span className={badgeClassName}>{displayCount}</span>;
};
