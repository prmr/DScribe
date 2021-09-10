package ca.mcgill.cs.swevo.dscribe.model;

import java.util.List;

import com.github.javaparser.JavaParser;

public interface Parseable
{

	public void parseCompilationUnit(JavaParser parser);

	public boolean writeToFile(List<Exception> exceptions);
}
