package ca.mcgill.cs.swevo.dscribe.instance;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.Expose;

import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.TypeNameResolver;

/**
 * A FocalMethod is unit of test (usually a method) Holds reference to templates.
 */
public class FocalMethod implements Iterable<TemplateInstance>
{
	@Expose
	private String name;
	@Expose
	private List<String> parameters;
	@Expose
	public List<TemplateInstance> tests = new ArrayList<>();

	public FocalMethod(String name, List<String> parameters)
	{
		this.name = name;
		if (parameters != null)
		{
			this.parameters = List.copyOf(parameters);
		}
		else
		{
			this.parameters = null;
		}
	}

	public void addTest(TemplateInstance test)
	{
		tests.add(test);
	}

	@Override
	public Iterator<TemplateInstance> iterator()
	{
		return tests.iterator();
	}

	public String getName()
	{
		return name;
	}

	public Optional<List<String>> getParameters()
	{
		return Optional.ofNullable(parameters);
	}

	public boolean validate(List<String> warnings, Class<?> declaringType, TemplateRepository repository)
	{
		try
		{
			tryFindMethod(declaringType);
		}
		catch (NoSuchMethodException e)
		{
			warnings.add("METHOD DOES NOT EXIST: Could not find java method " + name + " in the source code.");
			return false;
		}
		catch (ClassNotFoundException e)
		{
			warnings.add("INVALID PARAMETERS: Invalid parameter for method " + name + ".");
			return false;
		}
		InstanceContext context = new InstanceContext(declaringType);
		for (Iterator<TemplateInstance> iter = tests.iterator(); iter.hasNext();)
		{
			TemplateInstance test = iter.next();
			test.setContext(context);
			boolean valid = test.validate(warnings, repository);
			if (!valid)
			{
				iter.remove();
			}
		}
		return true;
	}

	private void tryFindMethod(Class<?> declaringType) throws ClassNotFoundException, NoSuchMethodException
	{
		boolean isConstructor = name.equals(declaringType.getSimpleName());
		if (parameters == null)
		{
			if (isConstructor)
			{
				if (declaringType.getDeclaredConstructors().length == 0)
				{
					throw new NoSuchMethodException();
				}
			}
			else
			{
				for (Method method : declaringType.getDeclaredMethods())
				{
					if (method.getName().equals(name))
					{
						return;
					}
				}
				throw new NoSuchMethodException();
			}
		}
		else
		{
			Class<?>[] parameterTypes = new Class<?>[parameters.size()];
			for (int i = 0; i < parameterTypes.length; i++)
			{
				parameterTypes[i] = TypeNameResolver.resolve(parameters.get(i));
			}
			if (isConstructor)
			{
				declaringType.getDeclaredConstructor(parameterTypes);
			}
			else
			{
				declaringType.getDeclaredMethod(name, parameterTypes);
			}
		}
	}
}
