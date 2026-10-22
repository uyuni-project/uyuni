import { IconTag } from "components/icontag";

type Props = {
  /** Title of the icon */
  text?: string | null;
};

/** Display help icon with a title */
const HelpIcon = ({ text }: Props) => {
  return text ? <IconTag icon="fa-question-circle" title={text} /> : null;
};

export default HelpIcon;
