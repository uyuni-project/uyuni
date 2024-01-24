#  pylint: disable=missing-module-docstring
from mgrlibmod import mltypes, mlerrcode

RESERVED_STREAMS = ["platform"]


class StreamIndexer:
    """
    Utility class to maintain a hash map k -> list(v) of module stream objects where each 'k' is a
    specific module stream and each 'v' is a specific version of the respective module stream.

    The resolution algoritm runs on whole lists so all versions of a stream is treated as a group.
    If any two versions of a stream have different runtime dependencies, they will be treated
    as different streams and hashed into separate lists.
    """

    def add(self, stream, stream_map):
        """
        add - add a stream to the map

        The stream is appended as another version to the list with the key matching the name,
        stream and runtime dependencies. If no fit is found, a new key is created.

        :param stream: the context object to add
        :param stream_map: the stream hash map
        """
        for row in stream_map:
            stream2 = row[0]
            if self._eq(stream, stream2):
                if stream not in row:
                    row.append(stream)
                return
        stream_map.append([stream])

    def remove_group(self, group, stream_map):
        """
        remove_index - removes a group from the map

        The key matching the specified group is removed from the map

        :param group: a list of streams with a common hash value (same name, stream and dependencies)
        :param stream_map: the stream hash map
        """
        stream1 = group[0]
        for row in stream_map:
            stream2 = row[0]
            if self._eq(stream1, stream2):
                stream_map.remove(row)
                return

    def add_group(self, group, stream_map):
        """
        add_group - adds a stream group to the map

        If a key with the same hash exists, the group will be appended to the list. Otherwise, a new
        key is created.

        :param group: a list of streams with a common hash value (same name, stream and dependencies)
        :param stream_map: the stream hash map
        """
        stream1 = group[0]
        for row in stream_map:
            stream2 = row[0]
            if self._eq(stream1, stream2):
                row.extend(group)
                return
        stream_map.append(group)

    def get_dep_streams(self, ctx):
        """
        get_dep_streams - get the first-level dependencies as (name, stream) tuples for a specified stream context
        """
        deps = ctx.get_dependencies()
        if not deps:
            return []

        deps = deps[0]
        dep_mods = [
            m
            for m in deps.get_runtime_modules()
            if m not in RESERVED_STREAMS and m != ctx.get_module_name()
        ]

        module_streams = []
        for module_name in dep_mods:
            try:
                stream_name = deps.get_runtime_streams(module_name)[0]
            # pylint: disable-next=bare-except
            except:
                # No stream specificed. Any stream will do
                stream_name = None
            module_streams.append((module_name, stream_name))

        return module_streams

    def _eq(self, ctx1, ctx2):
        """
        _eq - checks if two contexts have the same module name, stream name and runtime dependencies

        Used for hashing.
        """
        return self._is_same_stream(ctx1, ctx2) and self._all_deps_same(ctx1, ctx2)

    def _is_same_stream(self, ctx1, ctx2):
        """
        _is_same_stream - determine if two contexts belong to the same stream
        """
        return (
            ctx1.get_module_name() == ctx2.get_module_name()
            and ctx1.get_stream_name() == ctx2.get_stream_name()
        )

    def _get_contexts_for_stream(self, ctx, stream_map):
        """
        _get_contexts_for_stream - get a list of contexts that belong to the same stream as the specified context
        """
        return [s for s in stream_map if self._is_same_stream(s[0], ctx)]

    def _all_deps_same(self, ctx1, ctx2):
        """
        _all_deps_same - checks if two contexts have the same runtime dependencies
        """
        ctx1_deps = self.get_dep_streams(ctx1)
        ctx2_deps = self.get_dep_streams(ctx2)
        return set(ctx1_deps) == set(ctx2_deps)


