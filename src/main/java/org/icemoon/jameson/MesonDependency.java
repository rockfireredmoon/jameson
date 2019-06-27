package org.icemoon.jameson;

public class MesonDependency {

	private String name;
	private boolean required;
	private boolean conditional;
	private boolean has_fallback;

	public String name() {
		return name;
	}

	public MesonDependency name(String name) {
		this.name = name;
		return this;
	}

	public boolean required() {
		return required;
	}

	public MesonDependency required(boolean required) {
		this.required = required;
		return this;
	}

	public boolean conditional() {
		return conditional;
	}

	public MesonDependency conditional(boolean conditional) {
		this.conditional = conditional;
		return this;
	}

	public boolean hasFallback() {
		return has_fallback;
	}

	public MesonDependency hasFallback(boolean has_fallback) {
		this.has_fallback = has_fallback;
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
		MesonDependency other = (MesonDependency) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MesonDependency [name=" + name + ", required=" + required + ", conditional=" + conditional
				+ ", has_fallback=" + has_fallback + "]";
	}

}
