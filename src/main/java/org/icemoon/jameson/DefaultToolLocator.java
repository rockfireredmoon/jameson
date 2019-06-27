package org.icemoon.jameson;

import java.io.File;
import java.io.FileNotFoundException;

public class DefaultToolLocator implements ToolLocator {

	private String[] path;
	
	public DefaultToolLocator() {
		String pathspec = System.getenv(SystemUtils.IS_OS_WINDOWS ? "Path" : "PATH");
		if (pathspec != null && pathspec.length() > 0)
			path = pathspec.split(File.pathSeparator);
		else
			path = new String[] { "." };
	}

	public String[] path() {
		return path;
	}

	public DefaultToolLocator path(String[] path) {
		this.path = path;
		return this;
	}

	public String findTool(String command) throws FileNotFoundException {
		File cmd = new File(command);
		if (cmd.isAbsolute())
			return cmd.getAbsolutePath();
		else {
			for (String dir : path) {
				cmd = new File(dir, command);
				if (cmd.exists()) {
					return cmd.getAbsolutePath();
				} else {
					if (SystemUtils.IS_OS_WINDOWS
							&& !(command.toLowerCase().endsWith(".exe") || command.toLowerCase().endsWith(".bat"))) {
						cmd = new File(dir, command + ".exe");
						if (cmd.exists()) {
							return cmd.getAbsolutePath();
						}
						cmd = new File(dir, command + ".bat");
						if (cmd.exists()) {
							return cmd.getAbsolutePath();
						}
					}
				}
			}
		}
		throw new FileNotFoundException(String.format("Could not find meson command '%s' in the path %s", command,
				String.join(File.pathSeparator, path)));
	}
}
