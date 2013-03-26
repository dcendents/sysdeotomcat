package com.orchid.tomcat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.naming.resources.FileDirContext;

/**
 * When a resource is not found under the webapp context then it will load it from the classpath. (as defined by the tomcat plugin). Used to load tag
 * files when they are defined in an eclipse project on the classpath.
 */
public class DevFileDirContext extends FileDirContext {

    private String webClassPathFile = ".#webclasspath";
    private List<String> webClassPathFolders;

    /**
     * Builds a file directory context using the given environment.
     */
    public DevFileDirContext() {
        super();
    }

    /**
     * Builds a file directory context using the given environment.
     */
    public DevFileDirContext(Hashtable<String, Object> env) {
        super(env);
    }

    @Override
    protected File file(String name) {
        File file = super.file(name);

        if (file == null) {
            if (webClassPathFolders == null) {
                // Will return the folder classpath entries and ignore the jar files
                webClassPathFolders = readWebClassPathEntries();
            }
            for (String entry : webClassPathFolders) {
                File f = new File(entry);
                file = file(f, name);
                if (file != null) {
                    break;
                }
            }
        }

        return file;
    }

    private File file(File folder, String name) {
        File file = new File(folder, name);
        if (file.exists() && file.canRead()) {

            if (allowLinking)
                return file;

            // Check that this file belongs to our root path
            String canPath = null;
            try {
                canPath = file.getCanonicalPath();
            }
            catch (IOException e) {
                // Ignore
            }
            if (canPath == null)
                return null;
        }
        else {
            return null;
        }
        return file;

    }

    /**
     * Copied from the Tomcat DevLoader class.
     * @return
     */
    protected List<String> readWebClassPathEntries() {
        List<String> rc = null;

        String prjDir = super.getDocBase();
        if (prjDir == null) {
            return new ArrayList<String>();
        }

        rc = loadWebClassPathFile(new File(prjDir));

        if (rc == null)
            rc = new ArrayList<String>();
        return rc;
    }

    /**
     * Copied from the Tomcat DevLoader class.
     * @param prjDir
     * @return
     */
    protected List<String> loadWebClassPathFile(File prjDir) {
        File cpFile = new File(prjDir, this.webClassPathFile);
        if (cpFile.exists()) {
            FileReader reader = null;
            try {
                List<String> rc = new ArrayList<String>();
                reader = new FileReader(cpFile);
                LineNumberReader lr = new LineNumberReader(reader);
                String line = null;
                while ((line = lr.readLine()) != null) {
                    line = line.replace('\\', '/');
                    File path = new File(line);
                    if (path.exists() && path.isDirectory()) {
                        rc.add(line);
                    }
                }
                return rc;
            }
            catch (IOException ioEx) {
                if (reader != null)
                    ;
                return null;
            }
        }
        return null;
    }
}
