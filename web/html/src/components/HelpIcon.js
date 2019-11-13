// @flow
import * as React from 'react';

const HelpIcon = ({text} : {text: ?string}) : ?React.Element<'i'> => {
    return text ? <i className="fa fa-question-circle" title={text}></i> : null;
}

export default HelpIcon;
