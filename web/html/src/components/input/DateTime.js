// @flow

const React = require('react');
const { DateTimePicker } = require('../datetimepicker');
const { InputBase } = require('./InputBase');
const { FormContext } = require('./Form');

type Props = {
  timezone?: string,
} & InputBase.Props;

function DateTime(props: Props) {
  const {
    timezone,
    ...propsToPass
  } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass}>
      {
        ({
          setValue,
        }) => {
          const onChange = (value) => {
            setValue(props.name, value);
          };
          const fieldValue = formContext.model[props.name] || props.defaultValue || '';
          if(fieldValue instanceof Date) {
            return (
              <DateTimePicker
                onChange={onChange}
                value={fieldValue}
                timezone={timezone}
              />
            );
          } else {
            return null
          }
        }
      }
    </InputBase>
  );
}

DateTime.defaultProps = Object.assign({
  timezone: undefined,
}, InputBase.defaultProps);


module.exports = {
  DateTime,
};
