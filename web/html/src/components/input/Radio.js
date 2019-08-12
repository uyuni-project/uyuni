// @flow

const React = require('react');
const { InputBase } = require('./InputBase');

type Props = {
  items: Array<{label: string, value: string}>,
  horizontal?: boolean,
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
          const WrapperElem = props.horizontal ? "span" : "div";
          return (
            <>
              {
                props.items.map(({label, value}) =>
                                 <WrapperElem>
                                   <input
                                      style={{marginRight: "5px"}}
                                      type="radio"
                                      name={props.name}
                                      value={value}
                                      checked={props.value === value}
                                      className={inputClass}
                                      onBlur={onBlur}
                                      onChange={onChange} />
                                   <label style={{marginRight: "5px"}}>
                                     {label}
                                   </label>
                                </WrapperElem>)
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
