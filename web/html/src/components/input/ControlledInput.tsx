import { InputHTMLAttributes, useEffect, useState } from "react";

/**
 * @deprecated This is a placeholder fix to ensure input values are always set synchronously.
 * This is obsolete once we integrate Formik instead, see https://github.com/SUSE/spacewalk/issues/14250
 * and other related tickets for related info.
 */
export const ControlledInput = (props: InputHTMLAttributes<HTMLInputElement>) => {
  const { value, ...rest } = props;

  const [internalValue, setInternalValue] = useState(value);

  useEffect(() => {
    if (props.value !== internalValue) {
      setInternalValue(props.value);
    }
  }, [props.value]);

  return (
    <input
      {...rest}
      value={internalValue}
      onChange={(event) => {
        setInternalValue(event.target.value);
        props.onChange?.(event);
      }}
    />
  );
};
