import React, { useState } from 'react';

import SectionToggle from './SectionToggle';

const Group = (props) => {
    const [visible, setVisible] = useState(true);
    
    return (
        <div className={visible ? "formula-content-section-open group-heading" : "formula-content-section-closed group-heading"}>
            <SectionToggle setVisible={() => setVisible(!visible)} isVisible={(index) => visible}>
                <h4 id={props.id} key={props.id}>{props.header}</h4>
            </SectionToggle>
            <div>
            { visible ?
                <React.Fragment>
                    { props.help ? <p>{props.help}</p> : null }
                    { props.children }
                </React.Fragment> : null }
            </div>
    </div>);
}

export default Group;