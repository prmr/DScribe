package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.List;
import com.github.javaparser.JavaParser;

public interface DScribeClass {
  public void parseCompilationUnit(JavaParser parser);

  public boolean writeToFile(List<Exception> exceptions);
}
