package org.icemoon.jameson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MesonProjectInfo extends LinkedHashSet<MesonSubproject> {

	private static final long serialVersionUID = 1L;
	private MesonProject project;
	private long lastModified;
	private Map<String, MesonSubproject> info = new HashMap<>();
	private String version;
	private String description;
	private File subprojectsDirectory;

	protected MesonProjectInfo(MesonProject project) {
		this.project = project;
		subprojectsDirectory = new File(project.directory(), "subprojects");
	}

	public File subprojectsDirectory() {
		return subprojectsDirectory;
	}

	public String version() {
		checkReload();
		return version;
	}

	public String description() {
		checkReload();
		return description;
	}

	public MesonSubproject getOption(String name) {
		checkReload();
		return info.get(name);
	}

	@Override
	public int size() {
		checkReload();
		return super.size();
	}

	@Override
	public Spliterator<MesonSubproject> spliterator() {
		checkReload();
		return super.spliterator();
	}

	@Override
	public Iterator<MesonSubproject> iterator() {
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
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
	public boolean addAll(Collection<? extends MesonSubproject> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(MesonSubproject option) {
		throw new UnsupportedOperationException();
	}

	public boolean containsName(String name) {
		checkReload();
		return info.containsKey(name);
	}

	public MesonSubproject get(String name) {
		checkReload();
		MesonSubproject opt = info.get(name);
		if (opt == null)
			throw new IllegalArgumentException(String.format("No subproject named %s.", name));
		return opt;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	protected void checkReload() {
		File buildOptionsFile = new File(new File(project.resolvedBuildDirectory(), "meson-info"),
				"intro-projectinfo.json");
		if (lastModified == -1 || lastModified != buildOptionsFile.lastModified()) {
			lastModified = buildOptionsFile.lastModified();
			super.clear();
			info.clear();
			try {
				Gson gson = new Gson();
				Type type = new TypeToken<Map<String, Object>>() {
				}.getType();
				try (Reader reader = new FileReader(buildOptionsFile)) {
					Map<String, Object> json = gson.fromJson(reader, type);
					version = (String) json.get("version");
					description = (String) json.get("descriptive_name");
					subprojectsDirectory = new File(project.directory(), (String) json.get("subproject_dir"));
					for (Map<String, String> map : (List<Map<String, String>>) json.get("subprojects")) {
						String subname = (String) map.get("name");
						MesonSubproject subproject = new MesonSubproject(project,
								new File(subprojectsDirectory, subname)).name(subname)
										.version((String) map.get("version"))
										.description((String) map.get("descriptive_name"));
						super.add(subproject);
						info.put(subproject.name(), subproject);
					}

				}
			} catch (IOException ioe) {
				throw new IllegalStateException(
						String.format("Could not read options JSON file to discover options.", buildOptionsFile));
			}
		}
	}

}
