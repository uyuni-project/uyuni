import React from 'react';

type LoadingProps = {
  /** Text to be displayed with the loading spinner */
  text?: string,
  /** whether to show borders around the component */
  withBorders?: bool,
  /** Text to display as a tooltip */
  title?: string,
  /** whether to choose the size of the icon */
  size?: string
}

const _SIZES = {
  S: "12px",
  M: "24px",
  L: "36px",
  XL: "48px"
};

export function Loading({ withBorders, text, title, size } : LoadingProps) {
  return (
    <div className="panel-body text-center" title={title ? title : text}>
      {
          withBorders
            ? <div className="line-separator" />
            : null
        }
      <img src="/css/eos-icons/animated-svg/loading.svg" with={_SIZES[size.toUpperCase()]} height={_SIZES[size.toUpperCase()]} />
      <h4>{text}</h4>
      {
        withBorders
          ? <div className="line-separator" />
          : null
      }
    </div>
  );
}

Loading.defaultProps = {
  text: undefined,
  withBorders: false,
  title: undefined,
  size: "M",
}
