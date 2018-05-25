# encoding: utf-8

module Yast
  class SusemanagerSccClient < Client
    def main
      Yast.import "UI"
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "FileUtils"
      Yast.import "GetInstArgs"
      Yast.import "Label"
      Yast.import "Popup"
      Yast.import "Stage"
      Yast.import "String"
      Yast.import "Wizard"

      @args = GetInstArgs.argmap
      @ret = :auto

      @display_info = UI.GetDisplayInfo
      @text_mode = Ops.get_boolean(@display_info, "TextMode", false)

      @settings = { "SCC_USER" => "", "SCC_PASS" => "", "ISS_PARENT" => "" }

      @log_view_ID = nil

      @labels = {
        # text entry label
        "SCC_USER"   => _(
          "SCC Organization Credentials Username"
        ),
        # text entry label
        "SCC_PASS"   => _(
          "SCC Organization Credentials Password"
        ),
        # text entry label
        "ISS_PARENT" => _("Parent Server Name")
      }
      @product_name = SCR.Read(path(".usr_share_rhn_config_defaults_rhn.product_name")) || "SUSE Manager"

      @env_file = Ops.add(Directory.tmpdir, "/env_cc")
      if FileUtils.Exists(@env_file)
        SCR.Execute(path(".target.remove"), @env_file)
      end
      SCR.Execute(
        path(".target.bash"),
        Builtins.sformat("/usr/bin/touch %1; /bin/chmod 0600 %1;", @env_file)
      )

      # read existing values, if present
      Builtins.foreach(@settings) do |key, value|
        val = Builtins.getenv(key)
        if val != nil && val != ""
          Builtins.y2internal("value for %1 present: %2", key, val)
          Ops.set(@settings, key, val)
        end
      end

      @contents = HBox(
        HSpacing(1),
        VBox(
          RadioButtonGroup(
            Id(:rb),
            Frame(
              "",
              HBox(
                HSpacing(0.2),
                VBox(
                  VSpacing(0.4),
                  Left(
                    RadioButton(
                      Id(:scc),
                      Opt(:notify),
                      # radio button label
                      _("Connect to SCC"),
                      Ops.get(@settings, "ISS_PARENT", "") == ""
                    )
                  ),
                  HBox(
                    HSpacing(4),
                    VBox(
                      # text entry label
                      InputField(
                        Id("SCC_USER"),
                        Opt(:hstretch),
                        _("SCC Organization Credentials &Username"),
                        Ops.get(@settings, "SCC_USER", "")
                      ),
                      # text entry label
                      Password(
                        Id("SCC_PASS"),
                        Opt(:hstretch),
                        _("SCC Organization Credentials &Password"),
                        Ops.get(@settings, "SCC_PASS", "")
                      ),
                      VSpacing(),
                      PushButton(
                        Id("test_NU_credentials"),
                        Opt(:key_F6),
                        _("&Test...")
                      )
                    )
                  ),
                  @text_mode ? Empty() : VSpacing(),
                  Left(
                    RadioButton(
                      Id(:iss),
                      Opt(:notify),
                      # radio button label
                      _("Connect to #{@product_name} for inter-server sync"),
                      Ops.get(@settings, "ISS_PARENT", "") != ""
                    )
                  ),
                  HBox(
                    HSpacing(4),
                    # text entry label
                    InputField(
                      Id("ISS_PARENT"),
                      Opt(:hstretch),
                      _("Parent &Server Name"),
                      Ops.get(@settings, "ISS_PARENT", "")
                    )
                  ),
                  @text_mode ? Empty() : VSpacing(),
                  Left(
                    RadioButton(
                      Id(:skip),
                      Opt(:notify),
                      # radio button label
                      _("Skip Connection Setup"),
                      false
                    )
                  )
                )
              )
            )
          ),
          VSpacing(0.5)
        ),
        HSpacing(1)
      )

      # help text
      @help_text = _(
        "<p>Here, enter organization credentials (mirror credentials) from the SUSE Customer Center.</p>\n" \
        "<p>The connection to SCC or to another #{@product_name} Server can be configured later if needed.</p>"
      )

      # dialog caption
      Wizard.SetContents(
        _("SCC Settings"),
        @contents,
        @help_text,
        Ops.get_boolean(@args, "enable_back", true),
        Ops.get_boolean(@args, "enable_next", true)
      )
      UI.SetFocus(:scc)
      UI.ChangeWidget(Id("ISS_PARENT"), :Enabled, false)

      while true
        @ret = UI.UserInput
        if @ret == :scc || @ret == :iss || @ret == :skip
          Ops.set(@settings, "ISS_PARENT", "")
          UI.ChangeWidget(Id("ISS_PARENT"), :Enabled, @ret == :iss)
          UI.ChangeWidget(Id("SCC_USER"), :Enabled, @ret == :scc)
          UI.ChangeWidget(Id("SCC_PASS"), :Enabled, @ret == :scc)
          UI.ChangeWidget(Id("test_NU_credentials"), :Enabled, @ret == :scc)
        end
        break if @ret == :back
        break if @ret == :abort && Popup.ConfirmAbort(:incomplete)
        break if @ret == :next && UI.QueryWidget(Id(:rb), :CurrentButton) == :skip
        if @ret == "test_NU_credentials" || @ret == :next
          @missing = false
          @isscc = UI.QueryWidget(Id(:rb), :CurrentButton) == :scc
          Builtins.foreach(@settings) do |key, value|
            val = Convert.to_string(UI.QueryWidget(Id(key), :Value))
            if @isscc
              if key == "SCC_USER" || key == "SCC_PASS"
                if val == ""
                  # error label
                  Popup.Error(
                    Builtins.sformat(
                      _("The value of '%1' is empty."),
                      Ops.get(@labels, key, key)
                    )
                  )
                  UI.SetFocus(Id(key))
                  @missing = true
                  raise Break
                end
                Ops.set(@settings, key, val)
              else
                Ops.set(@settings, key, "")
              end
            else
              if key == "ISS_PARENT"
                if val == ""
                  # error label
                  Popup.Error(
                    Builtins.sformat(
                      _("The value of '%1' is empty."),
                      Ops.get(@labels, key, key)
                    )
                  )
                  UI.SetFocus(Id(key))
                  @missing = true
                  raise Break
                end
                Ops.set(@settings, key, val)
              else
                Ops.set(@settings, key, "")
              end
            end
          end
          if @missing
            @ret = :not_next
            next
          end
        end
        TestCredentials() if @ret == "test_NU_credentials"
        if @ret == :next
          Builtins.foreach(@settings) do |key, val|
            Builtins.setenv(key, val, true)
            SCR.Execute(
              path(".target.bash"),
              Builtins.sformat(
                "echo \"export %1='%2'\" >> %3",
                key,
                val,
                @env_file
              )
            )
          end

          break
        end
      end

      deep_copy(@ret)
    end

    # Function for logging in the LogView widget.
    # @param [String] text to be logged
    def LogThis(text)
      if UI.WidgetExists(Id(@log_view_ID))
        UI.ChangeWidget(Id(@log_view_ID), :LastLine, Ops.add(text, "\n"))
      end

      nil
    end

    # Gets the current credentials and use them to download a list of available
    # repositories from scc.suse.com. Progress is written to the LogView
    # identified by ID got as a function parameter.
    #
    # @param [Object] log_view widget ID
    def CredentialsTest(log_view)
      log_view = deep_copy(log_view)
      @log_view_ID = deep_copy(log_view)

      user = Ops.get(@settings, "SCC_USER", "")
      pass = Ops.get(@settings, "SCC_PASS", "")
      serv = Ops.get(@settings, "ISS_PARENT", "")
      serv = "scc.suse.com" if serv == ""
      url = Builtins.sformat("https://%1", serv)

      # File for writing the credentials
      test_file = Ops.add(Directory.tmpdir, "/curl_input_file")

      # At first, credentials need to be written to a temporary file
      # because of security reasons. If used on a commandline, `ps`
      # could reveal them.

      # TRANSLATORS: LogView line
      LogThis(_("Creating a temporary file..."))

      cmd_exit = Convert.to_integer(
        SCR.Execute(
          path(".target.bash"),
          Builtins.sformat(
            "echo \"# URL for downloading repos/patches\n" +
              "url=\\\"\\\"\n" +
              "# user:pass to be used for downloading\n" +
              "user=\\\"\\\"\" > '%1'",
            String.Quote(test_file)
          )
        )
      )

      if cmd_exit != 0
        # TRANSLATORS: LogView line
        LogThis(
          Builtins.sformat(_("Cannot create a temporary file %1."), test_file)
        )

        return false
      end

      # TRANSLATORS: LogView line
      LogThis(_("Writing credentials to a temporary file..."))
      if !SCR.RegisterAgent(
          path(".curlTempFile"),
          term(:ag_ini, term(:SysConfigFile, test_file))
        )
        Builtins.y2error("Cannot register agent")
        # TRANSLATORS: LogView line
        LogThis(
          Builtins.sformat(_("Cannot write to a temporary file %1."), test_file)
        )

        return false
      end

      SCR.Read(Builtins.add(path(".curlTempFile"), "url"))
      SCR.Read(Builtins.add(path(".curlTempFile"), "user"))

      # NUUrl
      if !SCR.Write(
          Builtins.add(path(".curlTempFile"), "url"),
          Builtins.sformat("%1/connect/organizations/repositories", url)
        )
        Builtins.y2error("Writing url failed")
      end

      # NUUser and NUPass
      # user:pass, all ":" in user or pass need to be escaped
      userpass = Builtins.sformat(
        "%1:%2",
        Builtins.mergestring(Builtins.splitstring(user, ":"), "\\:"),
        Builtins.mergestring(Builtins.splitstring(pass, ":"), "\\:")
      )

      if !SCR.Write(Builtins.add(path(".curlTempFile"), "user"), userpass)
        Builtins.y2error("Writing user failed")
      end

      if !SCR.Write(path(".curlTempFile"), nil)
        Builtins.y2error("Cannot write credentials")
        # TRANSLATORS: LogView line
        LogThis(
          Builtins.sformat(_("Cannot write to a temporary file %1."), test_file)
        )

        return false
      end

      if !SCR.UnregisterAgent(path(".curlTempFile"))
        Builtins.y2error("Cannot unregister agent")
      end
      insecure = ""
      insecure = "--insecure" if serv != "scc.suse.com"

      # TRANSLATORS: LogView line
      LogThis(
        Builtins.sformat(
          _("Downloading list of available repositories from %1..."),
          url
        )
      )
      cmd = Convert.to_map(
        SCR.Execute(
          path(".target.bash_output"),
          Builtins.sformat(
            "/usr/bin/curl -I -w '%%{http_code}' %1 --silent --config '%2' -o /dev/null",
            String.Quote(insecure),
            String.Quote(test_file)
          )
        )
      )

      if Ops.get_string(cmd, "stdout", "") == "200"
        LogThis(_("Downloaded list of repositories successfully."))

        return true
      else
        LogThis(_("Cannot download list of repositories."))
        LogThis(
          Builtins.sformat(
            _("Reason: Response code %1"),
            Ops.get_string(cmd, "stdout", "")
          )
        )

        return false
      end
    end

    # dialog for testing SCC connection
    def TestCredentials
      UI.OpenDialog(
        MinSize(
          52,
          12,
          VBox(
            # TRANSLATORS: LogView label
            LogView(Id("test_log"), _("&Test Details"), 5, 100),
            VSpacing(1),
            PushButton(Id(:ok), Opt(:default, :key_F10), Label.OKButton)
          )
        )
      )

      # complex.ycp
      ret = CredentialsTest("test_log")


      if ret == true
        # TRANSLATORS: LogView line
        UI.ChangeWidget(
          Id("test_log"),
          :LastLine,
          "\n" + _("Test result: success") + "\n"
        )
      else
        # TRANSLATORS: LogView line
        UI.ChangeWidget(
          Id("test_log"),
          :LastLine,
          "\n" + _("Test result: failure") + "\n"
        )
      end

      UI.UserInput
      UI.CloseDialog

      ret
    end
  end
end

Yast::SusemanagerSccClient.new.main
