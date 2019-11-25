// @flow

const React = require('react');
const { InputBase } = require('./InputBase');
const { FormContext } = require('./Form');

type Props = {
  children: React.Node,
  inputClass?: string,
} & InputBase.Props;

function Select(props: Props) {
  const {
    inputClass,
    children,
    ...propsToPass
  } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass}>
      {
        ({
          setValue,
          onBlur,
        }) => {
          const onChange = (event: Object) => {
            setValue(event.target.name, event.target.value);
          };
          const fieldValue = formContext.model[props.name] || props.defaultValue || '';
          return (
            <select
              className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
              name={props.name}
              disabled={props.disabled}
              value={fieldValue}
              onBlur={onBlur}
              onChange={onChange}
            >
              {children}
            </select>
          );
        }
      }
    </InputBase>
  );
}

Select.defaultProps = Object.assign({
  inputClass: undefined,
}, InputBase.defaultProps);

module.exports = {
  Select,
};
