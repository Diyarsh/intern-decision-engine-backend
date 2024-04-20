package ee.taltech.inbankbackend.config;
import java.time.Period;

/**
 * Holds all necessary constants for the decision engine.
 */
public class DecisionEngineConstants {
    public static final Integer MINIMUM_LOAN_AMOUNT = 2000;
    public static final Integer MAXIMUM_LOAN_AMOUNT = 10000;
    public static final Integer MAXIMUM_LOAN_PERIOD = 60;
    public static final Integer MINIMUM_LOAN_PERIOD = 12;
    public static final Integer SEGMENT_1_CREDIT_MODIFIER = 100;
    public static final Integer SEGMENT_2_CREDIT_MODIFIER = 300;
    public static final Integer SEGMENT_3_CREDIT_MODIFIER = 1000;

    // Expected lifetime for men.
    public static final Period EXPECTED_LIFETIME_MEN = Period.ofYears(75).withMonths(7);
    // Expected lifetime for women.
    public static final Period EXPECTED_LIFETIME_WOMEN = Period.ofYears(85).withMonths(4);
    // Legal age.
    public static final Period LEGAL_AGE = Period.ofYears(18);

    // Messages for age-related loan decisions.
    public static final String UNDERAGED_CLIENT_MESSAGE = "The client is too young.";
    public static final String OVERAGED_CLIENT_MESSAGE = "The client is older than the age limit for this loan.";
}
