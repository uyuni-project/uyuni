import { screen as rawScreen, Screen } from "@testing-library/react";

// Utility type, if a function TargetFunction returns a Promise, return an intersection with Promise<T>, otherwise with T
type ReturnFromWith<TargetFunction extends (...args: any[]) => any, T> = ReturnType<TargetFunction> extends Promise<unknown>
    ? ReturnType<TargetFunction> & Promise<T>
    : ReturnType<TargetFunction> & T;

/**
 * Testing-library doesn't ship generic versions of queries so sometimes it's a pain to annotate what we know comes out of them.
 * This simply adds the option to annotate the expected output of queries conveniently where needed.
 * This is still a bit of a hack though, if you have a better solution please feel free to switch this out.
 */
type GenericScreen = {
    [Key in keyof Screen]: Screen[Key] extends (...args: any[]) => any
        ? <T extends unknown>(...args: Parameters<Screen[Key]>) => ReturnFromWith<Screen[Key], T>
        : Screen[Key];
};

const screen = rawScreen as GenericScreen;

export { screen };
