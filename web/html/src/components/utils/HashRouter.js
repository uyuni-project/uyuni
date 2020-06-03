// @flow
import * as React from 'react';
import {useState, useEffect} from 'react';

declare var history: any;

type HashContextType = {
    hash: ?string,
    switch?: boolean,
    match?: boolean,
    goTo: (string) => void,
    back: () => void
}

export const HashRouterContext = React.createContext<HashContextType>({
        hash: null,
        renderOnlyMatching: true,
        goTo: (hash) => {},
        back: () => {}
    });

const hashUrlRegex = /^#\/(.*)$/;

function hashUrl(): ?string {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[1] : undefined;
}

type HashRouterProps = {
    initialPath: string,
    children: React.Node
}

const HashRouter = ({initialPath, children}: HashRouterProps) => {
    const [hash, setHash] = useState(initialPath);

    useEffect(() => {
        const hash = hashUrl();
        if (hash) {
            setHash(hash);
        } else {
            goTo(initialPath);
        }
        window.addEventListener("popstate", (event) => {
            setHash(hashUrl())
        });
    }, []);

    const goTo = (hash: string) => {
        history.pushState(null, "", "#/" + hash);
        setHash(hash);
    }

    const back = () => {
        history.back();
    }

    return (
        <HashRouterContext.Provider value={{hash: hash, goTo: goTo, back: back}}>
            {children}
        </HashRouterContext.Provider>
    );
}

type RouterProps = {
    path: string,
    children:  React.Node | (HashContextType) => React.Node
}

const Route = ({path, children}: RouterProps) => {
    return <HashRouterContext.Consumer>
        {context => {
            const match = path === context.hash;
            if (context.switch) {
                if (match) {
                    if (typeof children === "function") {
                        return children({match: true, ...context});
                    } else {
                        return children;
                    }
                } else {
                    return null;
                }
            } else {
                if (typeof children === "function") {
                    return children({match: match, ...context});
                } else {
                    return children;
                }
            }
        }}
    </HashRouterContext.Consumer>;
}

type SwitchProps = {
    children: React.Node
}

const Switch = ({children}: SwitchProps) => {
    return <HashRouterContext.Consumer>
        {context =>
            <HashRouterContext.Provider value={{switch: true, ...context}}>
                {children}
            </HashRouterContext.Provider>
        }
        </HashRouterContext.Consumer>;
}

export {HashRouter, Route, Switch};
