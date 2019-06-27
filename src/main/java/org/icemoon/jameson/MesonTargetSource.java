package org.icemoon.jameson;

import java.util.ArrayList;
import java.util.List;

public class MesonTargetSource {

	private String language;
	private List<String> compiler = new ArrayList<>();
	private List<String> parameters = new ArrayList<>();
	private List<String> sources = new ArrayList<>();
	private List<String> generated_sources = new ArrayList<>();

	public String language() {
		return language;
	}

	public MesonTargetSource lsetLanguage(String language) {
		this.language = language;
		return this;
	}

	public List<String> compiler() {
		return compiler;
	}

	public MesonTargetSource compiler(List<String> compiler) {
		this.compiler = compiler;
		return this;
	}

	public List<String> parameters() {
		return parameters;
	}

	public MesonTargetSource parameters(List<String> parameters) {
		this.parameters = parameters;
		return this;
	}

	public List<String> sources() {
		return sources;
	}

	public MesonTargetSource sources(List<String> sources) {
		this.sources = sources;
		return this;
	}

	public List<String> generatedSources() {
		return generated_sources;
	}

	public MesonTargetSource generatedSources(List<String> generatedSources) {
		this.generated_sources = generatedSources;
		return this;
	}

	@Override
	public String toString() {
		return "MesonTargetSource [language=" + language + ", compiler=" + compiler + ", parameters=" + parameters
				+ ", sources=" + sources + ", generatedSources=" + generated_sources + "]";
	}

}
