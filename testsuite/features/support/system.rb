# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

# Represents a system OS identity (host or container) and offers helper predicates.
class System
    def initialize(os_family)
      @os_family = os_family
    end
  
    def is_suse?
      %w[sles opensuse opensuse-leap sle-micro suse-microos opensuse-leap-micro].include?(@os_family)
    end
  
    def is_slemicro?
      @os_family.include?('sle-micro') || @os_family.include?('suse-microos') || @os_family.include?('sl-micro')
    end
  
    def is_leapmicro?
      @os_family.include?('opensuse-leap-micro')
    end
  
    def is_transactional?
      is_slemicro? || is_leapmicro?
    end
  
    def to_s
      "System<#{@os_family}>"
    end
  end
