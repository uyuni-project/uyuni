type LoadingProps = {
  /** Text to be displayed with the loading spinner */
  text?: string;

  /** whether to show borders around the component */
  withBorders?: boolean;
};

export function Spinner() {
  return <i className="fa fa-spinner fa-spin fa-1-5x" />;
}

export function Loading({ withBorders, text }: LoadingProps) {
  return (
    <div className="panel-body text-center">
      {withBorders ? <div className="line-separator" /> : null}
      <Spinner />
      <h4>{text || t("Loading...")}</h4>
      {withBorders ? <div className="line-separator" /> : null}
    </div>
  );
}
