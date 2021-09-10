package ca.mcgill.cs.swevo.dscribe.model;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.mcgill.cs.swevo.dscribe.Context;

public class ClassBuilder
{
	private Class<?> clazz;
	private Context context;

	public ClassBuilder withClass(Class<?> clazz)
	{
		assert clazz != null;
		this.clazz = clazz;
		return this;
	}

	public ClassBuilder withContext(Context context)
	{
		assert context != null;
		this.context = context;
		return this;
	}

	public FocalClass buildFocalClass()
	{
		Path binPath = getBinPath();
		Path classPath = classPathFromBinPath(binPath, context.sourceFolder());
		return new FocalClass(classPath, clazz);
	}

	public TestClass buildTestClass()
	{
		Path binPath = getBinPath();
		Path classPath = classPathFromBinPath(binPath, context.testFolder());
		return new TestClass(classPath);
	}

	private Path getBinPath()
	{
		URL binUrl = context.classLoader().getResource(resourceName());
		Path binPath = Paths.get(URI.create(binUrl.toString()));
		return binPath;
	}

	private String resourceName()
	{
		String className = clazz.getName();
		return className.replaceAll(Pattern.quote("."), "/") + ".class";
	}

	private static Path classPathFromBinPath(Path binPath, String targetFolder)
	{
		// TO DO : define src, test, bin in context
		String binFolder = Pattern.quote(File.separator + "bin" + File.separator);
		String srcFolder = Matcher.quoteReplacement(File.separator + targetFolder + File.separator);
		String binExt = Pattern.quote(".class");
		String srcExt = Matcher.quoteReplacement(".java");

		String binLocation = binPath.toString();
		String srcLocation = binLocation.replaceFirst(binFolder, srcFolder).replaceFirst(binExt, srcExt);
		return Paths.get(srcLocation);
	}
}
