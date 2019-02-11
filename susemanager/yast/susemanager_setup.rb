# encoding: utf-8

module Yast
  class SusemanagerSetupClient < Client
    def main
      Yast.import "GetInstArgs"
      Yast.import "Label"
      Yast.import "Wizard"
      Yast.import "FileUtils"

      Wizard.CreateDialog

      @dialog = [
        "susemanager_reqs",
        "susemanager_ask",
        "susemanager_migration",
        "susemanager_manager",
        "susemanager_cert",
        "susemanager_db",
        "susemanager_write",
        "susemanager_congratulate"
      ]

      @id = 0
      @result = :next
      @prev = :next

      while Ops.greater_or_equal(@id, 0) &&
          Ops.less_than(@id, Builtins.size(@dialog))
        @module_name = Ops.get_string(@dialog, @id, "")

        Builtins.y2debug("calling '%1'", @module_name)

        if @id == Ops.subtract(Builtins.size(@dialog), 1)
          Wizard.SetNextButton(:next, Label.FinishButton)
        elsif @id == Ops.subtract(Builtins.size(@dialog), 2)
          Wizard.SetNextButton(:next, Label.NextButton)
        end

        @result = WFM.CallFunction(@module_name, [GetInstArgs.argmap])

        @result = :cancel if @result == nil
        @result = deep_copy(@prev) if @result == :auto

        if @result == :cancel || @result == :abort
          break
        elsif @result == :next
          @id = Ops.add(@id, 1)
          @prev = :next
        elsif @result == :back
          @id = Ops.subtract(@id, 1)
          @prev = :back
        elsif @result == :finish
          @result = :next
          break
        end
      end

      Wizard.CloseDialog
      deep_copy(@result)
    end
  end
end

Yast::SusemanagerSetupClient.new.main
