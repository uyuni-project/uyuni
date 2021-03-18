import * as React from "react";

type HighlightProps = {
  /** text to display */
  text: string;

  /** substring to search in the text and highlight */
  highlight?: string;

  /** flag enabling highlighting */
  enabled?: boolean;
};

/** Search and highlight part of a text */
export function Highlight(props: HighlightProps) {
  let text = props.text;
  let high = props.highlight;

  if (!props.enabled || !high) {
    return <span key="hl">{text}</span>;
  }

  let pos = text.toLocaleLowerCase().indexOf(high.toLocaleLowerCase());
  if (pos < 0) {
    return <span key="hl">{text}</span>;
  }

  let chunk1: React.ReactNode = text.substring(0, pos);
  let chunk2: React.ReactNode = text.substring(pos, pos + high.length);
  let chunk3: React.ReactNode = text.substring(pos + high.length, text.length);

  chunk1 = chunk1 ? <span key="m1">{chunk1}</span> : null;
  chunk2 = chunk2 ? (
    <span key="m2" style={{ borderRadius: "2px" }}>
      <mark>{chunk2}</mark>
    </span>
  ) : null;
  chunk3 = chunk3 ? <span key="m3">{chunk3}</span> : null;

  return (
    <span key="hl">
      {chunk1}
      {chunk2}
      {chunk3}
    </span>
  );
}
Highlight.defaultProps = {
  enabled: false,
};
