import * as React from "react";

type Props = {
  time: string | Date;
};

const DateTime = (props: Props) => (
  <span title={moment(props.time).format("YYYY-MM-DD HH:mm:ss Z")}>{moment(props.time).fromNow()}</span>
);

export { DateTime };
