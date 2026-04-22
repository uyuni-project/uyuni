# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

module AITestReviewer
  # Base class for AI test reviewer backends.
  #
  # Concrete implementations must override {#review}.
  class BaseBackend
    # Reviews the given context.
    #
    # @param context [Hash] data needed by the backend implementation
    # @return [void]
    # @raise [NotImplementedError] when not overridden by a concrete backend
    def review(context)
      raise NotImplementedError, 'Backend must implement #review(context)'
    end
  end
end
