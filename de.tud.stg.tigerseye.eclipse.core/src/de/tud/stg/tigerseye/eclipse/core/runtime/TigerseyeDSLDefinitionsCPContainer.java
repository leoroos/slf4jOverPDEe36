package de.tud.stg.tigerseye.eclipse.core.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tud.stg.tigerseye.eclipse.core.DSLDefinition;
import de.tud.stg.tigerseye.eclipse.core.TigerseyeCore;

public class TigerseyeDSLDefinitionsCPContainer implements IClasspathContainer {

    public static final IPath CONTAINER_ID = new Path(
	    "TIGERSEYE_DSL_DEFINITIONS_SUPPORT");
    // TigerseyeClasspathContainer.CONTAINER_ID
    // .append(new Path("TIGERSEYE_DSL_DEFINITIONS_SUPPORT"));

    private static final String metaInfDir = "META-INF";

    private static final String projectRootDir = ".";

    private static final String defaultBinName = "bin";

    private static final Logger logger = LoggerFactory
	    .getLogger(TigerseyeDSLDefinitionsCPContainer.class);

    private final List<IClasspathEntry> cpEntries = new ArrayList<IClasspathEntry>();

    public TigerseyeDSLDefinitionsCPContainer(IProject project) {
	logger.trace("Adding DSL specific classpath for {}", project.getName());
	reset();
    }

    public void reset() {
	List<DSLDefinition> dslDefinitions = TigerseyeCore
		.getLanguageProvider().getDSLDefinitions();
	Set<String> iteratedContributor = new HashSet<String>();
	for (DSLDefinition dslDefinition : dslDefinitions) {
	    /*
	     * don't add same DSL from different contributors twice
	     */
	    if (dslDefinition.isActive()
		    && !iteratedContributor.contains(dslDefinition
			    .getContributorSymbolicName())) {
		try {
		    addProjectPathForDSL(dslDefinition);
		} catch (IOException e) {
		    logger.error("Failed to add dsl  {} to classpath",
			    dslDefinition, e);
		}
	    }
	}

	logger.trace("added classpathentries: {}", this.cpEntries);
    }

    private void addProjectPathForDSL(DSLDefinition dslDefinition)
	    throws IOException {
	String contributorSymbolicName = dslDefinition
		.getContributorSymbolicName();
	Bundle bundle = Platform.getBundle(contributorSymbolicName);
	// File bundleFile = new FileLocatorWrapper().getBundleFile(bundle);

	List<File> cpEntriesToAdd = null;
	// File buildProps = new File(bundleFile, "build.properties");
	// if (buildProps.exists()) {
	// cpEntriesToAdd = getClasspathEntriesForBundleProperties(bundleFile,
	// buildProps);
	// } else {
	// if (true)
	// throw new Error("unexpected");
	    DSLClasspathResolver resolver = new DSLClasspathResolver();
	    File[] resolveCPEntriesForBundle = resolver
		    .resolveCPEntriesForBundle(bundle);
	    if (resolveCPEntriesForBundle == null)
		cpEntriesToAdd = Collections.emptyList();
	    else
		cpEntriesToAdd = Arrays.asList(resolveCPEntriesForBundle);
	// }

	for (File cpEntry : cpEntriesToAdd) {
	    addFileAsCPEntryIfExistant(cpEntry);
	}
    }

    private List<File> getClasspathEntriesForBundleProperties(File bundleFile,
	    File buildProps) {
	Properties properties = new Properties();
	FileInputStream fileInputStream = null;
	try {
	    fileInputStream = new FileInputStream(buildProps);
	    properties.load(fileInputStream);
	} catch (Exception e) {
	    logger.trace("failed to get inputstream for {}", buildProps, e);
	    IOUtils.closeQuietly(fileInputStream);
	}

	List<String> dirFiles = new ArrayList<String>();
	/*
	 * usually says nothing about classpath
	 */
	// getResourceNamesForProperty(properties, "output..");
	List<String> includes = getResourceNamesForProperty(properties,
		"bin.includes");

	List<File> jars = new LinkedList<File>();
	for (String string : includes) {
	    File cpFile = new File(bundleFile, string);
	    if (isJarFile(cpFile)) {
		jars.add(cpFile);
	    } else if (cpFile.isDirectory()) {
		dirFiles.add(string);
	    }
	}
	List<File> resultCPEntries = new ArrayList<File>();
	resultCPEntries.addAll(jars);
	List<File> classFolders = getClassFolders(dirFiles, bundleFile);
	resultCPEntries.addAll(classFolders);
	return resultCPEntries;
    }

