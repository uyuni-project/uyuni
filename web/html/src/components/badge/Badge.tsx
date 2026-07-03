type StatusColor = "default" | "success" | "warning" | "error" | "info" | "running";

type SpecialColor = "gray" | "green" | "yellow" | "blue" | "red";

type DefaultBadgeProps = {
  text: string;
  icon?: string;
  small?: boolean;
};

type StatusProps = DefaultBadgeProps & {
  variant?: "status";
  color?: StatusColor;
};

type SpecialProps = DefaultBadgeProps & {
  variant: "special";
  color?: SpecialColor;
};

type BadgeProps = StatusProps | SpecialProps;

export const Badge = (props: BadgeProps) => {
  const { text, icon, small = false, variant = "status", color = variant === "status" ? "default" : "gray" } = props;

  // Determine the class name based on the status prop
  const resolvedColor = color ?? (variant === "status" ? "default" : "gray");
  const badgeClassName = ["badge", `badge--${variant}`, `badge--${resolvedColor}`, small && "badge--sm"]
    .filter(Boolean)
    .join(" ");
  const formatBadgeText = (text: string) => text.charAt(0).toUpperCase() + text.slice(1);
  return (
    <span className={badgeClassName} title={props.title} {...(props.title ? { "data-bs-toggle": "tooltip" } : {})}>
      {icon && <i className={`fa ${icon}`} aria-hidden="true" />}
      {formatBadgeText(text)}
    </span>
  );
};
