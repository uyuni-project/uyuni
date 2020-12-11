# Typescript migration

Notes
 - This relies on TS 4.1.2, 3.x does not give sufficient debugging output for automatic migrations.

Migration steps:  

1. Install new dependencies: `yarn`  
// 1. Migrate CommonJS imports and exports to ES6 as much as possible: `yarn migrate-cjs-to-es6`  
//    * This will log errors in places that they don't know how to handle, this is fine.
1. Check which Flow files are found: `yarn find-flow`  
1. Migrate any found Flow files to TS: `yarn migrate-flow`  
1. Check which Javascript files have type annotations without Flow or TS: `yarn find-untyped-annotated`
1. Migrate any of the above to TS: `yarn migrate-untyped-annotated`
    * There is one file which throws here, simply renaming to tsx is sufficient: `mv components/section-toolbar/section-toolbar.js components/section-toolbar/section-toolbar.tsx`
    * Confirm no more files remain that need to be updated, should output no file names: `yarn find-untyped-annotated`
// 1. Check for compilation errors: `yarn tsc`
//    * There is one `Unexpected token` issue in the JSX template in `manager/shared/menu/menu.js`, resolve it by replacing the token with `&gt;`
1. Check which files have `Object` annotations which should be `any`: `yarn find-object-to-any`
    * For context, Flow and TS handle `Object` very differently. In Flow it's a loose type that matches anything, in TS it's a strict type and `any` should be used to allow anything instead.
1. Fix any of the above: `yarn migrate-object-to-any`
1. In similar vain, migrate `Array<Object>` to `Array<any>`: `yarn migrate-array-object-array-any`
1. Some of our `useState` calls are typed incorrectly as `undefined` because they have no annotations, we should mark them as `any`. To find them: `yarn find-untyped-use-state`
1. Fix any of the above: `yarn migrate-untyped-use-state`
1. Some child renderers etc are typed as `React.ReactNode` while they should be `JSX.Element`. Find them: `yarn find-react-node-jsx-element`
1. Fix any of the above: `yarn migrate-react-node-jsx-element`
1. And also: `yarn migrate-react-node-jsx-element-2`


Notes:
 - about 10 files need manual Node -> ReactNode migration
 - a number of types are declared but not actually exported for other modules to use
 - untyped object initializations `let websocket = {};` could be fixed by adding `any`
 - some components can't be used as JSX children, why?

## Common problems you may encounter:

### `Argument of type 'Foo' is not assignable to parameter of type 'never'` when pushing into an empty array

Untyped arrays `const foo = []` are of type `never[]`, meaning you can't push anything non-empty into them.  
To fix the problem, add a type to the initialization: `const foo: Foo[] = []`  
This is not a problem if the source of the assignment is already typed as in `const foo = getTypedFoo()`

### `Type 'string[]' is not assignable to type 'number[]'`

Most commonly with `string` and `number`, but there are other similar instances as well.  
This means you're passing in an array of strings while the input is expected to be an array of numbers.  
To fix, either change the input or the expected input format.  

### `Type '(props: Props) => JSX.Element' is not assignable to type 'string'`  

Usually it means the target expects a string, but you want to pass an element.  
To fix, change the target's props to accept `JSX.Element` which among other things is a superset of `string`.