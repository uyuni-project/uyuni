type CounterBadgeProps = {
  count: number;
  status?: "default" | "highlight";
};

export const CounterBadge = ({ count, status = "default" }: CounterBadgeProps) => {
  // Determine the class name based on the status prop
  const badgeClassName = `badge counter-badge badge--${status}`;

  return <span className={badgeClassName}>{count}</span>;
};
