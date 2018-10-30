// @flow

const React = require('react');
const { DateTimePicker } = require('../datetimepicker');
const { InputBase } = require('./InputBase');

type Props = {
  timezone?: string,
} & InputBase.Props;

function DateTime(props: Props) {
  const {
    timezone,
    ...propsToPass
  } = props;
  return (
    <InputBase {...propsToPass}>
      {
        ({
          setValue,
        }) => {
          const onChange = (value) => {
            setValue(props.name, value);
          };
          return (
            <DateTimePicker
              onChange={onChange}
              value={props.value}
              timezone={timezone}
            />
          );
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
