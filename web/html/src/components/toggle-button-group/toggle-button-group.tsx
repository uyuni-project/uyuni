import { Button } from "components/buttons";

export type ToggleButtonOption<T extends string> = {
  value: T;
  label?: string;
  icon?: string;
  tooltip?: string;
  disabled?: boolean;
};

type ToggleButtonGroupProps<T extends string> = {
  value: T;
  options: ToggleButtonOption<T>[];
  onChange: (value: T) => void;
  className?: string;
  size?: "sm";
};

export function ToggleButtonGroup<T extends string>({
  value,
  options,
  onChange,
  className = "",
  size,
}: ToggleButtonGroupProps<T>) {
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
