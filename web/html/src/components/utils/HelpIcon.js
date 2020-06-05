// @flow
import * as React from 'react';

type Props = {
  /** Title of the icon */
  text: ?string,
}

/** Display help icon with a title */
const HelpIcon = ({text} : Props) : React.Node => {
    return text ? <i className="fa fa-question-circle spacewalk-help-link" title={text}></i> : null;
}

export default HelpIcon;
