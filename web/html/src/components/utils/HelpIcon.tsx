type Props = {
  /** Title of the icon */
  text?: string | null;
};

/** Display help icon with a title */
const HelpIcon = ({ text }: Props) => {
  return text ? (
    <i className="fa fa-question-circle spacewalk-help-link" data-bs-toggle="tooltip" title={text}></i>
  ) : null;
};

export default HelpIcon;
