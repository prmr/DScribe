package ca.mcgill.cs.swevo.dscribe.generation.doc;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.mcgill.cs.swevo.dscribe.instance.PlaceholderValue;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;

public class DocumentationFactory {
	
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\S*?\\$");
	private static final Pattern IFTHEN_PATTERN = Pattern.compile("IF:\\s(.*)\\sTHEN:\\s(.*)");
	private static final Pattern THREEPART_PATTERN = Pattern.compile("(\\[[^\\[\\]]*\\]\\s*){3}");
	
	private final Function<TemplateInstance, InfoFragment> factory;
	
	public DocumentationFactory(String format) {
		factory = fragmentFactory(format.trim());
	}
	
	private Function<TemplateInstance, InfoFragment> fragmentFactory(String format) {
		Matcher matcher = IFTHEN_PATTERN.matcher(format);
		if (matcher.matches()) {
			Function<TemplateInstance, Statement> ifFactory = statementFactory(matcher.group(1).trim(), true);
			Function<TemplateInstance, Statement> thenFactory = statementFactory(matcher.group(2).trim(), false);
			return instance -> new PrePostInfoFragment(ifFactory.apply(instance), thenFactory.apply(instance));
		}
		else {
			return instance -> new CustomInfoFragment(replacePlaceholders(format, instance));
		}
	}
	
	private Function<TemplateInstance, Statement> statementFactory(String format, boolean isCondition) {
		if (THREEPART_PATTERN.matcher(format).matches()) {
			String[] split = format.substring(1, format.length() - 1).split("\\]\\[", -1);
			assert split.length == 3;
			return instance -> new ThreePartStatement(replacePlaceholders(split[0], instance),
					replacePlaceholders(split[1], instance),
					replacePlaceholders(split[2], instance),
					isCondition);
		}
		else {
			return instance -> new CustomStatement(replacePlaceholders(format, instance));
		}
	}
	
	private String replacePlaceholders(String baseString, TemplateInstance instance) {
		StringBuilder builder = new StringBuilder();
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(baseString);
		while (matcher.find()) {
			String placeholder = matcher.group();
			if (!instance.containsPlaceholder(placeholder)) {
				throw new IllegalArgumentException(
						"Invalid placeholder name '" + placeholder + "' for template " + instance.getName());
			}
			PlaceholderValue value = instance.getPlaceholderValue(placeholder);
			String replacement;
			if (value.isList()) {
				replacement = value.getValueAsList().toString();
			}
			else {
				replacement = value.getValue();
			}
			matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(builder);
		return builder.toString();
	}
	
	public InfoFragment create(TemplateInstance instance) {
		return factory.apply(instance);
	}
}
