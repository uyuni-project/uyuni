# encoding: utf-8

module Yast
  class SusemanagerCertClient < Client
    def main
      Yast.import "UI"
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "FileUtils"
      Yast.import "GetInstArgs"
      Yast.import "Hostname"
      Yast.import "Popup"
      Yast.import "Stage"
      Yast.import "Wizard"

      @args = GetInstArgs.argmap
      @ret = :auto

      @migration_file = Ops.add(Directory.tmpdir, "/susemanager_migration")
      @migration = FileUtils.Exists(@migration_file)
      if @migration
        Builtins.y2milestone("migration was chosen, skipping this step")
        return deep_copy(@ret)
      end

      @invalid_pw_chars = "\"$'!+%`=\#@/"
      @valid_chars = ",.:;#'+*~?][(){}/§&%$\"!@0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_- "

      @settings = {
        "CERT_O"       => "",
        "CERT_OU"      => "",
        "CERT_CITY"    => "",
        "CERT_STATE"   => "",
        "CERT_COUNTRY" => "DE",
        "CERT_EMAIL"   => "",
        "CERT_PASS"    => "",
        "CERT_PASS2"   => ""
      }

      @labels = {
        # text entry label
        "CERT_O"       => _("Organization"),
        # text entry label
        "CERT_OU"      => _("Organization Unit"),
        # text entry label
        "CERT_CITY"    => _("City"),
        # text entry label
        "CERT_STATE"   => _("State"),
        # text entry label
        "CERT_COUNTRY" => _("Country"),
        # text entry label
        "CERT_EMAIL"   => _("E-mail")
      }

      @env_file = Ops.add(Directory.tmpdir, "/env_cert")
      if FileUtils.Exists(@env_file)
        SCR.Execute(path(".target.remove"), @env_file)
      end
      SCR.Execute(
        path(".target.bash"),
        Builtins.sformat("/usr/bin/touch %1; /bin/chmod 0600 %1;", @env_file)
      )
      if Ops.get(@settings, "CERT_EMAIL", "") == ""
        Ops.set(@settings, "CERT_EMAIL", Builtins.getenv("MANAGER_ADMIN_EMAIL"))
      end

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
          InputField(
            Id("CERT_O"),
            Opt(:hstretch),
            Ops.get(@labels, "CERT_O", ""),
            Ops.get(@settings, "CERT_O", "")
          ),
          InputField(
            Id("CERT_OU"),
            Opt(:hstretch),
            Ops.get(@labels, "CERT_OU", ""),
            Ops.get(@settings, "CERT_OU", "")
          ),
          InputField(
            Id("CERT_CITY"),
            Opt(:hstretch),
            Ops.get(@labels, "CERT_CITY", ""),
            Ops.get(@settings, "CERT_CITY", "")
          ),
          InputField(
            Id("CERT_STATE"),
            Opt(:hstretch),
            Ops.get(@labels, "CERT_STATE", ""),
            Ops.get(@settings, "CERT_STATE", "")
          ),
          InputField(
            Id("CERT_COUNTRY"),
            Opt(:hstretch),
            Ops.get(@labels, "CERT_COUNTRY", ""),
            Ops.get(@settings, "CERT_COUNTRY", "")
          ),
          # password entry label
          Password(
            Id("CERT_PASS"),
            Opt(:hstretch),
            _("SSL Pass&word"),
            Ops.get(@settings, "CERT_PASS", "")
          ),
          # text entry label
          Password(
            Id("CERT_PASS2"),
            Opt(:hstretch),
            _("R&epeat Password"),
            Ops.get(@settings, "CERT_PASS2", "")
          ),
          VSpacing(0.5)
        ),
        HSpacing(1)
      )


      # help text
      @help_text = _(
        "<p>Here, enter data needed for the creation of an SSL certificate. The certificate is used for a number of purposes like connections to a proxy, HTTPS protocol in browsers, and more.</p>"
      )

      # dialog caption
      Wizard.SetContents(
        _("Certificate Setup"),
        @contents,
        @help_text,
        Ops.get_boolean(@args, "enable_back", true),
        Ops.get_boolean(@args, "enable_next", true)
      )

      UI.ChangeWidget(Id("CERT_O"), :ValidChars, @valid_chars)
      UI.ChangeWidget(Id("CERT_OU"), :ValidChars, @valid_chars)
      UI.ChangeWidget(Id("CERT_CITY"), :ValidChars, @valid_chars)
      UI.ChangeWidget(Id("CERT_STATE"), :ValidChars, @valid_chars)
      UI.ChangeWidget(
        Id("CERT_COUNTRY"),
        :ValidChars,
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      )
      UI.ChangeWidget(Id("CERT_COUNTRY"), :InputMaxLength, 2)
      UI.SetFocus(Id("CERT_O"))

      while true
        @ret = UI.UserInput
        break if @ret == :back
        break if @ret == :abort && Popup.ConfirmAbort(:incomplete)
        if @ret == :next
          @missing = false
          Builtins.foreach(
            ["CERT_O", "CERT_OU", "CERT_CITY", "CERT_STATE", "CERT_COUNTRY"]
          ) do |key|
            val = Convert.to_string(UI.QueryWidget(Id(key), :Value))
            if val == ""
              label = Ops.get(@labels, key, key)
              Popup.Error(
                Builtins.sformat(_("The value of '%1' is empty."), label)
              )
              UI.SetFocus(Id(key))
              @missing = true
              raise Break
            end
          end
          next if @missing

          @pw1 = Convert.to_string(UI.QueryWidget(Id("CERT_PASS"), :Value))
          if @pw1 == ""
            Popup.Error(_("Password is missing."))
            UI.SetFocus(Id("CERT_PASS"))
            next
          end
          if @pw1 != UI.QueryWidget(Id("CERT_PASS2"), :Value)
            Popup.Error(_("Passwords do not match."))
            UI.SetFocus(Id("CERT_PASS"))
            next
          end

          if Ops.less_than(Builtins.size(@pw1), 7)
            Popup.Error(
              Builtins.sformat(
                _("The password should have at least %1 characters."),
                7
              )
            )
            UI.SetFocus(Id("CERT_PASS"))
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
            UI.SetFocus(Id("CERT_PASS"))
            next
          end

          Builtins.foreach(@settings) do |key, value|
            val = value
            if UI.WidgetExists(Id(key))
              val = Convert.to_string(UI.QueryWidget(Id(key), :Value))
            end
            val = Builtins.toupper(val) if key == "CERT_COUNTRY"
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
  end
end

Yast::SusemanagerCertClient.new.main
