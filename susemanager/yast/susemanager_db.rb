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

      @display_info = UI.GetDisplayInfo
      @text_mode = Ops.get_boolean(@display_info, "TextMode", false)

      @invalid_pw_chars = "\"'!+%`=@/"

      @settings = {
        "DB_BACKEND"          => "postgresql",
        "MANAGER_DB_NAME"     => "",
        "MANAGER_DB_HOST"     => "localhost",
        "MANAGER_DB_PORT"     => "",
        "MANAGER_DB_PROTOCOL" => "TCP",
        "MANAGER_USER"        => "susemanager",
        "MANAGER_PASS"        => "",
        "MANAGER_PASS2"       => ""
      }

      @local_db = {
        "MANAGER_DB_NAME"     => "susemanager",
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
        Ops.set(@settings, "MANAGER_USER", "spacewalk")
        Ops.set(@settings, "MANAGER_PASS", "spacewalk")
        Ops.set(@settings, "MANAGER_PASS2", "spacewalk")
      end

      @contents = HBox(
        HSpacing(3),
        VBox(
          RadioButtonGroup(
            Id(:rb),
            Frame(
              "",
              HBox(
                HSpacing(0.2),
                VBox(
                  VSpacing(0.4), # hbox
                  Left(
                    RadioButton(
                      Id(:localdb),
                      Opt(:notify),
                      # radio button label
                      _("Embedded Database"),
                      Ops.get(@settings, "MANAGER_DB_HOST", "") == "localhost"
                    )
                  ),
                  @text_mode ? Empty() : VSpacing(),
                  Left(
                    RadioButton(
                      Id(:remotedb),
                      Opt(:notify),
                      # radio button label
                      _("Remote Database"),
                      Ops.get(@settings, "MANAGER_DB_HOST", "") != "localhost"
                    )
                  ),
                  HBox(
                    HSpacing(3),
                    VBox(
                      VSpacing(0.4),
                      Left(
                        RadioButtonGroup(
                          Id("DB_BACKEND"),
                          HBox(
                            HSpacing(0.2),
                            RadioButton(
                              Id(:postgres),
                              Opt(:notify),
                              # radio button label
                              _("postgres Database"),
                              Ops.get(@settings, "DB_BACKEND", "") == "postgresql"
                            ),
                            RadioButton(
                              Id(:oracle),
                              Opt(:notify),
                              # radio button label
                              _("oracle Database"),
                              Ops.get(@settings, "DB_BACKEND", "") == "oracle"
                            )
                          )
                        )
                      ),
                      # text entry label
                      InputField(
                        Id("MANAGER_DB_NAME"),
                        Opt(:hstretch),
                        _("&Database SID"),
                        Ops.get(@settings, "MANAGER_DB_NAME", "")
                      ),
                      # text entry label
                      InputField(
                        Id("MANAGER_DB_HOST"),
                        Opt(:hstretch),
                        _("&Hostname"),
                        Ops.get(@settings, "MANAGER_DB_HOST", "")
                      ),
                      HBox(
                        # text entry label
                        InputField(
                          Id("MANAGER_DB_PORT"),
                          Opt(:hstretch),
                          _("&Port"),
                          Ops.get(@settings, "MANAGER_DB_PORT", "")
                        ),
                        # text entry label
                        InputField(
                          Id("MANAGER_DB_PROTOCOL"),
                          Opt(:hstretch),
                          _("Pro&tocol"),
                          Ops.get(@settings, "MANAGER_DB_PROTOCOL", "")
                        )
                      )
                    )
                  )
                )
              )
            )
          ),
          VSpacing(0.4),
          # text entry label
          InputField(
            Id("MANAGER_USER"),
            Opt(:hstretch),
            _("Database &User"),
            Ops.get(@settings, "MANAGER_USER", "")
          ),
          # text entry label
          Password(
            Id("MANAGER_PASS"),
            Opt(:hstretch),
            _("Database &Password"),
            Ops.get(@settings, "MANAGER_PASS", "")
          ),
          # text entry label
          Password(
            Id("MANAGER_PASS2"),
            Opt(:hstretch),
            _("R&epeat Password"),
            Ops.get(@settings, "MANAGER_PASS", "")
          ),
          VSpacing(0.4)
        ),
        HSpacing(1)
      )

      # help text
      @help_text = _(
        "<p>Decide if to use the embedded or a remote database for SUSE Manager. If you select Local Database, Port and Protocol are set automatically.</p>\n" +
          "<p>For Remote Database you need to fill <b>Database SID</b> (Oracle System ID), Hostname, Port and Protocol.</p>\n" +
          "<p>\n" +
          "If you use the local database, set a user name and a password for the SUSE Manager database user that should be created. For a remote database, enter a user name that already exists in the database configuration and enter the correct password for this user</p>"
      )

      # dialog caption
      Wizard.SetContents(
        _("Database Settings"),
        @contents,
        @help_text,
        Ops.get_boolean(@args, "enable_back", true),
        Ops.get_boolean(@args, "enable_next", true)
      )
      UI.ChangeWidget(Id("MANAGER_DB_PORT"), :ValidChars, "1234567890")
      UI.SetFocus(Id(:localdb))

      Builtins.foreach(@local_db) do |key, value|
        UI.ChangeWidget(
          Id(key),
          :Enabled,
          Ops.get(@settings, "MANAGER_DB_HOST", "") != "localhost"
        )
      end
      UI.ChangeWidget(
        Id("MANAGER_DB_PORT"),
        :Enabled,
        Ops.get(@settings, "MANAGER_DB_HOST", "") != "localhost"
      )
      UI.ChangeWidget(
        Id("DB_BACKEND"),
        :Enabled,
        Ops.get(@settings, "MANAGER_DB_HOST", "") != "localhost"
      )

      while true
        @ret = UI.UserInput
        if @ret == :remotedb || @ret == :localdb
          Builtins.foreach(@local_db) do |key, value|
            UI.ChangeWidget(Id(key), :Enabled, @ret == :remotedb)
          end
          UI.ChangeWidget(Id("DB_BACKEND"), :Enabled, @ret == :remotedb)
          UI.ChangeWidget(Id("MANAGER_DB_PORT"), :Enabled, @ret == :remotedb)
        end
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
          @localdb = UI.QueryWidget(Id(:rb), :CurrentButton) == :localdb
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
              elsif UI.QueryWidget(Id(key), :CurrentButton) == :postgres
                Builtins.y2milestone("remote db postgres detected")
                val = "postgresql"
              else
                Builtins.y2milestone("remote db oracle detected")
                val = "oracle"
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
              val = Convert.to_string(UI.QueryWidget(Id(key), :Value))
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
