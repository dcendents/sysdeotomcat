package org.apache.catalina.loader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;

public class DevLoader extends WebappLoader
{
  private static final String info = "org.apache.catalina.loader.DevLoader/1.0";
  private String webClassPathFile = ".#webclasspath";
  private String tomcatPluginFile = ".tomcatplugin";

  public DevLoader() {
  }

  public DevLoader(ClassLoader parent) {
    super(parent);
  }

  public void startInternal()
    throws LifecycleException
  {
    log("Starting DevLoader");

    super.startInternal();

    ClassLoader cl = super.getClassLoader();
    if (!(cl instanceof WebappClassLoader)) {
      logError("Unable to install WebappClassLoader !");
      return;
    }
    WebappClassLoader devCl = (WebappClassLoader)cl;

    List webClassPathEntries = readWebClassPathEntries();
    StringBuffer classpath = new StringBuffer();
    for (Iterator it = webClassPathEntries.iterator(); it.hasNext(); ) {
      String entry = (String)it.next();
      File f = new File(entry);
      if (f.exists()) {
        if ((f.isDirectory()) && (!entry.endsWith("/"))) f = new File(entry + "/"); try
        {
          URL url = f.toURL();

          devCl.addRepository(url.toString());
          classpath.append(f.toString() + File.pathSeparatorChar);
          log("added " + url.toString());
        } catch (MalformedURLException e) {
          logError(entry + " invalid (MalformedURL)");
        }
      } else {
        logError(entry + " does not exist !");
      }

    }

    String cp = (String)getServletContext().getAttribute("org.apache.catalina.jsp_classpath");
    StringTokenizer tokenizer = new StringTokenizer(cp, File.pathSeparatorChar);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();

      if ((token.charAt(0) == '/') && (token.charAt(2) == ':')) token = token.substring(1);
      classpath.append(token + File.pathSeparatorChar);
    }

    getServletContext().setAttribute("org.apache.catalina.jsp_classpath", classpath.toString());
    log("JSPCompiler Classpath = " + classpath);
  }

  protected void log(String msg)
  {
    System.out.println("[DevLoader] " + msg);
  }
  protected void logError(String msg) {
    System.err.println("[DevLoader] Error: " + msg);
  }

  protected List readWebClassPathEntries() {
    List rc = null;

    File prjDir = getProjectRootDir();
    if (prjDir == null) {
      return new ArrayList();
    }
    log("projectdir=" + prjDir.getAbsolutePath());

    rc = loadWebClassPathFile(prjDir);

    if (rc == null) rc = new ArrayList();
    return rc;
  }

  protected File getProjectRootDir() {
    File rootDir = getWebappDir();
    FileFilter filter = new FileFilter()
    {
      public boolean accept(File file) {
        return (file.getName().equalsIgnoreCase(DevLoader.this.webClassPathFile)) || 
          (file.getName().equalsIgnoreCase(DevLoader.this.tomcatPluginFile));
      }
    };
    while (rootDir != null) {
      File[] files = rootDir.listFiles(filter);
      if ((files != null) && (files.length >= 1)) {
        return files[0].getParentFile();
      }
      rootDir = rootDir.getParentFile();
    }
    return null;
  }

  protected List loadWebClassPathFile(File prjDir) {
    File cpFile = new File(prjDir, this.webClassPathFile);
    if (cpFile.exists()) {
      FileReader reader = null;
      try {
        List rc = new ArrayList();
        reader = new FileReader(cpFile);
        LineNumberReader lr = new LineNumberReader(reader);
        String line = null;
        while ((line = lr.readLine()) != null)
        {
          line = line.replace('\\', '/');
          rc.add(line);
        }
        return rc;
      } catch (IOException ioEx) {
        if (reader != null);
        return null;
      }
    }
    return null;
  }

  protected ServletContext getServletContext()
  {
    return ((Context)getContainer()).getServletContext();
  }

  protected File getWebappDir() {
    File webAppDir = new File(getServletContext().getRealPath("/"));
    return webAppDir;
  }
}