require File.join(File.dirname(__FILE__), "..", "..", "lib", "spacewalk_testsuite_base", "database")

module Test
  module Unit
    class TestCase
      include DatabaseLowLevelAccess
    end
  end
end

