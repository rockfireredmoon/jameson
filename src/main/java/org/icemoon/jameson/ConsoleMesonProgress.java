package org.icemoon.jameson;

import java.util.Stack;

public class ConsoleMesonProgress implements MesonProgress {
	
	private Stack<Integer> tasks = new Stack<>();
	
	private String spacing() {
		String s = "";
		for(int i = 0 ; i < tasks.size() - 1; i++)
			s += "  ";
		return s;
	}
	

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void start(int totalTasks) {
		System.out.println("Starting " + totalTasks + " tasks");
		tasks.add(totalTasks);
	}

	@Override
	public void end() {
		tasks.pop();
		System.out.println(spacing() + "Ended");
	}

	@Override
	public void progress(int tasks, String message) {
		System.out.println(spacing() + "Task " + tasks + "/" + this.tasks.peek() + " completed. " + message);
	}

	@Override
	public void message(MessageType type, String message) {
		if (type == MessageType.ERROR)
			System.err.println(spacing() + type.name() + ": " + message);
		else
			System.out.println(spacing() + type.name() + ": " + message);

	}

}
