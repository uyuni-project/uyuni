

const React = require('react');

type LoadingProps = {
  text: string,
  withBorders: bool
}

function Loading({ withBorders, text } : LoadingProps) {
  return (
    <div className="panel-body text-center">
      {
          withBorders
            ? <div className="line-separator" />
            : null
        }
      <i className="fa fa-spinner fa-spin fa-1-5x" />
      <h4>{text}</h4>
      {
        withBorders
          ? <div className="line-separator" />
          : null
      }
    </div>
  );
}

module.exports = {
  Loading,
};
