from mgrlibmod import mltypes, mlerrcode

class DependencyResolver:
    """
    Module dependency resolver

    Resolves dependencies for selected module streams traversing the dependency
    tree using a backtracking algorithm.
    """
    RESERVED_STREAMS = ["platform"]

    def __init__(self, proc):
        """
        __init__ - initialize the resolver with an MLLibmodProc instance

        :param proc: the MLLibmodProc instance to use for module index related operations
        """
        self._proc = proc

    def resolve(self, streams):
        """
        resolve - resolve dependencies for the requested streams

        The algorithm traverses the dependency tree recursively using backtracking to collect multiple solutions.

        :param streams: the requested streams
        :return:
            A list of solution-score pairs that satisfy all the dependency requirements for the requested streams

            Each solution is a list of selected stream contexts for the requested streams and their dependencies.
            The score indicates the number of selected default streams in the solution.
        """
        self._streams = streams

        # Collect all the available contexts for the requested streams
        # to get an initial list of candidates at the root of the tree
        contexts = []
        not_found = []
        for s in streams:
            ctx = self._proc.get_stream_contexts(s)
            if ctx:
                contexts.extend(self._proc.get_stream_contexts(s))
            else:
                not_found.append(s.to_obj())

        # Throw an error if any of the requested streams are not found
        if not_found:
            raise mlerrcode.MlModuleNotFound("Module not found").set_data("streams", not_found)

        self._solutions = []
        self._do_resolve([], contexts)
        return self._solutions

    def _preselect(self, selected, candidates):
        '''
        _preselect - select all the streams that doesn't have multiple contexts

        The method modifies the lists in place, moving elements from 'candidates' into 'selected'.
        The streams that have multiple contexts will remain in 'candidates' for backtracking later.

        :param selected: the list of selected elements
        :param candidates: the remaining candidate pool
        '''
        # Pool of contexts explored so far
        ctx_pool = selected[:]
        ctx_pool.extend(candidates)

        # Process stack: extended as dependencies are explored
        stack = candidates[:]
        candidates.clear()

        while stack:
            s = stack.pop()
            if len(self._get_contexts_for_stream(s, ctx_pool)) == 1:
                # There is only a single context for this stream so we'll pick it
                if s not in selected:
                    selected.append(s)
                deps = s.get_dependencies()[0]
                dep_mods = [m for m in deps.get_runtime_modules() if m not in self.RESERVED_STREAMS]
                for m in dep_mods:
                    try:
                        dep_str = deps.get_runtime_streams(m)[0]
                    except:
                        # No stream specified. Any stream will do
                        if next((c for c in ctx_pool if c.get_module_name() == m), False):
                            # Already has an alternative in the pool
                            continue
                        dep_str = None

                    # Add the dependencies to the stack for further processing
                    dep_ctx = self._proc.get_stream_contexts(mltypes.MLStreamType(m, dep_str))
                    stack.extend(dep_ctx)
                    ctx_pool.extend(dep_ctx)
            else:
                # Multiple contexts available for the stream.
                # Will be later resolved with backtracking
                if s not in candidates:
                    candidates.append(s)


    def _is_selection_valid(self, selection):
        """
        _is_selection_valid - check if a solution is valid

        :param selection: the list of selected contexts in the solution
        """
        for i in range(0, len(selection)):
            # Check if the selected item is unique in the list
            for j in range(i+1, len(selection)):
                if selection[i].get_module_name() == selection[j].get_module_name():
                    return False
            # Check if the selected stream conflicts with the matching requested stream
            for requested in self._streams:
                if selection[i].get_module_name() == requested.name and selection[i].get_stream_name() != requested.stream:
                    return False
        return True

    def _get_solution(self, selection):
        """
        _get_solution - get a solution-score pair where score indicates the number of default streams in the solution
        """
        num_defaults = 0
        for ctx in selection:
            if self._proc.get_default_stream(ctx.get_module_name()) == ctx.get_stream_name():
                num_defaults += 1

        return (selection, num_defaults)

    def _is_selected(self, module_name, stream_name, selection):
        """
        _is_selected - check if a specified module stream is in a selection list
        """
        for ctx in selection:
            if ctx.get_module_name() == module_name and (not stream_name or ctx.get_stream_name() == stream_name):
                return True
        return False

    def _are_deps_selected(self, stream, selection):
        """
        _are_deps_selected - check if all the dependencies of a stream are in a selection list
        """
        deps = stream.get_dependencies()[0]
        dep_mods = [m for m in deps.get_runtime_modules() if m not in self.RESERVED_STREAMS]
        for m in dep_mods:
            try:
                dep_str = deps.get_runtime_streams(m)[0]
            except:
                # No stream specified. Any stream will do
                dep_str = None
            if not self._is_selected(m, dep_str, selection):
                return False
        return True

    def _accept(self, selection):
        """
        _accept - check if the selection list is an acceptable solution

        :return: the solution-score pair if the solution is acceptable, None otherwise
        """
        all_found = True
        for s in self._streams:
            found = False
            for ctx in selection:
                if ctx.get_module_name() == s.name and ctx.get_stream_name() == s.stream and self._are_deps_selected(ctx, selection):
                    # Strem s and all its dependencies are in the list
                    found = True
            all_found = all_found and found
        return self._get_solution(selection) if all_found else None

    def _is_same_stream(self, s1, s2):
        '''
        _is_same_stream - determine if two contexts belong to the same stream
        '''
        return s1.get_module_name() == s2.get_module_name() and s1.get_stream_name() == s2.get_stream_name()

    def _get_contexts_for_stream(self, ctx, contexts):
        '''
        _get_contexts_for_stream - get a list of contexts that belong to the same stream as the specified context
        '''
        return set(c for c in contexts if self._is_same_stream(c, ctx))

    def _get_deps(self, ctx):
        '''
        _get_deps - get the list of contexts of the first-level dependencies for a specified stream context

        :param ctx: a context object
        '''
        all_deps = []
        deps = ctx.get_dependencies()[0]
        dep_mods = [m for m in deps.get_runtime_modules() if m not in self.RESERVED_STREAMS]
        for m in dep_mods:
            try:
                stream = deps.get_runtime_streams(m)[0]
            except:
                # No stream specified. Any stream will do
                stream = None
            all_deps.extend(self._proc.get_stream_contexts(mltypes.MLStreamType(m, stream)))
        return all_deps

    def _do_resolve(self, selected, candidates):
        """
        _do_resolve - calculate valid solutions that satisfy the dependency requirements

        :param selected: the list of already selected stream contexts
        :param candidates: the potential stream candidates to select from
        """
        # Select all the candidates that have no alternative contexts to reduce needed recursions
        self._preselect(selected, candidates)

        # Validate the current selection
        if not self._is_selection_valid(selected):
            return
        solution = self._accept(selected)
        if solution:
            # We've got a valid solution. Save it and keep solving.
            self._solutions.append(solution)

        if not candidates:
            return

        # Get all alternative contexts for the first candidate
        contexts = self._get_contexts_for_stream(candidates[0], candidates)

        # Loop through alternatives
        for c in contexts:
            # Remove the alternative contexts from the candidates
            candidates.remove(c)

        for c in contexts:
            newselected = selected[:]
            if c not in newselected:
                newselected.append(c)
            newcandidates = candidates[:]
            # The dependencies of the currently selected stream are now new candidates
            newcandidates.extend(self._get_deps(c))
            self._do_resolve(newselected, newcandidates)
