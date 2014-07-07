/**
 * 
 */
package org.fosstrak.ale.exception;

/**
 * business exception for the ALE cc spec validation exception. this exception is not used on WSDL interfaces - only ALE internal.
 * 
 * @author swieland
 * @author Wondeuk Yoon
 *
 */
public class CCSpecValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * empty implementation exception.
	 */
	public CCSpecValidationException() {
		super();
	}

	/**
	 * @param message exception string.
	 */
	public CCSpecValidationException(String message) {
		super(message);
	}

	/**
	 * @param cause exception cause.
	 */
	public CCSpecValidationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message exception string.
	 * @param cause exception cause.
	 */
	public CCSpecValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
