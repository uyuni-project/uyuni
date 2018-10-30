// @flow

const React = require('react');
const { InputBase } = require('./InputBase');

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
          return (
            <select
              className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
              name={props.name}
              disabled={props.disabled}
              value={props.value}
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
