import { useField } from "formik";

import { useId } from "utils/hooks";

export const AsRange = (props: React.InputHTMLAttributes<HTMLInputElement> & { label?: string }) => {
  const [startField] = useField(`${props.name}_start`);
  const [endField] = useField(`${props.name}_end`);
  const endId = useId();

  return (
    <div className="input-group">
      <input
        {...props}
        {...startField}
        aria-label={t("{rangeInputName} start", { rangeInputName: props.label ?? props.name })}
      />
      <span className="input-group-addon input-group-text" aria-hidden>
        -
      </span>
      <input
        {...props}
        {...endField}
        id={endId}
        aria-label={t("{rangeInputName} end", { rangeInputName: props.label ?? props.name })}
      />
    </div>
  );
};
