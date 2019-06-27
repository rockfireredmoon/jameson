package org.icemoon.jameson;

public interface MesonProgress {
	
	public enum MessageType {
		INFO, WARNING, ERROR
	}
	
	default boolean isCancelled() {
		return false;
	}

	default void start(int totalTasks) {
		
	}

	default void end() {
	}

	default void progress(int tasks, String message) {
		
	}
	
	void message(MessageType type, String message);
}
