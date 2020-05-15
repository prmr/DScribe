package ca.mcgill.cs.swevo.dscribe.template;

public interface TemplateRepository extends Iterable<String>
{
	/**
	 * Query to verify if a specific template method exists
	 *
	 * @param templateName
	 *            the name of the template
	 * @return true if a template exists for the specified name
	 */
	public boolean contains(String templateName);

	/**
	 * Query for a template method of the scaffold using its name. The MethodDeclaration object returned is a clone to
	 * ensure the original one is untouched.
	 *
	 * @param templateName
	 *            Name of template method.
	 * @return MethodDeclaration clone of the template method.
	 */
	public Template get(String templateName);
}
