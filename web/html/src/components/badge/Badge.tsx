type StatusColor = "default" | "success" | "warning" | "error" | "info" | "running";

type SpecialColor = "gray" | "green" | "yellow" | "blue" | "red";

type BadgeProps =
  | {
      text: string;
      icon?: string;
      variant?: "status";
      color?: StatusColor;
    }
  | {
      text: string;
      icon?: string;
      variant: "special";
      color?: SpecialColor;
    };

const Badge = (props: BadgeProps) => {
  const { text, icon, variant = "status", color = variant === "status" ? "default" : "gray" } = props;
  // Determine the class name based on the status prop

  const badgeClassName = ["badge", `badge--${variant}`, `badge--${color}`].join(" ");

  return (
    <span className={badgeClassName}>
      {icon && <i className={`fa ${icon}`} aria-hidden="true" />}
      {text}
    </span>
  );
};
export default Badge;
