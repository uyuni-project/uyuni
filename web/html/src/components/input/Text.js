// @flow
const React = require('react');
const { InputBase } = require('./InputBase');

type Props = {
  type: string,
  placeholder?: string,
  inputClass?: string,
} & InputBase.Props;

const Text = (props: Props) => {
  const {
    type,
    placeholder,
    inputClass,
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
            <input
              className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
              type={type || 'text'}
              name={props.name}
              value={props.value}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              placeholder={placeholder}
            />
          );
        }
      }
    </InputBase>
  )};

Text.defaultProps = Object.assign({
  type: 'text',
  placeholder: undefined,
  inputClass: undefined,
}, InputBase.defaultProps);

module.exports = {
  Text,
};
