package ca.mcgill.cs.swevo.dscribe.setup;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Properties;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPairFactory;

public class Setup {

	public static Context setupContext() 
	{
		Context context = Context.instance();
		Properties properties = new Properties();
		properties.setProperty("templateRepoPath", "/dscribe/templates");
		properties.setProperty("binFolder", "bin");
		properties.setProperty("srcFolder", "testdata");
		properties.setProperty("testFolder", "testdata");
		properties.setProperty("testClassNameConvention", "prefix");
		context.configure(System.getProperty("user.dir"), properties);
		return context;
	}

	public static Path getPathToClass(Context context, String className, String targetFolder)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException 
	{
		FocalTestPairFactory focalTestPairFactory = new FocalTestPairFactory(context);
		Method getPath = focalTestPairFactory.getClass().getDeclaredMethod("getPathToClass", String.class,
				String.class);
		getPath.setAccessible(true);
		return (Path) getPath.invoke(focalTestPairFactory, className, targetFolder);
	}

	public static CompilationUnit parse(String pathToFile) throws FileNotFoundException 
	{
		JavaParser parser = new JavaParser(new ParserConfiguration());
		File file = new File(pathToFile);
		ParseResult<CompilationUnit> result = parser.parse(file);
		return result.getResult().get();
	}
}
