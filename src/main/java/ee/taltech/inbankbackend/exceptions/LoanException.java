package ee.taltech.inbankbackend.exceptions;
/**
 * Custom exception class for various loan-related errors.
 */
public class LoanException extends Exception {
    public LoanException(String message) {
        super(message);
    }
    public LoanException(String message, Throwable cause) {
        super(message, cause);
    }
}

