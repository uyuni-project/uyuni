type CounterBadgeProps = {
  text?: string;
  status?: "default" | "highlight";
};

const CounterBadge = ({ text, status = "default" }: CounterBadgeProps) => {
  // Determine the class name based on the status prop
  const badgeClassName = `badge counter-badge badge--${status}`;

  return <span className={badgeClassName}>{text}</span>;
};
export { CounterBadge };
