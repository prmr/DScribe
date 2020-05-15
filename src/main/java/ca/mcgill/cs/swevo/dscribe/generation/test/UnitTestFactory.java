package ca.mcgill.cs.swevo.dscribe.generation.test;

import com.github.javaparser.ast.body.MethodDeclaration;

import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;

public class UnitTestFactory
{
	private final MethodDeclaration prototype;

	public UnitTestFactory(MethodDeclaration template)
	{
		prototype = template.clone();
	}

	public MethodDeclaration create(TemplateInstance instance)
	{
		MethodDeclaration clone = prototype.clone();
		clone.accept(new TemplateInstantiator(), instance);
		return clone;
	}
}
