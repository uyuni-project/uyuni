# encoding: utf-8

module Yast
  class SusemanagerReqsClient < Client
    def main
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "FileUtils"
      Yast.import "GetInstArgs"
      Yast.import "Popup"
      Yast.import "Stage"
      Yast.import "Wizard"

      @args = GetInstArgs.argmap

      # 2GB
      @enough_memory = 2000000

      @free_disk_space = 0
      @message = ""

      @meminfo = Convert.to_map(SCR.Read(path(".proc.meminfo")))
      #integer totalmem = meminfo["memtotal"]:0 + meminfo["swaptotal"]:0;
      @totalmem = Ops.get_integer(@meminfo, "memtotal", 0)

      Builtins.y2milestone(
        "Memory: %1, Swap: %2, Total: %3",
        Ops.get_integer(@meminfo, "memtotal", 0),
        Ops.get_integer(@meminfo, "swaptotal", 0),
        @totalmem
      )

      # something is wrong
      if @totalmem == nil
        # using only RAM if possible
        if Ops.get(@meminfo, "memtotal") != nil
          @totalmem = Ops.get_integer(@meminfo, "memtotal", 0) 
          # can't do anything, just assume we enough
        else
          @totalmem = @enough_memory
        end
      end

      # test if we did a setup already
      if Ops.greater_or_equal(
          SCR.Read(path(".target.size"), "/root/.MANAGER_SETUP_COMPLETE"),
          0
        )
        if !Popup.AnyQuestionRichText(
            _("already setup"),
            _(
              "SUSE Manager is already setup. A second attempt to run the setup will fail."
            ),
            40,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end

      # do we have less memory than needed?
      if Ops.less_than(@totalmem, @enough_memory)
        if !Popup.AnyQuestionRichText(
            _("Not enough memory"),
            _(
              "SUSE Manager requires 2G of memory to be installed and 8G for good perfomance. If you continue the product will not function correctly."
            ),
            40,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      @m = Convert.to_map(
        SCR.Execute(
          path(".target.bash_output"),
          "/usr/lib/susemanager/bin/check_disk_space.sh /var/spacewalk"
        )
      )
      Builtins.y2milestone("check_disk_space.sh call: %1", @m)
      @free_disk_space = Builtins.tointeger(Ops.get_string(@m, "stdout", "0"))
      if Ops.less_than(@free_disk_space, 100 * 1024 * 1024)
        @message = Builtins.sformat(
          _("    Not enough disk space (only %1G free)"),
          Ops.divide(@free_disk_space, 1024 * 1024)
        )
        if !Popup.AnyQuestionRichText(
            @message,
            _(
              "SUSE Manager requires 100G of free disk space in /var/spacewalk to be installed. If you continue the product will not function correctly."
            ),
            46,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      @m = Convert.to_map(
        SCR.Execute(
          path(".target.bash_output"),
          "/usr/lib/susemanager/bin/check_disk_space.sh /var/lib/pgsql"
        )
      )
      Builtins.y2milestone("check_disk_space.sh call: %1", @m)
      @free_disk_space = Builtins.tointeger(Ops.get_string(@m, "stdout", "0"))
      if Ops.less_than(@free_disk_space, 30 * 1024 * 1024)
        @message = Builtins.sformat(
          _("    Not enough disk space (only %1G free)"),
          Ops.divide(@free_disk_space, 1024 * 1024)
        )
        if !Popup.AnyQuestionRichText(
            @message,
            _(
              "SUSE Manager requires 30G of free disk space in /var/lib/pgsql to be installed. If you continue the product will not function correctly."
            ),
            46,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      @f_out = Convert.to_map(
        SCR.Execute(path(".target.bash_output"), "hostname -f", {})
      )
      if Ops.get_integer(@f_out, "exit", -1) != 0
        if !Popup.AnyQuestionRichText(
            _("hostname command failed"),
            _(
              "the execution of 'hostname -f' failed. The product will not install correctly."
            ),
            40,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      if Ops.less_than(
          Builtins.size(
            Builtins.filterchars(Ops.get_string(@f_out, "stdout", ""), ".")
          ),
          2
        )
        if !Popup.AnyQuestionRichText(
            _("illegal FQHN"),
            _(
              "the FQHN must contain at least 2 dots. The product will not function correctly."
            ),
            40,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      if Ops.greater_than(
          Builtins.size(
            Builtins.filterchars(Ops.get_string(@f_out, "stdout", ""), "_")
          ),
          0
        )
        if !Popup.AnyQuestionRichText(
            _("illegal FQHN"),
            _(
              "the FQHN must not contain the '_' (undersorce) character. The product will not function correctly."
            ),
            40,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      @h_out = Convert.to_map(
        SCR.Execute(path(".target.bash_output"), "hostname", {})
      )
      @d_out = Convert.to_map(
        SCR.Execute(path(".target.bash_output"), "hostname -d", {})
      )
      Builtins.y2milestone(
        "%1 == %2",
        Builtins.sformat(
          "%1.%2",
          Ops.get_string(@h_out, "stdout", "h"),
          Ops.get_string(@d_out, "stdout", "d")
        ),
        Ops.get_string(@f_out, "stdout", "x")
      )
      Ops.set(
        @h_out,
        "stdout",
        Builtins.deletechars(Ops.get_string(@h_out, "stdout", ""), "\n")
      )
      if Builtins.sformat(
          "%1.%2",
          Ops.get_string(@h_out, "stdout", "h"),
          Ops.get_string(@d_out, "stdout", "d")
        ) !=
          Ops.get_string(@f_out, "stdout", "x")
        if !Popup.AnyQuestionRichText(
            _("illegal FQHN"),
            _(
              "the output of 'hostname -f' does not match the real hostname. The product will not install correctly."
            ),
            40,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      if Ops.get_string(@f_out, "stdout", "x") !=
          Builtins.tolower(Ops.get_string(@f_out, "stdout", "y"))
        if !Popup.AnyQuestionRichText(
            _("Illegal Hostname"),
            _(
              "Your hostname contains upper case characters. The product will not function correctly."
            ),
            40,
            10,
            _("Continue anyway"),
            _("Exit installation"),
            :focus_no
          )
          return :abort if Popup.ConfirmAbort(:incomplete)
        end
      end
      :auto
    end
  end
end

Yast::SusemanagerReqsClient.new.main
