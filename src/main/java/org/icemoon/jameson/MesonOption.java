package org.icemoon.jameson;

import java.util.Arrays;
import java.util.List;

public class MesonOption {

	public enum OptionType {
		COMBO, BOOLEAN, ARRAY, INTEGER, STRING, FEATURE
	}

	private String name;
	private Object value;
	private OptionType type;
	private String[] choices;
	private String section;
	private String description;
	private int min;
	private int max;
	private boolean userDefined;

	public MesonOption() {
	}

	public MesonOption(String name, String value) {
		this(name, value, OptionType.STRING);
	}

	public MesonOption(String name, Object value, OptionType type, String... choices) {
		this.name = name;
		this.type = type;
		value(value);
		this.choices = choices;
	}

	public boolean userDefined() {
		return userDefined;
	}

	protected MesonOption userDefined(boolean userDefined) {
		this.userDefined = userDefined;
		return this;
	}

	public String name() {
		return name;
	}

	public MesonOption name(String name) {
		this.name = name;
		return this;
	}

	public String section() {
		return section;
	}

	public MesonOption section(String section) {
		this.section = section;
		return this;
	}

	public int min() {
		return min;
	}

	public MesonOption min(int min) {
		this.min = min;
		return this;
	}

	public int max() {
		return max;
	}

	public MesonOption max(int max) {
		this.max = max;
		return this;
	}

	public String description() {
		return description;
	}

	public MesonOption description(String description) {
		this.description = description;
		return this;
	}

	public boolean featureValue() {
		if (type != OptionType.FEATURE)
			throw new UnsupportedOperationException("This is not a feature value.");
		return Boolean.TRUE.equals(value);
	}

	public boolean booleanValue() {
		if (type != OptionType.BOOLEAN)
			throw new UnsupportedOperationException("This is not a boolean value.");
		return Boolean.TRUE.equals(value);
	}

	public int integerValue() {
		if (type != OptionType.INTEGER)
			throw new UnsupportedOperationException("This is not an integer value.");
		return ((Number) value).intValue();
	}

	public long longValue() {
		if (type != OptionType.INTEGER)
			throw new UnsupportedOperationException("This is not an integer value.");
		return ((Number) value).longValue();
	}

	@SuppressWarnings("unchecked")
	public List<String> arrayValue() {
		if (type != OptionType.ARRAY)
			throw new UnsupportedOperationException("This is not a array value.");
		return (List<String>) value;
	}

	public String stringValue() {
		return value.toString();
	}

	public Object value() {
		return value;
	}

	protected MesonOption value(Object value) {
		if (type == null)
			throw new IllegalStateException("Option has no type.");
		switch (type) {
		case ARRAY:
			if (!(value instanceof List))
				throw new IllegalArgumentException(String.format("Argument must be a %s", List.class));
			break;
		case FEATURE:
		case BOOLEAN:
			if (!(value instanceof Boolean))
				throw new IllegalArgumentException(String.format("Argument must be a %s", Boolean.class));
			break;
		case INTEGER:
			if (!(value instanceof Integer) && !(value instanceof Long) && !(value instanceof Short))
				throw new IllegalArgumentException(
						String.format("Argument must be a %s, %s or %s", Short.class, Integer.class, Long.class));
			break;
		case STRING:
		case COMBO:
			if (!(value instanceof String))
				throw new IllegalArgumentException(String.format("Argument must be a %s", String.class));
			break;
		}
		this.value = value;
		return this;
	}

	public OptionType type() {
		return type;
	}

	public MesonOption type(OptionType type) {
		this.type = type;
		return this;
	}

	public String[] choices() {
		return choices;
	}

	public MesonOption choices(String[] choices) {
		this.choices = choices;
		return this;
	}

	@Override
	public String toString() {
		return "MesonOption [name=" + name + ", value=" + value + ", type=" + type + ", choices="
				+ Arrays.toString(choices) + ", section=" + section + ", description=" + description + ", min=" + min
				+ ", max=" + max + ", userDefined=" + userDefined + "]";
	}


}
