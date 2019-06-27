# jameson

Simple library for interacting with the Meson build system from Java. Intended for use as a basis for IDE, build and CI system integration.a

## Installation

Available in Maven Central.

### Maven

```xml
	<dependency>
		<groupId>io.github.rockfireredmoon</groupId>
		<artifactId>jameson</artifactId>
		<version>0.0.1</version>
	</dependency>
```

## Usage

You'll need a project with a meson.build at the root in the usual way. Then construct a `MesonProject` pointing to this directory

```java
	MesonProject project = new MesonProject(new File("/path/to/project"));
```

### Initializing The Project

For most operations, you will need create the Meson build directory from the information supplied in the meson.build file(s). If not already initialized, you can do so with the following.

```java
	project.initialize(new ConsoleMesonProgress());
```

You can pass in your own implementation of a `MesonProgress`, wiring it to your UI toolkit of choice for example.

### Building The Project

Once initialized, one thing you can do with the project is build it.

```java
	project.build(new ConsoleMesonProgress());
```

### Cleaning The Project

To clean up the project (remove binaries, generated files etc).

```java
	project.clean(new ConsoleMesonProgress());
```

### Querying The Project

You can query various aspects of the project.

#### Project Information

```java
	MesonProjectInformation info = project.info();
	/* Display the main project version, other attribute can be queries such as name, description and more */
	System.out.println("Version: " + info.version());
	for(MesonSubproject sub : info) {
		/* Display the subproject name, other attributes can be queried such as decription, version and more */
		System.out.println("Subproject: " + sub.name());
	}
```

#### Targets

You can get a list of all the generated Makefile targets.

```java
	for(MesonTarget target : project.targets()) {
		/* Display the name, other attributes can be queried such as sources, type, id and more */
		System.out.println("Target: " + target.name());
	} 
```

### Project Options

You can get and set project options.

```java
	MesonOptions opts = project.options();
	System.out.println("Old build type: " + opts.getAsString("buildtype"));
	opts.put("buildtype", "release");
```

You can do the same thing on subprojects.

```java
	info.get("subproject1").options().put("my_custom_option", "val1");