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
      {options.map(({ value: optionValue, label, icon, disabled, tooltip }) => (
        <Button
          key={optionValue}
          text={label}
          icon={icon}
          disabled={disabled}
          className={`btn-default ${sizeClass} ${value === optionValue ? "active" : ""}`}
          handler={() => onChange(optionValue)}
          title={tooltip}
        />
      ))}
    </div>
  );
}