class DependencyResolver:
    """
    Module dependency resolver

    Resolves dependencies for selected module streams traversing the dependency
    tree using a backtracking algorithm.
    """

    def __init__(self, proc):
        """
        __init__ - initialize the resolver with an MLLibmodProc instance

        :param proc: the MLLibmodProc instance to use for module index related operations
        """
        self._proc = proc
        self._indexer = StreamIndexer()

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
                for c in ctx:
                    self._indexer.add(c, contexts)
            else:
                not_found.append(s.to_obj())

        # Throw an error if any of the requested streams are not found
        if not_found:
            raise mlerrcode.MlModuleNotFound("Module not found").set_data(
                "streams", not_found
            )

        self._solutions = []
        self._do_resolve([], contexts)
        return self._solutions

    def _preselect(self, selected, candidates):
        """
        _preselect - select all the streams that doesn't have multiple contexts

        The method modifies the lists in place, moving elements from 'candidates' into 'selected'.
        The streams that have multiple contexts will remain in 'candidates' for backtracking later.

        :param selected: the map of selected elements
        :param candidates: the remaining candidate pool map
        """
        # Pool of contexts explored so far
        ctx_pool = selected[:]
        ctx_pool.extend(candidates)

        # Process stack: extended as dependencies are explored
        stack = candidates[:]
        candidates.clear()

        while stack:
            s = stack.pop()

            # pylint: disable-next=protected-access
            if len(self._indexer._get_contexts_for_stream(s[0], ctx_pool)) == 1:
                # There is only a single context for this stream so we'll pick it
                if s not in selected:
                    self._indexer.add_group(s, selected)
                dep_mods = self._indexer.get_dep_streams(s[0])
                for module, stream in dep_mods:
                    if not stream:
                        # No stream specified. Any stream will do
                        if next(
                            (
                                c[0]
                                for c in ctx_pool
                                if c[0].get_module_name() == module
                            ),
                            False,
                        ):
                            # Already has an alternative in the pool
                            continue

                    # Add the dependencies to the stack for further processing
                    dep_ctx = self._proc.get_stream_contexts(
                        mltypes.MLStreamType(module, stream)
                    )
                    for c in dep_ctx:
                        self._indexer.add(c, stack)
                        self._indexer.add(c, ctx_pool)
            else:
                # Multiple contexts available for the stream.
                # Will be later resolved with backtracking
                if s not in candidates:
                    self._indexer.add_group(s, candidates)

    def _is_selection_valid(self, selection):
        """
        _is_selection_valid - check if a solution is valid

        :param selection: the list of selected contexts in the solution
        """
        for i in range(0, len(selection)):
            # Check if the selected item is unique in the list
            for j in range(i + 1, len(selection)):
                if (
                    selection[i][0].get_module_name()
                    == selection[j][0].get_module_name()
                ):
                    return False
            # Check if the selected stream conflicts with the matching requested stream
            for requested in self._streams:
                if (
                    selection[i][0].get_module_name() == requested.name
                    and selection[i][0].get_stream_name() != requested.stream
                ):
                    return False
        return True

    def _get_solution(self, selection):
        """
        _get_solution - get a solution-score pair where score indicates the number of default streams in the solution
        """
        num_defaults = 0
        for ctx in selection:
            if (
                self._proc.get_default_stream(ctx[0].get_module_name())
                == ctx[0].get_stream_name()
            ):
                num_defaults += 1

        return (selection, num_defaults)

    def _is_selected(self, module_name, stream_name, selection):
        """
        _is_selected - check if a specified module stream is in a selection list
        """
        for ctx in selection:
            if ctx[0].get_module_name() == module_name and (
                not stream_name or ctx[0].get_stream_name() == stream_name
            ):
                return True
        return False

    def _are_deps_selected(self, stream, selection):
        """
        _are_deps_selected - check if all the dependencies of a stream are in a selection list
        """
        dep_mods = self._indexer.get_dep_streams(stream)
        for module, stream in dep_mods:
            if not self._is_selected(module, stream, selection):
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
                if (
                    ctx[0].get_module_name() == s.name
                    and ctx[0].get_stream_name() == s.stream
                    and self._are_deps_selected(ctx[0], selection)
                ):
                    # Strem s and all its dependencies are in the list
                    found = True
            all_found = all_found and found
        return self._get_solution(selection) if all_found else None

    def _get_deps(self, ctx):
        """
        _get_deps - get the list of contexts of the first-level dependencies for a specified stream context

        :param ctx: a context object
        """
        all_deps = []
        dep_mods = self._indexer.get_dep_streams(ctx)
        for module, stream in dep_mods:
            all_deps.extend(
                self._proc.get_stream_contexts(mltypes.MLStreamType(module, stream))
            )
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
        # pylint: disable-next=protected-access
        contexts = self._indexer._get_contexts_for_stream(candidates[0][0], candidates)

        # Loop through alternatives
        for c in contexts:
            # Remove the alternative contexts from the candidates
            self._indexer.remove_group(c, candidates)

        for c in contexts:
            newselected = selected[:]
            if c not in newselected:
                self._indexer.add_group(c, newselected)
            newcandidates = candidates[:]
            # The dependencies of the currently selected stream are now new candidates
            for d in self._get_deps(c[0]):
                self._indexer.add(d, newcandidates)
            self._do_resolve(newselected, newcandidates)
