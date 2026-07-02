import { useEffect, useRef } from "react";

export type CheckInputProps = Omit<React.InputHTMLAttributes<HTMLInputElement>, "onChange" | "type"> & {
  indeterminate?: boolean;
  onChange?: (value: boolean) => void;
};

export const CheckInput = ({ indeterminate, onChange, ...rest }: CheckInputProps) => {
  const ref = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (ref.current) {
      ref.current.indeterminate = Boolean(indeterminate);
    }
  }, [indeterminate]);

  return <input {...rest} onChange={(event) => onChange?.(event.target.checked)} type="checkbox" ref={ref} />;
};
