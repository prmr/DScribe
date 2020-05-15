package ca.mcgill.cs.swevo.dscribe.parsing;

import java.util.ArrayList;
import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;

import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

public class UnitTestParser
{
	private final TemplateRepository repository;

	public UnitTestParser(TemplateRepository repository)
	{
		this.repository = repository;
	}

	public Optional<TemplateInstance> parse(MethodDeclaration test, String pkgName, String clsName)
	{
		for (String templateName : repository)
		{
			Template template = repository.get(templateName);
			Optional<UnitTestMatcher> matcher = template.getMatcher();
			if (matcher.isEmpty())
			{
				continue;
			}
			Optional<TemplateInstance> match = matcher.get().match(test, pkgName, clsName);
			if (match.isPresent() && match.get().validate(new ArrayList<>(), repository))
			{
				return match;
			}
		}
		return Optional.empty();
	}
}
