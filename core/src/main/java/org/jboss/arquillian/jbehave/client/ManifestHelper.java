package org.jboss.arquillian.jbehave.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;

public final class ManifestHelper {
	private static final String MANIFEST_ATTR_JBEHAVE_CORE="JbehaveCore-Version";
	private static final String MANIFEST_ATTR_JBEHAVE_SITE="JbehaveSite-Version";
	
	private static ManifestHelper INSTANCE;
	private String versionJbehaveCore;
	private String versionJbehaveSite;
	
	public static ManifestHelper instance(){
		if (INSTANCE == null){
			INSTANCE = new ManifestHelper();
		}
		return INSTANCE;
	}
	
	private String findArquillianJbehaveProjectVersion(){
		InputStream is = null;
		try{
			is = ManifestHelper.class.getResourceAsStream("/META-INF/maven/org.jboss.arquillian.jbehave/arquillian-jbehave-core/pom.properties");
			Properties prop = new Properties();
			prop.load(is);
			return prop.getProperty("version");
		}catch(IOException e){
			throw new IllegalStateException("Could not get version of org.jboss.arquillian.jbehave:arquillian-jbehave-core", e);	
		} finally{
			try {
				if (is!=null){
					is.close();
				}
			} catch (IOException e) {
				throw new IllegalStateException("Error was occured, when try to find version of org.jboss.arquillian.jbehave:arquillian-jbehave-core", e);
			}
		}
	}
	
	private Manifest findArquillianJbehaveProjectManifest(){
		final MavenResolvedArtifact artifact = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.jboss.arquillian.jbehave:arquillian-jbehave-core:jar:"+findArquillianJbehaveProjectVersion()).withoutTransitivity().asSingleResolvedArtifact();
    	FileInputStream fi = null;
		try {
			fi = new FileInputStream(artifact.asFile());
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("MANIFEST.MF was not found", e);
		}
    	JarInputStream jarStream;
		try {
			jarStream = new JarInputStream(fi);
		} catch (IOException e) {
			throw new IllegalStateException("Error was occured, when try to read MANIFEST.MF was not found", e);
		}
    	return jarStream.getManifest();
	}
	
	private ManifestHelper(){
    	Manifest manifest = findArquillianJbehaveProjectManifest();
    	Attributes attributes =  manifest.getMainAttributes();
        if (attributes==null){
        	throw new IllegalStateException("MANIFEST.MF does not contain main attributes");	
        }
        
	    versionJbehaveCore = attributes.getValue(MANIFEST_ATTR_JBEHAVE_CORE);
	    if (versionJbehaveCore == null || "".equals(versionJbehaveCore)){
	       	throw new IllegalArgumentException("MANIFEST.MF does not contain "+MANIFEST_ATTR_JBEHAVE_CORE);
        }
        
	    versionJbehaveSite = attributes.getValue(MANIFEST_ATTR_JBEHAVE_SITE);
	    if (versionJbehaveSite == null || "".equals(versionJbehaveSite)){
	       	throw new IllegalArgumentException("MANIFEST.MF does not contain "+MANIFEST_ATTR_JBEHAVE_SITE);
        }
        
	}

	public String getVersionJbehaveCore() {
		return versionJbehaveCore;
	}

	public String getVersionJbehaveSite() {
		return versionJbehaveSite;
	}
	
	
}
