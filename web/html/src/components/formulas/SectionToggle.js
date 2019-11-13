import * as React from 'react';

const SectionToggle = (props) => {

    const toggleSection = () => {
        let visible = props.isVisible(props.index);
        if (visible === undefined) {
            visible = true; // initially is open
        }
        props.setVisible(props.index, !visible);
    };

    return <span onClick={() => toggleSection()}>
        <i className={"fa " + (props.isVisible(props.index) ? "fa-angle-up" : "fa-angle-down")}
            title={t("Toggle section visibility")}/>
        {props.children}
        </span>;
}

export default SectionToggle;