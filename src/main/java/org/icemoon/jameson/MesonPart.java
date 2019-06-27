package org.icemoon.jameson;

import java.io.File;
import java.io.IOException;

public interface MesonPart {

	File directory();

	File resolvedBuildDirectory();
	
	MesonOptions options() throws IOException, MesonException;
	
	MesonProject project();
}
