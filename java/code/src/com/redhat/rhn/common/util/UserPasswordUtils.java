package com.redhat.rhn.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionMessage;

import com.redhat.rhn.common.conf.UserDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.common.SatConfigFactory;

public class UserPasswordUtils {
    
    private UserPasswordUtils(){}

    private static final String PSW_LENGHT_REGEX = ".{%s,%s}";
    private static final String PSW_LOWERCHAR_REGEX = "(?<lowerchar>=.*[a-z])";
    private static final String PSW_UPPERCHAR_REGEX = "(?<upperchar>=.*[A-Z])";
    private static final String PSW_DIGIT_REGEX = "(?<digits>=.*[0-9])";
    private static final String PSW_NO_CONSECUTIVE_CHAR_REGEX = "^(?!.*__)(?!.*_$)[A-Za-z]\\w*";
    private static final String PSW_SPECIAL_CHAR_REGEX = "(?<specials>=.*[%s])";
    private static final String PSW_MAX_OCCURENCE_REGEX = "$";
    private static final String PSW_NO_SPACE_TAB_NEWLINE_REGEX = "(?<spaces>=.[\\t\\n\\S+$])";

    public static Map<String, String> validatePassword(String pw) {
        // Validate the password
        Map<String, String> errors = new HashMap<>();
        Pattern validationRegex = Pattern.compile(buildRegexValidationPattern());
        Matcher validationMatcher = validationRegex.matcher(pw);
        if(validationMatcher.matches()) {
            return errors;
        }
        if(validationMatcher.group("lowerchar").isEmpty()) {
            errors.put(
                LocalizationService.getInstance().getMessage("error.minpassword"),
                SatConfigFactory.getSatConfigValue(SatConfigFactory.PSW_CHECK_LENGHT_MIN)
            );
        }
        if(validationMatcher.group("upperchar").isEmpty()) {
            errors.put(
                LocalizationService.getInstance().getMessage("error.maxpassword"),
                SatConfigFactory.getSatConfigValue(SatConfigFactory.PSW_CHECK_LENGHT_MIN)
            );
        }
        if(validationMatcher.group("digits").isEmpty()) {
            errors.put(
                LocalizationService.getInstance().getMessage("error.nodigits"),
                ""
            );
        }
        if(validationMatcher.group("specials").isEmpty()) {
            errors.put(
                LocalizationService.getInstance().getMessage("error.nospecialcharacters"),
                ""
            );
        }
        if(!validationMatcher.group("spaces").isEmpty()) {
            errors.put(
                LocalizationService.getInstance().getMessage("error.spacesnotallowed"),
                ""
            );
        }
        return errors;
    }

    private static String buildRegexValidationPattern() {
        StringBuilder regexPatternBuilder = new StringBuilder(PSW_NO_SPACE_TAB_NEWLINE_REGEX);
        if(SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_DIGIT_FLAG)) {
            regexPatternBuilder.append(PSW_DIGIT_REGEX);
        }
        if(SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_LOWER_CHAR_FLAG)) {
            regexPatternBuilder.append(PSW_LOWERCHAR_REGEX);
        }
        if(SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_UPPER_CHAR_FLAG)) {
            regexPatternBuilder.append(PSW_UPPERCHAR_REGEX);
        }
        if(SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_CONSECUTIVE_CHAR_FLAG)) {
            regexPatternBuilder.append(PSW_NO_CONSECUTIVE_CHAR_REGEX);
        }
        if(SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_SPECIAL_CHAR_FLAG)) {
            regexPatternBuilder.append(PSW_NO_CONSECUTIVE_CHAR_REGEX);
        }
        if(SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_SPECIAL_CHAR_FLAG)) {
            String specialCharRegex = String.format(
                PSW_SPECIAL_CHAR_REGEX,
                SatConfigFactory.getSatConfigValue(SatConfigFactory.PSW_CHECK_SPECIAL_CHARACTERS)
                );
            regexPatternBuilder.append(specialCharRegex);
        }
        if(SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_CONSECUTIVE_CHAR_FLAG)) {
            String specialCharRegex = String.format(
                PSW_MAX_OCCURENCE_REGEX,
                SatConfigFactory.getSatConfigValue(SatConfigFactory.PSW_CHECK_MAX_OCCURENCE)
                );
            regexPatternBuilder.append(specialCharRegex);
        }
        String lenghtRegex = String.format(
            PSW_LENGHT_REGEX,
            SatConfigFactory.getSatConfigValue(SatConfigFactory.PSW_CHECK_LENGHT_MIN),
            SatConfigFactory.getSatConfigValue(SatConfigFactory.PSW_CHECK_LENGHT_MAX)
            );
        regexPatternBuilder.append(lenghtRegex);
        return regexPatternBuilder.toString();
    }

}
