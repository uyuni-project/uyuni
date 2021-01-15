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

## Common problems you may encounter:

### `Argument of type 'Foo' is not assignable to parameter of type 'never'` when pushing into an empty array

Untyped arrays `const foo = []` are of type `never[]`, meaning you can't push anything non-empty into them.  
To fix the problem, add a type to the initialization: `const foo: Foo[] = []`  
This is not a problem if the source of the assignment is already typed as in `const foo = getTypedFoo()`

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

## Technical tidbits

We rely on TS 4.1.2, 3.x does not give sufficient debugging output for automatic migrations.  
