type StatusColor = "default" | "success" | "warning" | "error" | "info" | "running";

type SpecialColor = "gray" | "green" | "yellow" | "blue" | "red";

type BadgeProps =
  | {
      text: string;
      icon?: string;
      variant?: "status";
      color?: StatusColor;
      title?: string;
      small?: boolean;
    }
  | {
      text: string;
      icon?: string;
      variant: "special";
      color?: SpecialColor;
      title?: string;
      small?: boolean;
    };

export const Badge = (props: BadgeProps) => {
  const { text, icon, small = false, variant = "status", color } = props;

  const resolvedColor = color ?? (variant === "status" ? "default" : "gray");
  // Determine the class name based on the status prop

  const badgeClassName = ["badge", `badge--${variant}`, `badge--${resolvedColor}`, small && "badge--sm"]
    .filter(Boolean)
    .join(" ");
  return (
    <span className={badgeClassName} title={props.title} {...(props.title ? { "data-bs-toggle": "tooltip" } : {})}>
      {icon && <i className={`fa ${icon}`} aria-hidden="true" />}
      {text}
    </span>
  );
};
