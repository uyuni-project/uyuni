import { Button } from "components/buttons";

export type ToggleButtonOption = {
  value: string;
  label?: string;
  icon?: string;
  tooltip?: string;
  disabled?: boolean;
};

type ToggleButtonGroupProps = {
  value: string;
  options: ToggleButtonOption[];
  onChange: (value: string) => void;
  className?: string;
  size?: "sm";
};

export function ToggleButtonGroup({ value, options, onChange, className = "", size }: ToggleButtonGroupProps) {
  const sizeClass = size === "sm" ? "btn-sm" : "";

  return (
    <div className={`toggle-button-group ${className}`}>
      {options.map((option) => (
        <Button
          key={option.value}
          text={option.label}
          icon={option.icon}
          disabled={option.disabled}
          className={`btn-default ${sizeClass} ${value === option.value ? "active" : ""}`}
          handler={() => onChange(option.value)}
          title={option.tooltip}
        />
      ))}
    </div>
  );
}
