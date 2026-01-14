package com.wildermods.wilderforge.launch.exception;

/**
 * Thrown when a hard requirement for a mixin cannot be satisfied, 
 * such as a required mod being absent or having an incompatible version.
 * <p>
 * This indicates that the mixin cannot be applied and the game should not continue.
 * </p>
 *
 * @see java.lang.LinkageError
 */
@SuppressWarnings("serial")
public class UnsatisifiedRequirementError extends LinkageError {

	/**
	 * Constructs a new UnsatisifiedRequirementError with the specified detail message.
	 *
	 * @param message the detail message describing the unsatisfied requirement
	 */
	public UnsatisifiedRequirementError(String message) {
		super(message);
	}

	/**
	 * Constructs a new UnsatisifiedRequirementError with the specified cause.
	 *
	 * @param cause the underlying cause of this error
	 */
	public UnsatisifiedRequirementError(Throwable cause) {
		this("", cause);
	}

	/**
	 * Constructs a new UnsatisifiedRequirementError with the specified detail message and cause.
	 *
	 * @param message the detail message describing the unsatisfied requirement
	 * @param cause the underlying cause of this error
	 */
	public UnsatisifiedRequirementError(String message, Throwable cause) {
		super(message, cause);
	}

}
