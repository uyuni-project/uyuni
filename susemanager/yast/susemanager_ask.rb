# encoding: utf-8

module Yast
  class SusemanagerAskClient < Client
    def main
      Yast.import "UI"
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "Hostname"
      Yast.import "FileUtils"
      Yast.import "GetInstArgs"
      Yast.import "IP"
      Yast.import "Popup"
      Yast.import "Stage"
      Yast.import "Wizard"

      @args = GetInstArgs.argmap

      @migration_file = Ops.add(Directory.tmpdir, "/susemanager_migration")
      @migration = FileUtils.Exists(@migration_file)
      @product_name = SCR.Read(path(".usr_share_rhn_config_defaults_rhn.product_name")) || "SUSE Manager"

      if FileUtils.Exists(@migration_file)
        SCR.Execute(path(".target.remove"), @migration_file)
      end

      @contents = HBox(
        HSpacing(3),
        RadioButtonGroup(
          Id(:rb),
          VBox(
            Left(
              RadioButton(
                Id(:start),
                Opt(:notify),
                # radio button label
                _("Set up #{@product_name} from scratch"),
                !@migration
              )
            ),
            VSpacing(),
            Left(
              RadioButton(
                Id(:migration),
                Opt(:notify),
                # radio button label
                _("Migrate a #{@product_name} compatible server"),
                @migration
              )
            )
          )
        ),
        HSpacing(1)
      )


      # help text
      @help_text = _(
        "<p>Choose if you are setting up #{@product_name} from scratch or migrating to #{@product_name} from a #{@product_name} compatible server.</p>"
      )

      # dialog caption
      Wizard.SetContents(
        _("#{@product_name} Setup"),
        @contents,
        @help_text,
        Ops.get_boolean(@args, "enable_back", true),
        Ops.get_boolean(@args, "enable_next", true)
      )

      @ret = :back
      while true
        @ret = UI.UserInput
        break if @ret == :back
        break if @ret == :abort && Popup.ConfirmAbort(:incomplete)
        if @ret == :next
          if UI.QueryWidget(Id(:rb), :CurrentButton) == :migration
            Builtins.y2milestone("migration chosen")
            SCR.Execute(
              path(".target.bash"),
              Ops.add("/bin/touch ", @migration_file)
            )
          else
            Builtins.y2milestone("start from scratch chosen")
          end
          break
        end
      end

      deep_copy(@ret)
    end
  end
end

Yast::SusemanagerAskClient.new.main
