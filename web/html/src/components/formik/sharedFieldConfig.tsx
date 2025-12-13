import { type ReactNode, createContext, useContext } from "react";
export type SharedFieldConfigType = {
  /** CSS class to use for the label */
  labelClass?: string;

  // TODO: Rename to fieldClass once everything is done
  /** CSS class to use for the <div> element wrapping the field input part */
  divClass?: string;
};

const initialValue = {};

const FormMetadataContext = createContext<Partial<SharedFieldConfigType>>(initialValue);

/**
 * Get common field params shared across the whole form
 */
export const useSharedFieldConfig = () => useContext(FormMetadataContext);

type Props = Partial<SharedFieldConfigType> & {
  children?: ReactNode;
};

export const SharedFieldConfigProvider = ({ children, ...rest }: Props) => (
  <FormMetadataContext.Provider value={rest}>{children}</FormMetadataContext.Provider>
);
