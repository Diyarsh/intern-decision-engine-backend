package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.common.Gender;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import ee.taltech.inbankbackend.exceptions.LoanException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
public class DecisionEngine {

    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final EstonianPersonalCodeParser personalCodeParser = new EstonianPersonalCodeParser();

    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, LoanException {
        verifyInputs(personalCode, loanAmount, loanPeriod);
        int creditModifier = getCreditModifier(personalCode);
        verifyAgeRestriction(personalCode, loanPeriod, creditModifier);  // Pass loanPeriod and creditModifier

        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found due to low credit score segment.");
        }

        int outputLoanAmount = highestValidLoanAmount(creditModifier, loanPeriod); // Pass creditModifier
        return new Decision(outputLoanAmount, loanPeriod, null);
    }

    private int highestValidLoanAmount(int creditModifier, int loanPeriod) {
        return creditModifier * loanPeriod;
    }

    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));
        if (segment < 2500) return 0;
        if (segment < 5000) return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        if (segment < 7500) return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {
        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code.");
        }
        if (loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT || loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT) {
            throw new InvalidLoanAmountException("Loan amount must be between " + DecisionEngineConstants.MINIMUM_LOAN_AMOUNT +
                    " and " + DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + " euros.");
        }
        if (loanPeriod < DecisionEngineConstants.MINIMUM_LOAN_PERIOD || loanPeriod > DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            throw new InvalidLoanPeriodException("Loan period must be between " + DecisionEngineConstants.MINIMUM_LOAN_PERIOD +
                    " and " + DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + " months.");
        }
    }

    private LocalDate parseBirthDate(String personalCode) {
        // Assuming the Estonian personal code format is correct and the date starts at position 1
        String year = personalCode.substring(1, 3);
        String month = personalCode.substring(3, 5);
        String day = personalCode.substring(5, 7);
        String centuryIndicator = personalCode.substring(0, 1);

        int century = (centuryIndicator.equals("3") || centuryIndicator.equals("4")) ? 1900 : 2000;
        int yearFull = century + Integer.parseInt(year);

        return LocalDate.parse(yearFull + "-" + month + "-" + day, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private void verifyAgeRestriction(String personalCode, int loanPeriod, int creditModifier)
            throws LoanException {
        try {
            LocalDate birthDate = parseBirthDate(personalCode);  // Replace with your own method if necessary
            Gender gender = personalCodeParser.getGender(personalCode);  // Handle the exception
            LocalDate today = LocalDate.now();
            Period age = Period.between(birthDate, today);
            Period expectedLifetime = (gender == Gender.MALE) ? DecisionEngineConstants.EXPECTED_LIFETIME_MEN
                    : DecisionEngineConstants.EXPECTED_LIFETIME_WOMEN;

            if (age.getYears() < 18) {
                throw new LoanException(DecisionEngineConstants.UNDERAGED_CLIENT_MESSAGE);
            }

            LocalDate loanEnd = today.plusMonths(loanPeriod);
            if (!birthDate.plus(expectedLifetime).isAfter(loanEnd)) {
                throw new LoanException(DecisionEngineConstants.OVERAGED_CLIENT_MESSAGE);
            }
        } catch (PersonalCodeException e) {
            throw new LoanException("Error parsing personal code: " + e.getMessage(), e);
        }
    }
}



