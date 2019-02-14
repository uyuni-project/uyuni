# encoding: utf-8

module Yast
  class SusemanagerDbClient < Client
    def main
      Yast.import "UI"
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "FileUtils"
      Yast.import "GetInstArgs"
      Yast.import "Message"
      Yast.import "Package"
      Yast.import "Popup"
      Yast.import "Stage"
      Yast.import "Wizard"

      @args = GetInstArgs.argmap
      @ret = :auto

      @migration_file = Ops.add(Directory.tmpdir, "/susemanager_migration")
      @migration = FileUtils.Exists(@migration_file)
      if @migration
        Builtins.y2milestone(
          "migration was chosen, skipping this step"
        )
        return deep_copy(@ret)
      end

      @display_info = UI.GetDisplayInfo
      @text_mode = Ops.get_boolean(@display_info, "TextMode", false)

      @product_name = SCR.Read(path(".usr_share_rhn_config_defaults_rhn.product_name")) || "SUSE Manager"
      @invalid_pw_chars = "\\\"$'!+%`=\#@/"

      if @product_name == "Uyuni"
        db_user = "uyuni"
      else
        db_user = "susemanager"
      end

      @settings = {
        "DB_BACKEND"          => "postgresql",
        "MANAGER_DB_NAME"     => "",
        "MANAGER_DB_HOST"     => "localhost",
        "MANAGER_DB_PORT"     => "",
        "MANAGER_DB_PROTOCOL" => "TCP",
        "MANAGER_USER"        => db_user,
        "MANAGER_PASS"        => "",
        "MANAGER_PASS2"       => ""
      }

      @local_db = {
        "MANAGER_DB_NAME"     => db_user,
        "MANAGER_DB_HOST"     => "localhost",
        "MANAGER_DB_PROTOCOL" => "TCP"
      }

      @env_file = Ops.add(Directory.tmpdir, "/env_db")
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

      if @migration
        Ops.set(@settings, "MANAGER_USER", db_user)
        Ops.set(@settings, "MANAGER_PASS", "")
        Ops.set(@settings, "MANAGER_PASS2", "")
      end

      @contents = HBox(
        HSpacing(1),
          VBox(
          # text entry label
          InputField(
            Id("MANAGER_USER"),
            Opt(:hstretch),
            _("Database &User"),
            Ops.get(@settings, "MANAGER_USER", "")
          ),
          VSpacing(1),
          # text entry label
          Password(
            Id("MANAGER_PASS"),
            Opt(:hstretch),
            _("Database &Password"),
            Ops.get(@settings, "MANAGER_PASS", "")
          ),
          VSpacing(1),
          # text entry label
          Password(
            Id("MANAGER_PASS2"),
            Opt(:hstretch),
            _("R&epeat Password"),
            Ops.get(@settings, "MANAGER_PASS", "")
          ),
        ),
        HSpacing(1)
	)

      # help text
      @help_text = _(
        "<p>By default SUSE Manager is using an internal postgresql database named '" + db_user + "'. " +
        "If you want to use an external database or a custom database name, you will " +
        "need to modify the answer file created by this user interface (/root/setup_env.sh) " +
        "and run the setup procedure manually with the following command:</p>\n\n" +
        "<p>\n\n/usr/lib/susemanager/bin/mgr-setup -s</p>\n\n" +
        "after collecting all data with this user interface."
      )

      # dialog caption
      Wizard.SetContents(
        _("Database Settings"),
        @contents,
        @help_text,
        Ops.get_boolean(@args, "enable_back", true),
        Ops.get_boolean(@args, "enable_next", true)
      )
      UI.SetFocus(Id("MANAGER_USER"))

      while true
        @ret = UI.UserInput
        break if @ret == :back
        break if @ret == :abort && Popup.ConfirmAbort(:incomplete)
        if @ret == :next
          @pw1 = Convert.to_string(UI.QueryWidget(Id("MANAGER_PASS"), :Value))
          if @pw1 == ""
            Popup.Error(_("Password is missing."))
            UI.SetFocus(Id("MANAGER_PASS"))
            next
          end
          if @pw1 != UI.QueryWidget(Id("MANAGER_PASS2"), :Value)
            Popup.Error(_("Passwords do not match."))
            UI.SetFocus(Id("MANAGER_PASS"))
            next
          end
          #DB Password needs to be 7 chars long at least. Invalid chars: " $ ' !
          if Ops.less_than(Builtins.size(@pw1), 7)
            Popup.Error(
              Builtins.sformat(
                _("The password should have at least %1 characters."),
                7
              )
            )
            UI.SetFocus(Id("MANAGER_PASS"))
            next
          end
          if @pw1 != Builtins.deletechars(@pw1, @invalid_pw_chars)
            Popup.Error(
              Builtins.sformat(
                _(
                  "The password contains invalid characters.\nThe invalid characters are: %1"
                ),
                @invalid_pw_chars
              )
            )
            UI.SetFocus(Id("MANAGER_PASS"))
            next
          end
          @cracklib_check_result = Convert.to_string(
            SCR.Execute(path(".crack"), @pw1)
          )
          if @cracklib_check_result != ""
            Popup.Error(
              Builtins.sformat(
                _("The password failed the cracklib check with:\n'%1'"),
                @cracklib_check_result
              )
            )
            UI.SetFocus(Id("MANAGER_PASS"))
            next
          end
          @localdb = 1
          Builtins.foreach(@settings) do |key, value|
            val = ""
            if key == "DB_BACKEND"
              if @localdb
                if FileUtils.Exists("/etc/init.d/oracle")
                  Builtins.y2milestone("local db oracle detected")
                  val = "oracle"
                else
                  Builtins.y2milestone("local db postgres detected")
                  val = "postgresql"
                end
              else
                val = "postgresql"
              end

              oraclepkgs = [
                "spacewalk-oracle",
                "spacewalk-java-oracle",
                "spacewalk-backend-sql-oracle"
              ]
              postgrespkgs = [
                "spacewalk-postgresql",
                "spacewalk-java-postgresql",
                "spacewalk-backend-sql-postgresql"
              ]
              if val == "oracle"
                # install oracle packages
                install_packages = FilterPackageList(oraclepkgs, false)
                remove_packages = FilterPackageList(postgrespkgs, true)
                if Ops.greater_than(Builtins.size(install_packages), 0) ||
                    Ops.greater_than(Builtins.size(remove_packages), 0)
                  if !Package.DoInstallAndRemove(
                      install_packages,
                      remove_packages
                    )
                    Popup.Error(Message.FailedToInstallPackages)
                  end
                end
              elsif val == "postgresql"
                # install postgresql packages
                install_packages = FilterPackageList(postgrespkgs, false)
                remove_packages = FilterPackageList(oraclepkgs, true)
                if Ops.greater_than(Builtins.size(install_packages), 0) ||
                    Ops.greater_than(Builtins.size(remove_packages), 0)
                  if !Package.DoInstallAndRemove(
                      install_packages,
                      remove_packages
                    )
                    Popup.Error(Message.FailedToInstallPackages)
                  end
                end
              end
            else
              if key != "MANAGER_DB_HOST" && key != "MANAGER_DB_PORT" &&
                 key != "MANAGER_DB_PROTOCOL" && key != "MANAGER_DB_NAME"
                val = Convert.to_string(UI.QueryWidget(Id(key), :Value))
              end
            end
            if @localdb
              if Builtins.haskey(@local_db, key)
                val = Ops.get(@local_db, key, "")
              elsif val == "postgresql"
                Builtins.setenv("MANAGER_DB_PORT", "5432", true)
                SCR.Execute(
                  path(".target.bash"),
                  Builtins.sformat(
                    "echo \"export %1='%2'\" >> %3",
                    "MANAGER_DB_PORT",
                    "5432",
                    @env_file
                  )
                )
              elsif val == "oracle"
                Builtins.setenv("MANAGER_DB_PORT", "1521", true)
                SCR.Execute(
                  path(".target.bash"),
                  Builtins.sformat(
                    "echo \"export %1='%2'\" >> %3",
                    "MANAGER_DB_PORT",
                    "1521",
                    @env_file
                  )
                )
              elsif key == "MANAGER_DB_PORT"
                next
              end
            end
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
          if @localdb
            Builtins.setenv("LOCAL_DB", "1", true)
            SCR.Execute(
              path(".target.bash"),
              Builtins.sformat(
                "echo \"export %1='%2'\" >> %3",
                "LOCAL_DB",
                "1",
                @env_file
              )
            )
          else
            Builtins.setenv("LOCAL_DB", "0", true)
            SCR.Execute(
              path(".target.bash"),
              Builtins.sformat(
                "echo \"export %1='%2'\" >> %3",
                "LOCAL_DB",
                "0",
                @env_file
              )
            )
          end


          break
        end
      end

      deep_copy(@ret)
    end

    def FilterPackageList(_in, doRemove)
      _in = deep_copy(_in)
      ret = []

      Builtins.foreach(_in) do |pkg|
        if Package.Installed(pkg) && doRemove ||
            !Package.Installed(pkg) && !doRemove
          ret = Builtins.add(ret, pkg)
        end
      end
      deep_copy(ret)
    end
  end
end

Yast::SusemanagerDbClient.new.main
