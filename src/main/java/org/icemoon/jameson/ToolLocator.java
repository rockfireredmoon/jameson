package org.icemoon.jameson;

import java.io.FileNotFoundException;

public interface ToolLocator {

	String findTool(String tool) throws FileNotFoundException;
}
