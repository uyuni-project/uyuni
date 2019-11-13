// @flow

const React = require('react');
const { useState } = require('react');
const { InputBase } = require('./InputBase');

const styles = require('./Radio.css');

type Props = {
  items: Array<{label: string, value: string}>,
  inline?: boolean,
  openOption?: boolean,
  inputClass?: string,
} & InputBase.Props;

function Radio(props: Props) {
  const [isPristine, setIsPristine] = useState(true);

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

          const onChange = (name, value) => {
            setValue(name, value);
            setIsPristine(false);
          };

          const isOpenOption = props.openOption
            && !props.items.some(item => item.value === props.value)
            && (props.value || !isPristine);

          const radioClass = props.inline ? "radio-inline" : "radio";
          return (
            <span className={styles.radio}>
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
                      onChange={event => onChange(event.target.name, event.target.value)} />
                    {label}
                  </label>
                )
              }

              {
                props.openOption &&
                <div className={`radio ${styles['open_option_wrapper']}`}>
                  <label className={`radio-inline ${styles.open_option_wrapper_align_wrapper}`}>
                    <input
                      className={styles.open_option_wrapper_align_content}
                      type="radio"
                      name={props.name}
                      checked={isOpenOption}
                      onChange={() => onChange(props.name, '')}
                    />
                    {"Other keyword: "}
                  </label>
                  <input
                    name={props.name}
                    type="text"
                    disabled={!isOpenOption}
                    value={isOpenOption ? props.value : ''}
                    onChange={event => onChange(event.target.name, event.target.value)}
                  />
                </div>
              }
              </span>
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