    private List<File> getClassFolders(List<String> cpDirFiles, File bundleFile) {
	/*
	 * TODO refactor: should consider includes+projectroot as default cp and
	 * the Manifest classpath. Additionally to provide access during
	 * development allow bin folder.
	 */
	/*
	 * If class output folder exists it can not be nested inside a project
	 * root folder which as well is declared as class folder. Such a
	 * combination will cause unresolved class path problems. I assume that
	 * the relevant class folder is the specifically as "output.." property
	 * defined folder.
	 */
	int metaInfIndex = hasStringBeginningWith(cpDirFiles, metaInfDir);
	if (metaInfIndex >= 0) {
	    cpDirFiles.remove(metaInfIndex);
	}
	// int stdBinIndex = hasStringBeginningWith(cpDirFiles, defaultBinName);
	// boolean hasStdBin = stdBinIndex >= 0;
	// if (!hasStdBin) {
	boolean hasStdBin = containsBinFolder(bundleFile);
	if (hasStdBin) {
	    cpDirFiles.add(defaultBinName);
	    // }
	} else {
	    cpDirFiles.add(projectRootDir);
	}
	int rootCPIndex = hasStringBeginningWith(cpDirFiles, projectRootDir);
	boolean hasRootCP = rootCPIndex >= 0;
	if (hasRootCP && hasStdBin) {
	    cpDirFiles.remove(rootCPIndex);
	    hasRootCP = false;
	}

	List<File> classFolder = new ArrayList<File>();
	for (String string : cpDirFiles) {
	    File cpFolderFile = new File(bundleFile, string);
	    // TODO refactor this is just a quick and dirty workaround
	    if (!string.startsWith(projectRootDir) && hasRootCP
		    && cpFolderFile.isDirectory()) {
		logger.trace(
			"not adding {} to avoid nesting conflicts since root directory is also on classpath",
			string);
	    } else {
		classFolder.add(cpFolderFile);
	    }
	}
	return classFolder;
    }

    /**
     * Returns error code depending on whether {@code beginningWith} is
     * contained in {@code cpDirFiles} List.
     * 
     * @param cpDirFiles
     * @param beginningWith
     * @return <ul>
     *         <li>{@code -1} if no string beginning with {@code beginningWith}
     *         is contained
     *         <li>a integer greater or equal {@code 0} if {@code beginningWith}
     *         is contained. The integer represents the entry that starts with
     *         the {@code beginningWith} string.
     *         </ul>
     */
    private int hasStringBeginningWith(List<String> cpDirFiles,
	    String beginningWith) {
	for (int i = 0; i < cpDirFiles.size(); i++) {
	    if (cpDirFiles.get(i).startsWith(beginningWith))
		return i;
	}
	return -1;
    }

    private boolean containsBinFolder(File bundleFile) {
	String[] list = bundleFile.list();
	for (String string : list) {
	    if (string.equals(defaultBinName))
		return true;
	}
	return false;
    }

    private void addJarsAsCPEntry(List<File> jars) {
	for (File jar : jars) {
	    addFileAsCPEntryIfExistant(jar);
	}
    }

    private List<String> getResourceNamesForProperty(Properties properties,
	    String property) {
	LinkedList<String> resources = new LinkedList<String>();
	String resourcesCSV = properties.getProperty(property, null);
	if (resourcesCSV != null) {
	    String[] resourcesArray = resourcesCSV.split(",");
	    for (String resourceName : resourcesArray) {
		resources.add(resourceName);
	    }
	}
	return resources;
    }

    private void addFileAsCPEntryIfExistant(File cpFile) {
	if (cpFile.exists()) {
	    Path cpEntryPath = new Path(cpFile.getAbsolutePath());
	    IClasspathEntry newLibraryEntry = JavaCore.newLibraryEntry(
		    cpEntryPath, null, null);
	    this.cpEntries.add(newLibraryEntry);
	}
    }

    private boolean isJarFile(File cpFile) {
	return cpFile.isFile() && cpFile.getName().endsWith(".jar");
    }

    @Override
    public IClasspathEntry[] getClasspathEntries() {
	return this.cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
    }

    @Override
    public String getDescription() {
	return "Tigerseye DSL Definitions";
    }

    @Override
    public int getKind() {
	return IClasspathContainer.K_APPLICATION;
    }

    @Override
    public IPath getPath() {
	return CONTAINER_ID;
    }

}