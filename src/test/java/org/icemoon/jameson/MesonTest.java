package org.icemoon.jameson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.icemoon.jameson.MesonOption.OptionType;
import org.junit.Test;

public class MesonTest {

	static List<File> tempDirs = new LinkedList<File>();
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				for (File d : tempDirs) {
					try {
						Files.walkFileTree(d.toPath(), new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
								Files.delete(file);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
								// theoretically possible
								Files.delete(file);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
								if (exc == null) {
									Files.delete(dir);
									return FileVisitResult.CONTINUE;
								} else {
									throw exc;
								}
							}
						});
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		});
	}

	@Test
	public void testInvalid() {
		assertEquals(MesonProject.State.INVALID, new MesonProject(new File("SOME_NON_EXISTING_DIR")).getState());
	}

	@Test
	public void testBare() {
		assertEquals(MesonProject.State.BARE, new MesonProject(createProjectDir()).getState());
	}

	@Test
	public void testUninitialized() throws IOException {
		assertEquals(MesonProject.State.UNINITIALIZED,
				new MesonProject(populateProjectDir(createProjectDir())).getState());
	}

	@Test
	public void testInitialized() throws IOException, MesonException {
		assertEquals(MesonProject.State.INITIALIZED, new MesonProject(populateProjectDir(createProjectDir()))
				.initialize(new ConsoleMesonProgress()).getState());
	}

	@Test
	public void testInfo() throws IOException, MesonException {
		assertEquals("main", new MesonProject(populateProjectDir(createProjectDir()))
				.initialize(new ConsoleMesonProgress()).info().description());
	}

	@Test
	public void testTargets() throws IOException, MesonException {
		assertEquals("main@exe", new MesonProject(populateProjectDir(createProjectDir()))
				.initialize(new ConsoleMesonProgress()).targets().get(0).id());
	}

	@Test
	public void testDependencies() throws IOException, MesonException {
		assertEquals("main@exe", new MesonProject(populateProjectDir(createProjectDir()))
				.initialize(new ConsoleMesonProgress()).targets().get(0).id());
	}

	@Test
	public void testBuildTypeOption() throws IOException, MesonException {
		File projDir = createProjectDir();
		MesonProject meson = new MesonProject(populateProjectDir(projDir)).initialize(new ConsoleMesonProgress());
		meson.options().put("buildtype", "release");

		/* Recreate meson because options are cached */
		meson = new MesonProject(projDir);
		assertEquals("release", meson.options().getAsString("buildtype"));
	}

	@Test
	public void testAddOption() throws IOException, MesonException {
		File projDir = createProjectDir();
		MesonProject meson = new MesonProject(populateProjectDir(projDir)).initialize(new ConsoleMesonProgress());
		meson.options().add(new MesonOption("myopt", "myval", OptionType.STRING));

		/* Recreate meson because options are cached */
		meson = new MesonProject(projDir);
		assertEquals("myval", meson.options().getAsString("myopt"));
	}

	@Test
	public void testBuild() throws IOException, MesonException {
		MesonProject project = new MesonProject(populateProjectDir(createProjectDir()))
				.initialize(new ConsoleMesonProgress());
		project.build(new ConsoleMesonProgress());
		assertTrue(new File(project.resolvedBuildDirectory(), "main").exists());
	}

	@Test
	public void testClean() throws IOException, MesonException {
		MesonProject project = new MesonProject(populateProjectDir(createProjectDir()))
				.initialize(new ConsoleMesonProgress());
		project.build(new ConsoleMesonProgress());
		project.clean(new ConsoleMesonProgress());
		assertFalse(new File(project.resolvedBuildDirectory(), "main").exists());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownOption() throws IOException, MesonException {
		File projDir = createProjectDir();
		MesonProject meson = new MesonProject(populateProjectDir(projDir)).initialize(new ConsoleMesonProgress());
		meson.options().getAsString("XXXXXX");
	}

	@Test
	public void testSubprojects() throws IOException, MesonException {
		File projDir = populateProjectDir(createProjectDir());
		addSubProject(projDir);
		MesonProject meson = new MesonProject(projDir).initialize(new ConsoleMesonProgress());
		MesonProjectInfo info = meson.info();
		assertEquals(1, info.size());
		assertEquals("testsub1", info.iterator().next().name());
	}

	@Test
	public void testBuildSubproject() throws IOException, MesonException {
		File projDir = populateProjectDir(createProjectDir());
		addSubProject(projDir);
		ConsoleMesonProgress progress = new ConsoleMesonProgress();
		MesonProject meson = new MesonProject(projDir).initialize(progress);
		meson.build(progress);
		MesonSubproject sub = meson.info().get("testsub1");
		assertTrue(new File(sub.resolvedBuildDirectory(), "testsub1").exists());
	}

	@Test
	public void testCleanSubproject() throws IOException, MesonException {
		File projDir = populateProjectDir(createProjectDir());
		addSubProject(projDir);
		ConsoleMesonProgress progress = new ConsoleMesonProgress();
		MesonProject meson = new MesonProject(projDir).initialize(progress);
		meson.build(progress);
		meson.clean(progress);
		MesonSubproject sub = meson.info().get("testsub1");
		assertFalse(new File(sub.resolvedBuildDirectory(), "testsub1").exists());
	}

	@Test
	public void testSubprojectOption() throws IOException, MesonException {
		File projDir = populateProjectDir(createProjectDir());
		File subDir = addSubProject(projDir);
		try (PrintWriter pw = new PrintWriter(new FileWriter(new File(subDir, "meson-options.txt"), true))) {
			pw.println("option('someoption', type : 'string', value : 'optval', description : 'An option')");
		}
		MesonProject meson = new MesonProject(projDir).initialize(new ConsoleMesonProgress());
		MesonOptions options = meson.info().get("testsub1").options();
		assertEquals("optval", options.getAsString("someoption"));
		assertTrue(options.get("someoption").userDefined());
	}

	private File addSubProject(File projectDir) throws IOException {
		File subdir = new File(new File(projectDir, "subprojects"), "testsub1");
		subdir.mkdirs();
		populateProjectDir("testsub1", subdir);
		try (PrintWriter pw = new PrintWriter(new FileWriter(new File(projectDir, "meson.build"), true))) {
			pw.println("subproject('" + subdir.getName() + "')");
		}
		return subdir;
	}

	private File populateProjectDir(File dir) throws IOException {
		return populateProjectDir("main", dir);
	}

	private File populateProjectDir(String projectName, File dir) throws IOException {
		try (PrintWriter w = new PrintWriter(new FileWriter(new File(dir, "meson.build")), true)) {
			w.println("project('" + projectName + "', 'c')");
			w.println("executable('" + projectName + "', '" + projectName + ".c')");
		}
		try (PrintWriter w = new PrintWriter(new FileWriter(new File(dir, projectName + ".c")), true)) {
			w.println("#include <stdio.h>");
			w.println("int main(void) {");
			w.println("    printf(\"Hello from meson " + projectName + "!\\n\");");
			w.println("    return 0;");
			w.println("}");
		}
		return dir;
	}

	private File createProjectDir() {
		// String tmpdir = System.getProperty("java.io.tmpdir");
		String tmpdir = "tmp";

		File dir = new File(new File(new File(tmpdir), "mesontest"), UUID.randomUUID().toString());
		if (!dir.mkdirs())
			throw new IllegalStateException("Could not create temporary directory " + dir);
		tempDirs.add(dir);
		return dir;
	}
}
