# encoding: utf-8

module Yast
  class SusemanagerWriteClient < Client
    def main
      Yast.import "UI"
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "FileUtils"
      Yast.import "Popup"
      Yast.import "Stage"
      Yast.import "Wizard"

      @dir = Directory.tmpdir
      @env_file = "/root/setup_env.sh"
      @logfile = "/var/log/susemanager_setup.log"
      @errfile = "/var/log/susemanager_setup.err"
      @ret = :back

      @display_info = UI.GetDisplayInfo
      @text_mode = Ops.get_boolean(@display_info, "TextMode", false)

      Builtins.foreach([@env_file, @logfile, @errfile]) do |file|
        SCR.Execute(path(".target.remove"), file) if FileUtils.Exists(file)
      end
      SCR.Execute(
        path(".target.bash"),
        Builtins.sformat("/usr/bin/touch %1; /bin/chmod 0600 %1;", @env_file)
      )

      @migration_file = Ops.add(Directory.tmpdir, "/susemanager_migration")
      @migration = FileUtils.Exists(@migration_file)
      @product_name = SCR.Read(path(".usr_share_rhn_config_defaults_rhn.product_name")) || "SUSE Manager"

      Builtins.foreach(
        ["env_force", "env_migration", "env_manager", "env_db", "env_cert", "env_cc"]
      ) do |file|
        file_path = Builtins.sformat("%1/%2", @dir, file)
        if FileUtils.Exists(file_path)
          SCR.Execute(
            path(".target.bash"),
            Builtins.sformat("cat %1 >> %2", file_path, @env_file)
          )
        end
      end


      @cont = VBox(
        VSpacing(0.4),
        ReplacePoint(
          Id(:rp_label),
          # text label
          Label(Id(:label), _("Setup is prepared."))
        ),
        @text_mode ?
          VBox(
            # label
            Left(Label(_("Setup script output"))),
            LogView(Id(:stdout), "", 6, 0),
            VSpacing(0.4),
            # label
            Left(Label(_("Error output"))),
            LogView(Id(:stderr), "", 2, 0)
          ) :
          VBox(
            # label
            Left(Label(_("Setup script output"))),
            VWeight(3, LogView(Id(:stdout), "", 8, 0)),
            VSpacing(0.4),
            # label
            Left(Label(_("Error output"))),
            VWeight(1, LogView(Id(:stderr), "", 4, 0))
          )
      )

      if @migration
        @cont = VBox(
          VSpacing(2),
          # text label
          Label(
            _(
              "Now you can start the migration process using the mgr-setup script.\n" +
                "View the available options with /usr/lib/susemanager/bin/mgr-setup -h\n" +
                "\n" +
                "Be aware that this process can take more than 10 hours,\n" +
                "depending on the configuration of your #{@product_name} server.\n" +
                "\n" +
                "For more information on how to migrate a #{@product_name} server,\n" +
                "refer to the #{@product_name} Quick Start."
            )
          ),
          VSpacing(2)
        )
      end

      @contents = HBox(
        HSpacing(1),
        VBox(VSpacing(0.4), @cont, VSpacing(0.4)),
        HSpacing(1)
      )

      @help_text = @migration ?
        "" :
        # help text
        _(
          "<p>Now, the configuration script is running, and it will take some time.</p>\n<p>The script output and possible error output can be watched on the screen.</p>"
        )

      # dialog caption
      Wizard.SetContents(_("Write Settings"), @contents, @help_text, true, true)

      while @migration
        @ret = UI.UserInput
        if @ret == :abort && !Popup.ConfirmAbort(:incomplete)
          next
        else
          return deep_copy(@ret)
        end
      end

      if !Popup.YesNo(_("Now you can either start the setup process directly or exit the\n" +
                        "user interface and make custom modifications to the answer file\n" +
                        "/root/setup_env.sh. If you choose to do so, you will need to run\n" +
                        "the actual setup manually by running\n\n" +
                        "/usr/lib/susemanager/bin/mgr-setup -s\n\n" +
                        "Run setup now?"))
        return :abort
      end

      UI.ReplaceWidget(
        Id(:rp_label),
        # text label
        Label(Id(:label), _("Setup is running. Please wait..."))
      )

      @pid = -1

      UI.BusyCursor
      Wizard.DisableNextButton
      Wizard.DisableBackButton

      @cmd = Builtins.sformat("/usr/lib/susemanager/bin/mgr-setup -s")
      @pid = Convert.to_integer(SCR.Execute(path(".process.start_shell"), @cmd))
      @status = 0

      while true
        @ret = Convert.to_symbol(UI.PollInput)
        if SCR.Read(path(".process.running"), @pid) != true
          update_output
          # explicitely check the process buffer after exit (bnc#488799)
          @buf = Convert.to_string(SCR.Read(path(".process.read"), @pid))
          @err_buf = Convert.to_string(
            SCR.Read(path(".process.read_stderr"), @pid)
          )
          if @buf != nil && @buf != ""
            UI.ChangeWidget(Id(:stdout), :LastLine, Ops.add(@buf, "\n"))
          end
          if @err_buf != nil && @err_buf != ""
            UI.ChangeWidget(Id(:stderr), :LastLine, Ops.add(@err_buf, "\n"))
          end

          @status = Convert.to_integer(SCR.Read(path(".process.status"), @pid))
          Builtins.y2internal("exit status of the script: %1", @status)
          # text label
          @message = _("Setup is completed.")
          if @status != 0
            # text label
            @message = _("Setup failed.")
          end
          UI.ReplaceWidget(
            Id(:rp_label),
            # text label
            Label(Id(:label), Opt(:boldFont), @message)
          )
          break
        else
          update_output
        end
        if @ret == :cancel || @ret == :abort
          SCR.Execute(path(".process.kill"), @pid, 15)
          UI.ReplaceWidget(
            Id(:rp_label),
            # text label
            Label(Id(:label), Opt(:boldFont), _("Setup has been aborted."))
          )
          break
        end
        Builtins.sleep(100)
      end

      SCR.Execute(path(".process.kill"), @pid)

      # save the logs
      @stdout = Convert.to_string(UI.QueryWidget(Id(:stdout), :Value))
      SCR.Write(path(".target.string"), @logfile, @stdout)
      @stderr = Convert.to_string(UI.QueryWidget(Id(:stderr), :Value))
      SCR.Write(path(".target.string"), @errfile, @stderr)

      UI.NormalCursor

      Wizard.EnableBackButton
      Wizard.EnableNextButton

      while true
        @ret = UI.UserInput
        return nil if @status != 0
        if @ret == :abort && !Popup.ConfirmAbort(:incomplete)
          next
        else
          return deep_copy(@ret)
        end
      end

      nil
    end

    def update_output
      line = Convert.to_string(SCR.Read(path(".process.read_line"), @pid))
      if line != nil && line != ""
        UI.ChangeWidget(Id(:stdout), :LastLine, Ops.add(line, "\n"))
      end
      err = Convert.to_string(SCR.Read(path(".process.read_line_stderr"), @pid))
      if err != nil && err != ""
        UI.ChangeWidget(Id(:stderr), :LastLine, Ops.add(err, "\n"))
      end

      nil
    end
  end
end

Yast::SusemanagerWriteClient.new.main
