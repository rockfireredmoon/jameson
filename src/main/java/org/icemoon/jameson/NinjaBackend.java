package org.icemoon.jameson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.icemoon.jameson.MesonProgress.MessageType;

public class NinjaBackend implements MesonBackend {

	private Meson meson;

	public NinjaBackend(Meson meson) {
		this.meson = meson;
	}

	@Override
	public void build(MesonProject project, MesonProgress progress) throws IOException, MesonException {
		runCommand(project, progress);
	}

	private void runCommand(MesonProject project, MesonProgress progress, String... args)
			throws FileNotFoundException, IOException, MesonException {
		List<String> cmdline = new ArrayList<>();
		cmdline.add(meson.toolLocator().findTool("ninja"));
		cmdline.addAll(Arrays.asList(args));
		ProcessBuilder builder = new ProcessBuilder(cmdline);
		builder.directory(project.resolvedBuildDirectory());
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
		int totalTasks = -1;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = r.readLine()) != null) {
				if (progress != null) {
					if (line.matches("\\[\\d+/\\d+\\]")) {
						int eidx = line.indexOf(']');
						String[] els = line.substring(1, eidx).split("/");
						int task = Integer.parseInt(els[0]);
						int total = Integer.parseInt(els[0]);
						if (totalTasks != total) {
							progress.start(total);
						}
						progress.progress(task, line.substring(eidx + 1).trim());
					} else
						progress.message(MessageType.INFO, line);
				}
			}
		}
		try {
			int exit = process.waitFor();
			if (exit != 0)
				throw new MesonException(String.format("Ninja exited with code %d", exit));
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void clean(MesonProject project, MesonProgress progress) throws IOException, MesonException {
		runCommand(project, progress, "clean");
	}

}
