package org.icemoon.jameson;

import java.io.File;

public class MesonSubproject implements MesonPart {

	private String name;
	private String version;
	private String description;
	private MesonProject project;
	private File directory;
	private MesonSubprojectOptions options;

	protected MesonSubproject(MesonProject project, File directory) {
		this.project = project;
		this.directory = directory;
	}

	public File directory() {
		return directory;
	}

	public MesonOptions options() {
		if (options == null) {
			options = new MesonSubprojectOptions(this);
		}
		return options;
	}

	public MesonProject project() {
		return project;
	}

	public String name() {
		return name;
	}

	protected MesonSubproject name(String name) {
		this.name = name;
		return this;
	}

	public String version() {
		return version;
	}

	protected MesonSubproject version(String version) {
		this.version = version;
		return this;
	}

	public String description() {
		return description;
	}

	protected MesonSubproject description(String description) {
		this.description = description;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MesonSubproject other = (MesonSubproject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MesonSubproject [name=" + name + ", version=" + version + ", description=" + description + ", project="
				+ project + "]";
	}

	@Override
	public File resolvedBuildDirectory() {
		return new File(new File(project().resolvedBuildDirectory(), "subprojects"), name());
	}

}
