package org.icemoon.jameson;

import java.util.ArrayList;
import java.util.List;

public class MesonTarget {
	
	public enum Type {
		EXECUTABLE, STATIC_LIBRARY, SHARED_LIBRARY, CUSTOM, RUN, JAR
	}

	private String name;
	private String id;
	private Type type;
	private String defined_in;
	private boolean build_by_default;
	private List<MesonTargetSource> target_sources = new ArrayList<>();
	private String subproject;
	private boolean installed;
	private List<String> install_filename = new ArrayList<>();

	public String name() {
		return name;
	}

	public MesonTarget name(String name) {
		this.name = name;
		return this;
	}

	public String id() {
		return id;
	}

	public MesonTarget id(String id) {
		this.id = id;
		return this;
	}

	public Type type() {
		return type;
	}

	public MesonTarget type(Type type) {
		this.type = type;
		return this;
	}

	public String definedIn() {
		return defined_in;
	}

	public MesonTarget definedIn(String definedIn) {
		this.defined_in = definedIn;
		return this;
	}

	public boolean buildByDefault() {
		return build_by_default;
	}

	public MesonTarget buildByDefault(boolean buildByDefault) {
		this.build_by_default = buildByDefault;
		return this;
	}

	public List<MesonTargetSource> targetSources() {
		return target_sources;
	}

	public MesonTarget targetSources(List<MesonTargetSource> targetSources) {
		this.target_sources = targetSources;
		return this;
	}

	public String subproject() {
		return subproject;
	}

	public MesonTarget subproject(String subprojects) {
		this.subproject = subprojects;
		return this;
	}

	public boolean installed() {
		return installed;
	}

	public MesonTarget installed(boolean installed) {
		this.installed = installed;
		return this;
	}

	public List<String> installFilenames() {
		return install_filename;
	}

	public MesonTarget installFilename(List<String> installFilename) {
		this.install_filename = installFilename;
		return this;
	}

	@Override
	public String toString() {
		return "MesonTarget [name=" + name + ", id=" + id + ", type=" + type + ", definedIn=" + defined_in
				+ ", buildByDefault=" + build_by_default + ", targetSources=" + target_sources + ", subproject="
				+ subproject + ", installed=" + installed + ", installFilename=" + install_filename + "]";
	}

}
