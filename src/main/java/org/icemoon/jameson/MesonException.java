package org.icemoon.jameson;

public class MesonException extends Exception {

	private static final long serialVersionUID = 1L;

	public MesonException() {
		super();
	}

	public MesonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MesonException(String message, Throwable cause) {
		super(message, cause);
	}

	public MesonException(String message) {
		super(message);
	}

	public MesonException(Throwable cause) {
		super(cause);
	}

}
