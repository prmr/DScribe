///*******************************************************************************
// * Copyright 2020 McGill University
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *******************************************************************************/
//package ca.mcgill.cs.swevo.dscribe.generation.doc;
//
//import static java.nio.charset.StandardCharsets.UTF_8;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import com.github.javaparser.JavaParser;
//import com.github.javaparser.ParseResult;
//import com.github.javaparser.ParserConfiguration;
//import com.github.javaparser.ParserConfiguration.LanguageLevel;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.BodyDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.body.TypeDeclaration;
//import com.github.javaparser.javadoc.Javadoc;
//import com.github.javaparser.javadoc.JavadocBlockTag;
//import com.github.javaparser.javadoc.description.JavadocDescription;
//import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
//import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
//import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
//import com.github.javaparser.symbolsolver.JavaSymbolSolver;
//import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
//
//import ca.mcgill.cs.swevo.dscribe.Context;
//import ca.mcgill.cs.swevo.dscribe.generation.Generator;
//import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
//import ca.mcgill.cs.swevo.dscribe.template.Template;
//import ca.mcgill.cs.swevo.dscribe.utils.TypeNameResolver;
//import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException;
//import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException.GenerationError;
//
//public class DocGenerator extends Generator
//{
//	private final JavaParser parser;
//
//	private Map<String, Set<InfoFragment>> fragments;
//	private Map<Path, CompilationUnit> asts;
//	private List<Path> srcRoots;
//
//	public DocGenerator()
//	{
//		ParserConfiguration config = new ParserConfiguration();
//		config.setLanguageLevel(LanguageLevel.CURRENT);
//		// TODO detect tab size? still issues
//		config.setTabSize(4);
//		config.setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver(false)));
//		parser = new JavaParser(config);
//	}
//
//	@Override
//	public List<Path> output()
//	{
//		return List.copyOf(asts.keySet());
//	}
//
//	@Override
//	public void prepare(Context context)
//	{
//		super.prepare(context);
//		srcRoots = context.srcPaths();
//		asts = new HashMap<>();
//		fragments = new HashMap<>();
//	}
//
//	@Override
//	protected void addInvocation(TemplateInstance instance, Template template)
//	{
//		String focus = extractFocus(instance);
//		Optional<DocumentationFactory> factory = template.getDocFactory();
//		if (factory.isPresent())
//		{
//			InfoFragment fragment = factory.get().create(instance);
//			fragments.computeIfAbsent(focus, f -> new HashSet<>()).add(fragment);
//		}
//	}
//
//	private String extractFocus(TemplateInstance instance)
//	{
//		String focus = instance.getPlaceholderValue("$package$").getValue() + "."
//				+ instance.getPlaceholderValue("$class$").getValue() + "."
//				+ instance.getPlaceholderValue("$method$").getValue() + "(" + instance
//						.getPlaceholderValue("$paramtypes$").getValueAsList().stream().collect(Collectors.joining(", "))
//				+ ")";
//		return focus;
//	}
//
//	@Override
//	public List<Exception> generate()
//	{
//		List<Exception> errors = new ArrayList<>();
//		for (Entry<String, Set<InfoFragment>> fragmentSet : fragments.entrySet())
//		{
//			try
//			{
//				String key = fragmentSet.getKey();
//				MethodDeclaration method = getMethod(key);
//				Set<InfoFragment> combinedFragments = InfoFragment.combine(fragmentSet.getValue());
//				Set<String> dscribeFragments = combinedFragments.stream().map(x -> x.print())
//						.collect(Collectors.toSet());
//				addDscribeFragments(method, dscribeFragments);
//			}
//			catch (Exception e)
//			{
//				errors.add(e);
//			}
//		}
//		for (Entry<Path, CompilationUnit> file : asts.entrySet())
//		{
//			rewriteJavaFile(file.getKey(), file.getValue());
//		}
//		return errors;
//	}
//
//	private MethodDeclaration getMethod(String key)
//	{
//		assert key.matches("(\\w*\\.)+\\w+\\(([\\w\\.]+(, )?)+\\)");
//		// i.e., alphanum tokens separated by dots, then a list of parameters inside 1 pair of parentheses
//		List<String> location = List.of(key.replaceAll("\\(.*\\)", "").split("\\."));
//		String[] parameters = key.replaceFirst(".*\\(", "").replace(")", "").split(", ");
//		Entry<Path, Integer> source = findSource(location);
//		try
//		{
//			CompilationUnit ast = asts.computeIfAbsent(source.getKey(), this::parseCompilationUnit);
//			String methodName = location.get(location.size() - 1);
//			location = location.subList(source.getValue(), location.size() - 1);
//			MethodDeclaration method = locateMethod(ast, location, methodName, parameters);
//			return method;
//		}
//		catch (IndexOutOfBoundsException e)
//		{
//			throw new GenerationException(GenerationError.INVALID_SOURCE_FILE, e);
//		}
//	}
//
//	private Entry<Path, Integer> findSource(List<String> location)
//	{
//		for (Path srcRoot : srcRoots)
//		{
//			Entry<Path, Integer> source = tryFindSourceFile(location, srcRoot);
//			if (source != null)
//			{
//				return source;
//			}
//		}
//		throw new GenerationException(GenerationError.MISSING_SOURCE_FILE);
//	}
//
//	private Entry<Path, Integer> tryFindSourceFile(List<String> location, Path srcRoot)
//	{
//		Path path = srcRoot;
//		int i = 0;
//		for (String file : location)
//		{
//			i++;
//			if (file.isBlank())
//			{
//				continue;
//			}
//			Path javaFile = path.resolve(file + ".java");
//			if (javaFile.toFile().exists())
//			{
//				return Map.entry(javaFile, i);
//			}
//			Path subfolder = path.resolve(file);
//			if (subfolder.toFile().exists() && subfolder.toFile().isDirectory())
//			{
//				path = subfolder;
//			}
//			else
//			{
//				return null;
//			}
//		}
//		return null;
//	}
//
//	private CompilationUnit parseCompilationUnit(Path path)
//	{
//		try
//		{
//			ParseResult<CompilationUnit> parseResult = parser.parse(path);
//			if (!parseResult.isSuccessful())
//			{
//				throw new GenerationException(GenerationError.INVALID_SOURCE_FILE);
//			}
//			CompilationUnit compilationUnit = parseResult.getResult().get();
//			LexicalPreservingPrinter.setup(compilationUnit);
//			return compilationUnit;
//		}
//		catch (IOException e)
//		{
//			throw new GenerationException(GenerationError.IO_ERROR, e);
//		}
//	}
//
//	private MethodDeclaration locateMethod(CompilationUnit root, List<String> location, String methodName,
//			String[] methodParameters)
//	{
//		TypeDeclaration<?> type = root.getType(0);
//		for (String nestedType : location)
//		{
//			type = searchMembers(type, nestedType);
//		}
//		for (MethodDeclaration method : type.getMethods())
//		{
//			if (matchSignature(method, methodName, methodParameters))
//			{
//				return method;
//			}
//		}
//		throw new GenerationException(GenerationError.INVALID_SOURCE_FILE);
//	}
//
//	private TypeDeclaration<?> searchMembers(TypeDeclaration<?> parent, String target)
//	{
//		for (BodyDeclaration<?> nested : parent.getMembers())
//		{
//			if (nested.isTypeDeclaration() && nested.asTypeDeclaration().getNameAsString().equals(target))
//			{
//				return nested.asTypeDeclaration();
//			}
//		}
//		throw new GenerationException(GenerationError.INVALID_SOURCE_FILE);
//	}
//
//	private boolean matchSignature(MethodDeclaration method, String methodName, String[] paramTypes)
//	{
//		if (!method.getNameAsString().equals(methodName))
//		{
//			return false;
//		}
//		ResolvedMethodDeclaration resolvedMethod = method.resolve();
//		int nbParams = resolvedMethod.getNumberOfParams();
//		if (paramTypes.length != nbParams)
//		{
//			return false;
//		}
//		for (int i = 0; i < nbParams; i++)
//		{
//			ResolvedParameterDeclaration param = resolvedMethod.getParam(i);
//			String typeString = TypeNameResolver.canonicalName(param.getType());
//			if (!typeString.equals(paramTypes[i]))
//			{
//				return false;
//			}
//		}
//		return true;
//	}
//
//	private void addDscribeFragments(MethodDeclaration method, Set<String> dscribeFragments)
//	{
//		Javadoc javadoc = method.getJavadoc().orElseGet(() -> new Javadoc(new JavadocDescription()));
//		for (Iterator<JavadocBlockTag> iter = javadoc.getBlockTags().iterator(); iter.hasNext();)
//		{
//			JavadocBlockTag tag = iter.next();
//			if (tag.getTagName().equals("dscribe"))
//			{
//				iter.remove();
//			}
//		}
//		for (String fragment : dscribeFragments)
//		{
//			javadoc.addBlockTag("dscribe", fragment);
//		}
//		method.setJavadocComment(javadoc);
//		// TODO control indentation
//	}
//
//	private void rewriteJavaFile(Path file, CompilationUnit ast)
//	{
//		try (BufferedWriter out = Files.newBufferedWriter(file, UTF_8))
//		{
//			LexicalPreservingPrinter.print(ast, out);
//		}
//		catch (IOException e)
//		{
//			throw new GenerationException(GenerationError.IO_ERROR);
//		}
//	}
//}
