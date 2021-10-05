import * as React from "react";
import Validation from "components/validation";
import { InputBase } from "components/input/InputBase";
import { FormContext } from "components/input/Form";
import * as utils from "./utils";

type Props = {
  /** name prefix of the field to map in the form model.
   * The two values will be named `${prefix}_address` and `${prefix}_prefix` */
  prefix: string;

  /** Set to true to wait an IPv6 address rather than an IPv4 one */
  ipv6?: boolean;

  /** Label to display for the field */
  label?: string;

  /** Hint string to display */
  hint?: string;

  /** CSS class to use for the label */
  labelClass?: string;

  /** CSS class to use for the <div> element wrapping the field input part */
  divClass?: string;

  /** Indicates whether the field is required in the form */
  required?: boolean;

  /** Indicates whether the field is disabled */
  disabled?: boolean;

  /** Function to call when the data model needs to be changed.
   *  Takes a name and a value parameter.
   */
  onChange?: (name?: string | undefined, value?: string) => void;
};

export const NetworkAddress = (props: Props) => {
  const { ipv6, ...propsToPass } = props;
  const formContext = React.useContext(FormContext);
  const ipVersion = ipv6 ? "6" : "4";
  return (
    <InputBase
      name={[`${props.prefix}_address`, `${props.prefix}_prefix`]}
      validators={[
        utils.allOrNone,
        (values) => {
          const address = values[`${props.prefix}_address`] || "";
          const prefix = values[`${props.prefix}_prefix`] || "";
          return (
            (address === "" && prefix === "") ||
            (Validation.matches(ipv6 ? utils.ipv6Pattern : utils.ipv4Pattern)(address) &&
              Validation.isInt({ min: 0, max: ipv6 ? 128 : 32 })(prefix))
          );
        },
      ]}
      invalidHint={t(`Value needs to be a valid IPv${ipVersion} address with prefix`)}
      {...propsToPass}
    >
      {({ setValue, onBlur }) => {
        const onChange = (event: any) => {
          setValue(event.target.name, event.target.value);
        };
        const addressValue = (formContext.model || {})[`${props.prefix}_address`] || "";
        const prefixValue = (formContext.model || {})[`${props.prefix}_prefix`] || "";
        return (
          <div className="input-group network-address">
            <input
              className="form-control"
              type="text"
              name={`${props.prefix}_address`}
              value={addressValue}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              aria-label={t(`IPv${ipVersion} ${props.label || ""}`)}
              title={t(`IPv${ipVersion} ${props.label || ""}`)}
            />
            <span className="input-group-addon">/</span>
            <input
              className="form-control network-prefix"
              type="text"
              name={`${props.prefix}_prefix`}
              value={prefixValue}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              aria-label={t(`IPv${ipVersion} ${props.label || ""} prefix`)}
              title={t(`IPv${ipVersion} ${props.label || ""} prefix`)}
            />
          </div>
        );
      }}
    </InputBase>
  );
};

NetworkAddress.defaultProps = {
  ipv6: false,
  label: undefined,
  hint: undefined,
  labelClass: undefined,
  divClass: undefined,
  required: false,
  disabled: false,
  onChange: undefined,
};
