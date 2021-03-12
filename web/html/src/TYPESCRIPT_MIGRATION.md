# Typescript migration

In the long term, we're migrating from Flow to Typescript.  
We have an assistive migration script to assist in this transition.  

```sh
$ yarn
$ yarn migrate [--verbose] files
```

For example `yarn migrate components/**/*.js` will try to migrate all JS files in the components folder.  
Realistically, this is too big of a piece to bite in one go.  

The migration script also logs out suggestions for related files to migrate.  
The most reasonable approach is to migrate a file or a small set, and then migrate related dependencies, if feasible:  

```sh
$ yarn migrate foo/bar.js foo/tea.js
# Most of the output omitted
the following files need manual review:
	foo/tea.tsx
the following imported files have annotations but are not marked as typed:
	utils/cup.js
$ yarn migrate utils/cup.js
$ yarn tsc
# Get a list of remaining errors that need to be resolved manually
```

## Configuring your editor

If you're using VS Code, the editor config is already included.  
For other editors, Typescript has integration plugins for more or less all popular editors.  

Usually, you will only need to specify where the Typescript SDK is, how to configure this depends on your editor:  
```json
{
    "typescript.tsdk": "./web/html/src/node_modules/typescript/lib"
}
```

## Common problems you may encounter:

### `Argument of type 'Foo' is not assignable to parameter of type 'never'` when pushing into an empty array

Untyped arrays `const foo = []` are of type `never[]`, meaning you can't push anything non-empty into them.  
To fix the problem, add a type to the initialization: `const foo: Foo[] = [];`  
This is not a problem if the source of the assignment is already typed as in `const foo = getTypedFoo();`

### `Type 'string[]' is not assignable to type 'number[]'`

Most commonly with `string` and `number`, but there are other similar instances as well.  
This means you're passing in an array of strings while the input is expected to be an array of numbers.  
To fix, either change the input or the expected input format.  

### `Type '(props: Props) => React.ReactNode' is not assignable to type 'string'`  

Usually it means the target expects a string, but you want to pass an element.  
To fix, change the target's props to accept `React.ReactNode` which among other things is a superset of `string`.  
In certain cases `JSX.Element` may be applicable as well, see [examples and discussions here](https://github.com/typescript-cheatsheets/react#useful-react-prop-type-examples).  

### `import type {Node} from 'react';`

TODO: Karl will fix this in a followup PR. About 10 files need manual `Node` -> `React.ReactNode`.

### `Property 'children' does not exist on type 'IntrinsicAttributes'`

This can happen when you have a string prop `headingLevel: string` and want to use it in JSX to create an element such as `<HeadingLevel style={{ width: "85%" }}>`.  
To solve the problem, let TS know that you expect this to be a node name, not a string: `headingLevel: keyof JSX.IntrinsicElements`

### Using `React.Children.toArray()` with `React.cloneElement()`  

The following snippet will throw with `Type 'string' is not assignable to type ReactElement...`. The problem is that [`ReactChild` includes `string | number`](https://stackoverflow.com/a/42261933/1470607), which is not a valid clone target.

```ts
React.Children.toArray(props.children).map(child => React.cloneElement(child));
```

To solve the problem, add a type guard that checks whether the child is a valid element to clone:

```ts
React.Children.toArray(props.children).map(child => React.isValidElement(child) ? React.cloneElement(child) : child);
```

### Passing down a specific component as a prop

Given a component that wants to consume a specific component as a prop

```tsx
<Parent child={<Child />} />
```

If the strict type of `child` is important, you can define the prop as 

```ts
type Props = {
	child?: React.ReactComponentElement<typeof Child>;
}
```

### `Type 'string' is not assignable to type 'Location'` when using `window.location`

The problem stems from code like: `window.location = "/rhn/manager/clusters";`  
`window.location` is an object, the correct property to assign to is `window.location.href` which is the location string: `window.location.href = "/rhn/manager/clusters";`

### `SyntaxError: Unexpected token` in JSX

A typical example of the error is this:  

```tsx
Module build failed (from ./node_modules/babel-loader/lib/index.js):
SyntaxError: Foo.ts: Unexpected token, expected "," (16:9)
  15 |   return (
> 16 |     <div className="interface">
     |          ^
```

This slightly obscure error message usually means that your file is `.ts`, but it should be `.tsx`.  
The migrator tries to detect this automatically, but it isn't infallible.  

### `Type '(string | null | undefined)[]' is not assignable to type 'string[]'` when using `Array.prototype.filter()`

A simple example of the problem is the below (otherwise perfectly valid) snippet:  

```tsx
const foo: (string | null | undefined)[] = [];
const bar: string[] = foo.filter(Boolean);
//    ^^^ Error shown here
```

This is a [very long-standing open issue in Typescript](https://github.com/microsoft/TypeScript/issues/16655). Shortly put, the `Boolean` constructor doesn't correctly constrain the types as it constrains values.  

We have a shim in place to fix this (see `lib.es5.d.ts`), if that doesn't work you can use either the below snippet or other alternatives from the link.  

```tsx
const foo: (string | null | undefined)[] = [];
const bar: string[] = foo.filter((item): item is string => Boolean(item));
```

## Technical tidbits

We rely on TS 4.1.2, 3.x does not give sufficient debugging output for automatic migrations.  
