package org.icemoon.jameson;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.icemoon.jameson.MesonProgress.MessageType;

public class Meson {

	private final static String DEFAULT_COMMAND = "meson";
	private String command;
	private ToolLocator toolLocator;

	public Meson() {
		this(DEFAULT_COMMAND);
		toolLocator = new DefaultToolLocator();
	}

	public Meson(String command) {
		this.command = command;
	}

	public MesonBackend backend(String backend) {
		if (backend.equals("ninja")) {
			return new NinjaBackend(this);
		}
		throw new UnsupportedOperationException(String.format(
				"Unknown backend %s. To add support, subclass %s and override the backend() method to create a custom instance of a %s.",
				backend, Meson.class, MesonBackend.class));
	}

	public ToolLocator toolLocator() {
		return toolLocator;
	}

	public Meson toolLocator(ToolLocator toolLocator) {
		this.toolLocator = toolLocator;
		return this;
	}

	public String command() {
		return command;
	}

	public Meson command(String command) {
		return this;
	}

	public void start(File cwd, MesonProgress progress, String... args) throws IOException, MesonException {
		List<String> cmdline = new ArrayList<>();
		cmdline.add(toolLocator.findTool(command));
		cmdline.addAll(Arrays.asList(args));
		ProcessBuilder builder = new ProcessBuilder(cmdline);
		builder.directory(cwd);
		Process process = builder.start();

		if (progress != null) {
			Thread errThread = new Thread() {
				public void run() {
					try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
						String line;
						while ((line = r.readLine()) != null) {
							progress.message(MessageType.ERROR, line);
						}
					} catch (IOException ioe) {
					}
				}
			};
			errThread.start();
			errThread.interrupt();
		}
		try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = r.readLine()) != null) {
				if (progress != null) {
					progress.message(MessageType.INFO, line);
				}
			}
		}
		try {
			int exit = process.waitFor();
			if (exit != 0)
				throw new MesonException(String.format("Meson exited with code %d", exit));
		} catch (InterruptedException e) {
		}
	}
}
