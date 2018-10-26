// @flow

const React = require('react');
const { InputBase } = require('./InputBase');

type Props = {
  inputClass?: string,
} & InputBase.Props;

function Check(props: Props) {
  const {
    label,
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
          const setChecked = (event: Object) => {
            setValue(event.target.name, event.target.checked);
          };
          return (
            <div className="checkbox">
              <label htmlFor={props.name}>
                <input
                  id={props.name}
                  className={inputClass}
                  name={props.name}
                  type="checkbox"
                  checked={props.value}
                  onChange={setChecked}
                  onBlur={onBlur}
                  disabled={props.disabled}
                />
                <span>{label}</span>
              </label>
            </div>
          );
        }
      }
    </InputBase>
  );
}

Check.defaultProps = Object.assign({
  inputClass: undefined,
}, InputBase.defaultProps);

module.exports = {
  Check,
};
