import { Check } from "components/input";

export type CheckFilterOption = {
  /** Visible label next to the checkbox */
  label: string;

  checked: boolean;

  onChange: (checked: boolean) => void;

  /** Id of the checkbox, defaults to the label */
  id?: string;
};

type Props = {
  options: CheckFilterOption[];

  /** Leading label, pass `undefined` to render none */
  label?: string;

  /** CSS class for the wrapping element */
  className?: string;
};

/**
 * A row of checkboxes narrowing down the data of a table, meant to be passed as one of the
 * `additionalFilters` or `titleButtons` of a table. Purely presentational: every option keeps
 * its own state, and the caller decides what each of them filters out.
 */
export function CheckFilterGroup({ options, label = t("Filter by:"), className }: Props) {
  return (
    <div className={`d-flex align-items-center ${className ?? ""}`}>
      {label ? <span className="control-label me-3">{label}</span> : null}
      {options.map((option, index) => (
        <span key={option.id ?? option.label} className={index < options.length - 1 ? "me-4" : ""}>
          <Check
            id={option.id ?? option.label}
            checked={option.checked}
            onChange={option.onChange}
            label={option.label}
          />
        </span>
      ))}
    </div>
  );
}
