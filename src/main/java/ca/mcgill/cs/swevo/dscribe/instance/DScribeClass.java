package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.List;

import com.github.javaparser.JavaParser;

import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;


public interface DScribeClass 
{
	public void parseCompilationUnit(JavaParser parser);
	public boolean validate(List<String> warnings, TemplateRepository repository);
	public boolean writeToFile(List<Exception> exceptions);
}
