package org.icemoon.jameson;

import java.io.IOException;

public interface MesonBackend {

	void build(MesonProject project, MesonProgress progress) throws IOException, MesonException;

	void clean(MesonProject project, MesonProgress progress) throws IOException, MesonException;
}
