package org.icemoon.jameson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class MesonProject implements MesonPart {

	public enum State {
		INVALID, BARE, UNINITIALIZED, INITIALIZED
	}

	public enum ConfigurationLayout {
		SINGLE, MULTI
	}

	private File directory;
	private String configuration = "default";
	private ConfigurationLayout configurationLayout = ConfigurationLayout.SINGLE;
	private File buildDirectory = new File("builddir");
	private Meson tool;
	private MesonProjectOptions options;
	private MesonProjectInfo info;
	private List<MesonTarget> targets = new ArrayList<>();
	private long targetsLastModified = -1;
	private List<MesonDependency> dependencies = new ArrayList<>();
	private long dependenciesLastModified = -1;

	/**
	 * Construct a new Meson project rooted at the provided path.
	 * 
	 * @param directory
	 */
	public MesonProject(File directory) {
		this(directory, new Meson());
	}

	/**
	 * Construct a new Meson project rooted at the provided path.
	 * 
	 * @param directory directory where project is rooted
	 * @param tool      the meson tool to execute
	 */
	public MesonProject(File directory, Meson tool) {
		assert directory != null;
		assert tool != null;

		this.directory = directory;
		this.tool = tool;
	}

	public String configuration() {
		return configuration;
	}

	public MesonProject configuration(String configuration) {
		assert configuration != null && configuration.length() > 0;
		this.configuration = configuration;
		return this;
	}

	public ConfigurationLayout configurationLayout() {
		return configurationLayout;
	}

	public MesonProject configurationLayout(ConfigurationLayout configurationLayout) {
		assert configurationLayout != null;
		this.configurationLayout = configurationLayout;
		return this;
	}

	public Meson tool() {
		return tool;
	}

	public MesonProject tool(Meson tool) {
		assert tool != null;
		this.tool = tool;
		return this;
	}

	public File directory() {
		return directory;
	}

	public MesonProject directory(File directory) {
		assert directory != null;
		this.directory = directory;
		return this;
	}

	public File buildDirectory() {
		return buildDirectory;
	}

	public MesonProject buildDirectory(File buildDirectory) {
		assert buildDirectory != null;
		this.buildDirectory = buildDirectory;
		return this;
	}

	public MesonProjectInfo info() throws IOException, MesonException {
		State state = getState();
		if (state != State.INITIALIZED && state != State.UNINITIALIZED)
			throw new IllegalStateException(String.format("This project is not in the %s or %s states.",
					State.INITIALIZED, State.UNINITIALIZED));

		if (info == null)
			info = new MesonProjectInfo(this);
		return info;

	}

	public List<MesonTarget> targets() throws IOException {

		State state = getState();
		if (state != State.INITIALIZED && state != State.UNINITIALIZED)
			throw new IllegalStateException(String.format("This project is not in the %s or %s states.",
					State.INITIALIZED, State.UNINITIALIZED));

		File targetsFile = new File(new File(resolvedBuildDirectory(), "meson-info"), "intro-targets.json");
		long lastMod = targetsFile.exists() ? targetsFile.lastModified() : -2;
		if (targetsLastModified == -1 || targetsLastModified != lastMod) {
			targets.clear();
			targetsLastModified = lastMod;
			if (lastMod != -2) {
				GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.registerTypeAdapter(MesonTarget.Type.class, new TargetTypeDeserializer());
				Gson gson = gsonBuilder.create();
				try (FileReader r = new FileReader(targetsFile)) {
					List<MesonTarget> opts = Arrays.asList(gson.fromJson(r, MesonTarget[].class));
					for (MesonTarget opt : opts) {
						targets.add(opt);
					}
				}
			}
		}
		return targets;
	}

	public List<MesonDependency> dependencies() throws IOException {

		State state = getState();
		if (state != State.INITIALIZED && state != State.UNINITIALIZED)
			throw new IllegalStateException(String.format("This project is not in the %s or %s states.",
					State.INITIALIZED, State.UNINITIALIZED));

		File dependenciesFile = new File(new File(resolvedBuildDirectory(), "meson-info"), "intro-dependencies.json");
		long lastMod = dependenciesFile.exists() ? dependenciesFile.lastModified() : -2;
		if (dependenciesLastModified == -1 || dependenciesLastModified != lastMod) {
			dependencies.clear();
			dependenciesLastModified = lastMod;
			if (lastMod != -2) {
				GsonBuilder gsonBuilder = new GsonBuilder();
				Gson gson = gsonBuilder.create();
				try (FileReader r = new FileReader(dependenciesFile)) {
					List<MesonDependency> opts = Arrays.asList(gson.fromJson(r, MesonDependency[].class));
					for (MesonDependency opt : opts) {
						dependencies.add(opt);
					}
				}
			}
		}
		return dependencies;
	}

	public MesonOptions options() throws IOException, MesonException {
		State state = getState();
		if (state != State.INITIALIZED && state != State.UNINITIALIZED)
			throw new IllegalStateException(String.format("This project is not in the %s or %s states.",
					State.INITIALIZED, State.UNINITIALIZED));

		if (options == null)
			options = new MesonProjectOptions(this);
		return options;

	}

	public MesonProject initialize(MesonProgress progress) throws IOException, MesonException {
		if (getState() != State.UNINITIALIZED)
			throw new IllegalStateException(String.format("This project is not in the %s state.", State.UNINITIALIZED));

		tool.start(directory, progress, resolvedBuildDirectory().getAbsolutePath());
		return this;
	}

	public void build(MesonProgress progress) throws IOException, MesonException {
		if (getState() != State.INITIALIZED)
			throw new IllegalStateException(String.format("This project is not in the %s state.", State.INITIALIZED));

		tool.backend(options().getAsString("backend")).build(this, progress);
	}

	public void clean(MesonProgress progress) throws IOException, MesonException {
		if (getState() != State.INITIALIZED)
			throw new IllegalStateException(String.format("This project is not in the %s state.", State.INITIALIZED));

		tool.backend(options().getAsString("backend")).clean(this, progress);
	}

	public State getState() {
		File path = new File(directory, "meson.build");
		if (path.exists()) {
			if (resolvedBuildDirectory().exists())
				return State.INITIALIZED;
			else
				return State.UNINITIALIZED;
		} else if (directory.exists())
			return State.BARE;
		else
			return State.INVALID;
	}

	public File resolvedBuildDirectory() {
		if (configurationLayout == ConfigurationLayout.SINGLE)
			return buildDirectory.isAbsolute() ? buildDirectory : new File(directory, buildDirectory.getPath());
		else
			return new File(
					buildDirectory.isAbsolute() ? buildDirectory : new File(directory, buildDirectory.getPath()),
					configuration);
	}

	@Override
	public MesonProject project() {
		return this;
	}

	static class TargetTypeDeserializer implements JsonDeserializer<MesonTarget.Type> {

		@Override
		public MesonTarget.Type deserialize(JsonElement val, Type type, JsonDeserializationContext ctx)
				throws JsonParseException {
			try {
				return MesonTarget.Type.valueOf(val.getAsString().toUpperCase().replace(' ', '_'));
			} catch (Exception e) {
				return MesonTarget.Type.CUSTOM;
			}
		}
	}
}
