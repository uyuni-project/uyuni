import { FieldHookConfig, FieldInputProps } from "formik";

type WithRenderer<RendererProps> = Omit<RendererProps, keyof FieldInputProps<any>> & {
  as: (props: RendererProps) => JSX.Element;
};

type WithoutRenderer = {
  as?: string;
};

type MaybeRenderer<RendererProps> = WithRenderer<RendererProps> | WithoutRenderer;

export type FieldProps<ValueType, MaybeProps> = Omit<FieldHookConfig<ValueType>, "as"> &
  MaybeRenderer<MaybeProps> & {
    /** CSS class for the <input> element */
    inputClass?: string;

    /** Label to display for the field */
    label?: string;

    /** CSS class to use for the label, overrides `labelClass` set on the `Form` */
    labelClass?: string;

    // TODO: Rename to fieldClass once everything is done
    /** CSS class to use for the <div> element wrapping the field input part, overrides `divClass` set on the `Form` */
    divClass?: string;
  };
