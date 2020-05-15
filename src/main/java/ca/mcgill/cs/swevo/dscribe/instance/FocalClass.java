package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.annotations.Expose;

import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.TypeNameResolver;

/**
 * A FocalClass Holds reference to different units under tests. (usually methods)
 */
public class FocalClass implements Iterable<FocalMethod>
{
	@Expose
	private String fullname;
	@Expose
	private List<FocalMethod> methods = new ArrayList<>();

	private String simpleName = null;
	private String packageName = null;

	public FocalClass(String name)
	{
		fullname = name;
	}

	public String getName()
	{
		return fullname;
	}

	public void addFocalMethod(FocalMethod focalMethod)
	{
		methods.add(focalMethod);
	}

	public String getSimpleName()
	{
		assert simpleName != null;
		return simpleName;
	}

	public String getPackageName()
	{
		assert packageName != null;
		return packageName;
	}

	@Override
	public Iterator<FocalMethod> iterator()
	{
		return methods.iterator();
	}

	public boolean validate(List<String> warnings, TemplateRepository repository)
	{
		Class<?> type;
		try
		{
			type = TypeNameResolver.resolve(fullname);
			simpleName = type.getSimpleName();
			packageName = type.getPackageName();
		}
		catch (ClassNotFoundException e)
		{
			warnings.add("CLASS DOES NOT EXIST: Could find java class " + fullname
					+ " in the source code. Please provide valid canonical name.");
			return false;
		}
		for (Iterator<FocalMethod> iter = methods.iterator(); iter.hasNext();)
		{
			FocalMethod focus = iter.next();
			boolean valid = focus.validate(warnings, type, repository);
			if (!valid)
			{
				iter.remove();
			}
		}
		return true;
	}
}
