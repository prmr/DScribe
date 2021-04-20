package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.List;

import com.github.javaparser.JavaParser;

import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

public class FocalTestPair implements DScribeClass
{
	private final FocalClass focalClass;
	private final TestClass testClass;
	
	public FocalTestPair(FocalClass focalClass, TestClass testClass)
	{
		assert focalClass != null && testClass != null;
		this.focalClass = focalClass;
		this.testClass = testClass;
	}

	@Override
	public void parseCompilationUnit(JavaParser parser) 
	{
		focalClass.parseCompilationUnit(parser);
		testClass.parseCompilationUnit(parser);		
	}

	@Override
	public boolean validate(List<String> warnings, TemplateRepository repository) 
	{
//		boolean validFocal = focalClass.validate(warnings, repository);
//		boolean validTest = testClass.validate(warnings, repository);
//		return validFocal && validTest; 
		return true;
	}

	public void extractTemplateDataFromAnnotations(boolean docs)
	{
		SrcTemplateDataInstantiator stdp = new SrcTemplateDataInstantiator();
		TestTemplateDataInstantiator ttdp = new TestTemplateDataInstantiator();
		stdp.visit(focalClass.compilationUnit(), focalClass);
		if (docs)
			ttdp.visit(testClass.compilationUnit(), focalClass);
	}
	
	public FocalClass focalClass()
	{
		return focalClass;
	}
	
	public TestClass testClass()
	{
		return testClass;
	}

	@Override
	public boolean writeToFile(List<Exception> exceptions) 
	{
		boolean success = testClass.writeToFile(exceptions);
		if (success)
		{
			success = focalClass.writeToFile(exceptions);
			return success;
		}
		return false;
	}
}
