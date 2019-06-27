package org.icemoon.jameson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;

import org.icemoon.jameson.MesonOption.OptionType;
import org.icemoon.jameson.MesonProject.State;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public abstract class MesonOptions extends LinkedHashSet<MesonOption> {

	private static final long serialVersionUID = 1L;
	private MesonPart part;
	private long infoLastModified = -1;
	private long lastModified = -1;
	private Map<String, MesonOption> info = new HashMap<>();

	public MesonOptions(MesonPart part) {
		this.part = part;
	}

	public MesonOption getOption(String key) {
		return info.get(key);
	}

	@Override
	public int size() {
		checkReload();
		return super.size();
	}

	@Override
	public Spliterator<MesonOption> spliterator() {
		checkReload();
		return super.spliterator();
	}

	@Override
	public Iterator<MesonOption> iterator() {
		checkReload();
		return super.iterator();
	}

	@Override
	public boolean isEmpty() {
		checkReload();
		return super.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		checkReload();
		return super.contains(o);
	}

	@Override
	public boolean remove(Object o) {
		checkReload();
		return super.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		checkReload();
		return super.removeAll(c);
	}

	@Override
	public Object[] toArray() {
		checkReload();
		return super.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		checkReload();
		return super.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		checkReload();
		return super.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends MesonOption> c) {
		checkReload();
		return super.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		checkReload();
		return super.retainAll(c);
	}

	@Override
	public boolean add(MesonOption option) {
		checkReload();
		MesonOption existing = info.get(option.name());
		if (existing != null)
			throw new IllegalStateException(String.format("Option %s already exists.", option.name()));
		super.add(option);
		try {
			try (PrintWriter pw = new PrintWriter(
					new FileWriter(new File(part.directory(), "meson-options.txt"), true))) {
				pw.println(serializedString(option));
			}
		} catch (IOException ioe) {
			throw new IllegalStateException(String.format("Failed to save meson configuration.", ioe));
		}
		info.put(option.name(), option);
		return true;
	}

	private String escape(String str) {
		return str.replace("'", "\\'");
	}

	private List<String> splitParse(String line) {
		boolean inQuote = false;
		boolean escape = false;
		List<String> args = new ArrayList<>();
		StringBuilder word = new StringBuilder();
		for (char c : line.toCharArray()) {
			if (c == '\\' & !escape) {
				escape = true;
				word.append(c);
			} else if (c == '\'' && !escape) {
				inQuote = !inQuote;
				word.append(c);
			} else if (c == ',' && !escape && !inQuote) {
				args.add(word.toString());
				word.setLength(0);
				;
			} else {
				word.append(c);
				escape = false;
			}
		}
		args.add(word.toString());
		word.setLength(0);
		return args;
	}

	private String parseString(String str) {
		if (!str.startsWith("'") || !str.endsWith("'")) {
			throw new IllegalArgumentException(String.format("%s. Not a valid string"));
		}
		StringBuilder word = new StringBuilder();
		char[] arr = str.toCharArray();
		boolean escape = false;
		for (int i = 1; i < arr.length - 1; i++) {
			char c = arr[i];
			if (c == '\\' & !escape) {
				escape = true;
				word.append(c);
			} else {
				escape = false;
				word.append(c);
			}
		}
		return word.toString();
	}

	private List<MesonOption> parseOptionsFile(File file) throws IOException {
		List<MesonOption> opts = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("option('")) {
					line = line.substring(7, line.length() - 1);
					MesonOption opt = new MesonOption();
					opt.userDefined(true);
					opts.add(opt);
					for (String arg : splitParse(line)) {
						arg = arg.trim();
						int idx = arg.indexOf(':');
						if (idx == -1) {
							opt.name(parseString(arg));
						} else {
							String name = arg.substring(0, idx).trim();
							String val = arg.substring(idx + 1).trim();
							if (name.equals("type")) {
								opt.type(OptionType.valueOf(parseString(val).toUpperCase()));
							} else if (name.equals("description")) {
								opt.description(parseString(val));
							} else if (name.equals("min")) {
								opt.min(Integer.parseInt(val));
							} else if (name.equals("max")) {
								opt.min(Integer.parseInt(val));
							} else if (name.equals("value")) {
								Object o = null;
								if (val.startsWith("'")) {
									o = parseString(val);
								} else if (val.startsWith("[")) {
									// TODO arr
								} else if (val.equals("true") || val.equals("false")) {
									o = Boolean.valueOf(val);
								} else {
									try {
										o = Long.parseLong(val);
									} catch (NumberFormatException nfe) {
										throw new IllegalArgumentException(String.format("Unsupported value %s.", val));
									}
								}
								opt.value(o);
							}
						}
					}

				}
			}
		}
		return opts;
	}

	@SuppressWarnings("unchecked")
	private String serializedString(MesonOption option) {
		// TODO escaping
		StringBuilder b = new StringBuilder();
		b.append("option('");
		b.append(option.name());
		b.append("', type: '");
		b.append(option.type().name().toLowerCase());
		b.append("', value: ");
		if (option.type() == OptionType.ARRAY) {
			serializeStringList(b, (List<String>) option.value());
		} else if (option.type() == OptionType.BOOLEAN || option.type() == OptionType.INTEGER) {
			b.append(option.value());
		} else if (option.type() == OptionType.FEATURE) {
			b.append("'");
			b.append(option.booleanValue() ? "enabled" : "disabled");
			b.append("'");
		} else {
			b.append("'");
			b.append(escape((String) option.value()));
			b.append("'");
		}
		if (option.description() != null && option.description().length() > 0) {
			b.append(", description: '");
			b.append(escape(option.description()));
			b.append("'");
		}
		if (option.type() == OptionType.COMBO) {
			b.append(", choices: ");
			serializeStringList(b, option.choices() == null ? null : Arrays.asList(option.choices()));
		}
		if (option.type() == OptionType.INTEGER) {
			b.append(", min: ");
			b.append(option.min());
			b.append(", max: ");
			b.append(option.max());
		}

		b.append(")");
		return b.toString();
	}

	private void serializeStringList(StringBuilder b, List<String> lst) {
		b.append("[");
		if (lst != null) {
			for (int i = 0; i < lst.size(); i++) {
				if (i > 0)
					b.append(",");
				b.append("'");
				b.append(escape(lst.get(i)));
				b.append("'");
			}
		}
		b.append("]");
	}

	public boolean containsName(String name) {
		checkReload();
		return info.containsKey(name);
	}

	public MesonOption get(String name) {
		checkReload();
		MesonOption opt = info.get(name);
		if (opt == null)
			throw new IllegalArgumentException(String.format("No option named %s.", name));
		return opt;
	}

	public String getAsString(String name) {
		checkReload();
		MesonOption opt = info.get(name);
		if (opt == null)
			throw new IllegalArgumentException(String.format("No option named %s.", name));
		return opt.stringValue();
	}

	public String put(String name, Object value) {
		checkReload();
		if (!containsName(name))
			throw new IllegalArgumentException(String.format("No option named %s.", name));
		MesonOption opt = info.get(name);
		Object pval = opt.value();
		String psval = opt.stringValue();
		if (!Objects.equals(pval, value)) {
			opt.value(value);
			try {
				part.project().tool().start(part.project().directory(), null, "configure",
						part.project().resolvedBuildDirectory().getAbsolutePath(), "-D" + name + "=" + value);
			} catch (MesonException | IOException ioe) {
				throw new IllegalStateException(String.format("Failed to save meson configuration.", ioe));
			}
		}
		return psval;
	}

	@Override
	public void clear() {
		if (part.project().getState() == State.INITIALIZED)
			throw new IllegalStateException("Cannot clear build options once build directory is initialized.");
		checkReload();
		super.clear();
	}

	protected void checkReload() {
		File mesonOptionsFile = new File(part.directory(), "meson-options.txt");
		long mesonOptionsLastMod = mesonOptionsFile.exists() ? mesonOptionsFile.lastModified() : -2;

		File buildOptionsFile = new File(new File(part.project().resolvedBuildDirectory(), "meson-info"),
				"intro-buildoptions.json");
		long buildOptionsLastMod = buildOptionsFile.exists() ? buildOptionsFile.lastModified() : -2;

		boolean load = false;
		if (infoLastModified == -1 || infoLastModified != buildOptionsLastMod || buildOptionsLastMod == -1
				|| lastModified != buildOptionsLastMod) {
			infoLastModified = buildOptionsLastMod;
			lastModified = mesonOptionsLastMod;
			super.clear();
			info.clear();
			load = true;
		}

		if (load) {
			try {
				if (infoLastModified != -2) {
					GsonBuilder gsonBuilder = new GsonBuilder();
					gsonBuilder.registerTypeAdapter(OptionType.class, new MesonOptions.OptionTypeDeserializer());
					Gson gson = gsonBuilder.create();
					try (FileReader r = new FileReader(buildOptionsFile)) {
						List<MesonOption> opts = Arrays.asList(gson.fromJson(r, MesonOption[].class));

						for (MesonOption opt : opts) {
							super.add(opt);
							info.put(opt.name(), opt);
						}
					}
				}

				if (mesonOptionsLastMod != -2) {
					for (MesonOption o : parseOptionsFile(mesonOptionsFile)) {
						if (!super.contains(o)) {
							super.add(o);
							info.put(o.name(), o);
						}
					}
				}
			} catch (IOException ioe) {
				throw new IllegalStateException(
						String.format("Could not read options JSON file to discover options.", buildOptionsFile));
			}
		}
	}

	static class OptionTypeDeserializer implements JsonDeserializer<OptionType> {

		@Override
		public OptionType deserialize(JsonElement val, Type type, JsonDeserializationContext ctx)
				throws JsonParseException {
			return OptionType.valueOf(val.getAsString().toUpperCase());
		}
	}
}
