package cn.jerry.log4j2.annotation.resolver;

import cn.jerry.logging.ConsoleLogger;
import org.apache.logging.log4j.core.util.Loader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class Resolver {

    private static final String VFSZIP = "vfszip";
    private static final String BUNDLE_RESOURCE = "bundleresource";

    /**
     * The ClassLoader to use when looking for classes.
     * If null then the ClassLoader returned by Thread.currentThread().getContextClassLoader() will be used.
     */
    private ClassLoader classloader;
    private final ResolverTester matchTester;
    /**
     * The set of matches being accumulated.
     */
    private final Set<Class<?>> classMatches = new HashSet<>();

    public Resolver(ClassLoader classloader, ResolverTester matchTester) {
        this.classloader = classloader;
        this.matchTester = matchTester;
    }

    /**
     * Provides access to the classes discovered so far.
     * If no calls have been made to any of the {@code find()} methods, this set will be empty.
     *
     * @return the set of classes that have been discovered.
     */
    public Set<Class<?>> getClasses() {
        return classMatches;
    }

    /**
     * Returns the classloader that will be used for scanning for classes.
     * If no explicit ClassLoader has been set by the calling, the context class loader will be used.
     *
     * @return the ClassLoader that will be used to scan for classes
     */
    public ClassLoader getClassLoader() {
        if (classloader == null) {
            classloader = Loader.getClassLoader(Resolver.class, null);
        }
        return classloader;
    }

    /**
     * Sets an explicit ClassLoader that should be used when scanning for classes.
     * If none is set then the context classloader will be used.
     *
     * @param classloader a ClassLoader to use when scanning for classes
     */
    public void setClassLoader(final ClassLoader classloader) {
        this.classloader = classloader;
    }

    /**
     * Attempts to discover classes that pass the test.
     * Accumulated classes can be accessed by calling {@link #getClasses()}.
     *
     * @param packageNames one or more package names to scan (including subpackages) for classes
     */
    public void find(final String... packageNames) {
        if (packageNames == null) {
            return;
        }

        for (final String pkg : packageNames) {
            findInPackage(pkg);
        }
    }

    /**
     * Scans for classes starting at the package provided and descending into subpackages.
     * Each class is offered up to the Test as it is discovered, and if the Test returns true the class is retained.
     * Accumulated classes can be fetched by calling {@link #getClasses()}.
     *
     * @param packageName the name of the package from which to start scanning for classes, e.g. {@code net.sourceforge.stripes}
     */
    public void findInPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        final String pkgName = "" + packageName.replace('.', '/');
        final ClassLoader loader = getClassLoader();
        Enumeration<URL> urls;

        try {
            urls = loader.getResources(pkgName);
        } catch (final IOException ioe) {
            ConsoleLogger.error(this.getClass(), "Could not read package: " + pkgName, ioe);
            return;
        }

        while (urls.hasMoreElements()) {
            try {
                final URL url = urls.nextElement();
                final String urlPath = extractPath(url);

                // Check for a jar in a war in JBoss
                if (BUNDLE_RESOURCE.equals(url.getProtocol())) {
                    ConsoleLogger.info(Resolver.class, "Unsupported protocol: " + BUNDLE_RESOURCE);
                } else if (VFSZIP.equals(url.getProtocol())) {
                    final String path = urlPath.substring(0, urlPath.length() - pkgName.length() - 2);
                    final URL newURL = new URL(url.getProtocol(), url.getHost(), path);
                    try (JarInputStream stream = new JarInputStream(newURL.openStream())) {
                        loadImplementationsInJar(pkgName, path, stream);
                    }
                } else {
                    final File file = new File(urlPath);
                    if (file.isDirectory()) {
                        loadImplementationsInDirectory(pkgName, file);
                    } else {
                        loadImplementationsInJar(pkgName, file);
                    }
                }
            } catch (final IOException | URISyntaxException ioe) {
                ConsoleLogger.error(this.getClass(), "could not read entries", ioe);
            }
        }
    }

    /**
     * scan all packages in project path
     */
    public void findInProject() {
        URL url = Resolver.class.getResource("");
        String path;
        try {
            path = extractPath(url);
        } catch (Exception e) {
            ConsoleLogger.error(this.getClass(), "Finding cancelled becauseof failing to extract path, url: " + url + ", error1: "
                    + e.getMessage());
            return;
        }
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        String packageName = Resolver.class.getPackage().getName().replace('.', '/');
        if (path.endsWith(packageName)) {
            path = url.getPath().substring(0, url.getPath().length() -
                    packageName.length() - 2);
        }
        List<String> jarsPath = new ArrayList<>();
        List<String> filesPath = new ArrayList<>();
        List<String> classes = new ArrayList<>();
        if (BUNDLE_RESOURCE.equals(url.getProtocol())) {
            return;
        } else if (url.getProtocol().startsWith("vfs")) {
            // for jboss
            findInJBoss(path, url, jarsPath, classes);
        } else {
            // for tomcat
            findInTomcat(path, filesPath);
        }
        for (String jarPath : jarsPath) {
            loadImplementationsInJar(url.getProtocol(), url.getHost(), jarPath);
        }
        for (String filePath : filesPath) {
            final File file = new File(filePath);
            if (file.isDirectory()) {
                loadImplementationsInDirectory(null, file);
            } else {
                scanJarsAndClasses(filePath, null, classes);
            }
        }
        for (String clazz : classes) {
            addIfMatching(clazz);
        }
    }

    private void findInJBoss(String path, URL url, List<String> jarsPath, List<String> classes) {
        if (!VFSZIP.equals(url.getProtocol())) return;
        // scan jars and classes in ear/war
        String basePath;
        int index = path.indexOf(".war");
        if (index == -1) index = path.indexOf(".ear");
        basePath = path.substring(0, index + 4);
        scanJarsAndClasses(basePath, jarsPath, classes);
    }

    private void findInTomcat(String path, List<String> filesPath) {
        int index = path.indexOf("/WEB-INF/");
        if (index == -1) {
            filesPath.add(path);
            URL classesUrl = Resolver.class.getResource("/");
            if (classesUrl != null) filesPath.add(classesUrl.getPath());
        } else {
            // scan jar
            String basePath = path.substring(0, index) + "/WEB-INF/lib/";
            String[] subFiles = new File(basePath).list();
            if (subFiles != null) {
                for (String subFile : subFiles) {
                    if (subFile.endsWith(".jar")) {
                        filesPath.add(basePath + subFile);
                    }
                }
            }
            // add classes
            basePath = path.substring(0, index) + "/WEB-INF/classes/";
            if (new File(basePath).exists()) {
                filesPath.add(basePath);
            }
        }
    }

    /**
     * scan jars in lib directory in a war/ear
     */
    private void scanJarsAndClasses(final String path, List<String> jarsPath, List<String> classes) {
        try {
            URL newURL = new URL("jar:file:" + path + "!/");
            JarURLConnection jarURLConnection = (JarURLConnection) newURL.openConnection();
            JarFile jarFile = jarURLConnection.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            JarEntry jarEntry;
            String jarEntryName;
            int index;
            while (jarEntries.hasMoreElements()) {
                jarEntry = jarEntries.nextElement();
                jarEntryName = jarEntry.getName();
                if (!jarEntry.isDirectory() && jarEntryName.endsWith(".jar")) {
                    if (jarsPath != null) {
                        jarsPath.add(path + "/" + jarEntryName);
                    }
                } else if (!jarEntry.isDirectory() && jarEntryName.endsWith(".class") && classes != null) {
                    index = jarEntryName.indexOf("/classes/");
                    if (index != -1) {
                        jarEntryName = jarEntryName.substring(index + "/classes/".length());
                    }
                    classes.add(jarEntryName);
                }
            }
        } catch (final IOException ioe) {
            ConsoleLogger.error(this.getClass(), "Scan jar file failed, path: " + path + ", error2: " + ioe.getMessage());
        }
    }

    String extractPath(final URL url) throws UnsupportedEncodingException, URISyntaxException {
        String urlPath = url.getPath(); // same as getFile but without the Query portion

        // I would be surprised if URL.getPath() ever starts with "jar:" but no harm in checking
        if (urlPath.startsWith("jar:")) {
            urlPath = urlPath.substring(4);
        }
        // For jar: URLs, the path part starts with "file:"
        if (urlPath.startsWith("file:")) {
            urlPath = urlPath.substring(5);
        }
        // If it was in a JAR, grab the path to the jar
        if (urlPath.indexOf('!') != -1) {
            urlPath = urlPath.substring(0, urlPath.indexOf('!'));
        }

        // Finally, decide whether to URL-decode the file name or not...
        final String protocol = url.getProtocol();
        final List<String> neverDecode = Arrays.asList(VFSZIP, BUNDLE_RESOURCE);
        if (neverDecode.contains(protocol)) {
            return urlPath;
        }
        final String cleanPath = new URI(urlPath).getPath();
        if (new File(cleanPath).exists()) {
            // if URL-encoded file exists, don't decode it
            return cleanPath;
        }
        return URLDecoder.decode(urlPath, ResolverConstants.UTF_8.name());
    }

    /**
     * Finds matches in a physical directory on a filesystem.
     * Examines all files within a directory - if the File object is not a directory, and ends with <i>.class</i>
     * the file is loaded and tested to see if it is acceptable according to the Test.
     * Operates recursively to find classes within a folder structure matching the package structure.
     *
     * @param parent   the package name up to this directory in the package hierarchy. E.g. if /classes is in the classpath and
     *                 we wish to examine files in /classes/org/apache then the values of <i>parent</i> would be
     *                 <i>org/apache</i>
     * @param location a File object representing a directory
     */
    private void loadImplementationsInDirectory(final String parent, final File location) {
        if (!location.isDirectory()) return;

        List<File> subFiles = new ArrayList<>();
        List<String> parents = new ArrayList<>();
        subFiles.add(location);
        parents.add(parent);
        File file;
        File[] temp;
        String fileParent;
        int size = subFiles.size();
        for (int i = 0; i < size; i++) {
            file = subFiles.get(i);
            fileParent = parents.get(i);
            if (file.isDirectory()) {
                temp = file.listFiles();
                if (temp != null) {
                    dealSubDir(temp, subFiles, parents, fileParent);
                    size += temp.length;
                }
            } else {
                addIfMatching(fileParent);
            }
        }
    }

    private void dealSubDir(File[] temp, List<File> subFiles, List<String> parents, String fileParent) {
        for (File subFile : temp) {
            subFiles.add(subFile);
            parents.add(fileParent == null || fileParent.isEmpty()
                    ? subFile.getName()
                    : fileParent + "/" + subFile.getName());
        }
    }

    /**
     * Finds matching classes within a jar files that contains a folder structure matching the package structure.
     * If the File is not a JarFile or does not exist a warning will be logged, but no error will be raised.
     *
     * @param parent  the parent package under which classes must be in order to be considered
     * @param jarFile the jar file to be examined for classes
     */
    private void loadImplementationsInJar(final String parent, final File jarFile) {
        String errorMsg = "Could not search jar file '%s' for classes matching criteria: ";
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile))) {
            loadImplementationsInJar(parent, jarFile.getPath(), jarStream);
        } catch (final FileNotFoundException ex) {
            ConsoleLogger.error(this.getClass(), String.format(errorMsg, jarFile) + this.matchTester + " file not found", ex);
        } catch (final IOException ioe) {
            ConsoleLogger.error(this.getClass(), String.format(errorMsg, jarFile) + this.matchTester + " due to an IOException", ioe);
        }
    }

    private void loadImplementationsInJar(final String protocol,
            final String host, final String jarPath) {
        URL newURL;
        try {
            newURL = new URL(protocol, host, jarPath);
        } catch (Exception e) {
            ConsoleLogger.error(this.getClass(), "Failed to create JarInputStream, url: " + jarPath
                    + ", error3: " + e.getMessage());
            return;
        }
        try (JarInputStream stream = new JarInputStream(newURL.openStream())) {
            loadImplementationsInJar("", jarPath, stream);
        } catch (Exception e) {
            ConsoleLogger.error(this.getClass(), "Failed to create JarInputStream, url: " + jarPath
                    + ", error: " + e.getMessage());
        }
    }

    /**
     * Finds matching classes within a jar files that contains a folder structure matching the package structure.
     * If the File is not a JarFile or does not exist a warning will be logged, but no error will be raised.
     *
     * @param parent the parent package under which classes must be in order to be considered
     * @param stream The jar InputStream
     */
    private void loadImplementationsInJar(final String parent, final String path,
            final JarInputStream stream) {
        try {
            String pkg = parent == null ? "" : parent;
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                final String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(pkg)) {
                    addIfMatching(name);
                }
            }
        } catch (final IOException ioe) {
            ConsoleLogger.error(this.getClass(), "Could not search jar file '" + path
                    + "' for classes matching criteria: " + this.matchTester + " due to an IOException", ioe);
        }
    }

    /**
     * Add the class designated by the fully qualified class name provided to the set of resolved classes
     * if and only if it is approved by the Test supplied.
     *
     * @param fqName the fully qualified name of a class
     */
    protected void addIfMatching(final String fqName) {
        try {
            final ClassLoader loader = getClassLoader();
            String externalName = fqName.substring(0, fqName.indexOf('.')).replace('/', '.');
            while (externalName.indexOf('.') == 0) {
                externalName = externalName.substring(1);
            }
            if (!this.matchTester.matchesPkg(externalName)) {
                return;
            }

            final Class<?> type = loader.loadClass(externalName);
            if (this.matchTester.matchesAnnotation(type)) {
                classMatches.add(type);
            }
        } catch (Exception e) {
            ConsoleLogger.error(this.getClass(), "Could not examine class: " + fqName
                    + ", error4: " + e.getMessage());
        }
    }

}
