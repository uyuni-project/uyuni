// @flow

const React = require('react');
const { InputBase } = require('./InputBase');

type Props = {
  items: Array<{label: string, value: string}>,
  inline?: boolean,
  inputClass?: string,
} & InputBase.Props;

function Radio(props: Props) {
  const {
    items,
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
          const radioClass = props.inline ? "radio-inline" : "radio";
          return (
            <>
              {
                props.items.map(({label, value}) =>
                  <label className={radioClass}>
                    <input
                      type="radio"
                      name={props.name}
                      value={value}
                      checked={props.value === value}
                      className={inputClass}
                      onBlur={onBlur}
                      onChange={onChange} />
                    {label}
                  </label>
                )
              }
            </>
          );
        }
      }
    </InputBase>
  );
}

Radio.defaultProps = Object.assign({
  inputClass: undefined,
}, InputBase.defaultProps);

module.exports = {
  Radio,
};
