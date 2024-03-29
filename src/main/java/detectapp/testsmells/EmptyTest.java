package detectapp.testsmells;

import com.github.javaparser.ast.body.MethodDeclaration;
import detectapp.model.TestCodeElement;
import detectapp.model.TestMethod;
import detectapp.model.TestSmell;

import java.util.ArrayList;
import java.util.List;

public class EmptyTest extends TestSmell {

    private String name = "Empty Test";
    private List<TestCodeElement> testCodeElements;

    public EmptyTest() {
        this.testCodeElements = new ArrayList<>();
    }

    @Override
    public List<TestCodeElement> getTestCodeElements() {
        return testCodeElements;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void visit(MethodDeclaration method, Void arg) {
        TestMethod testMethod = new TestMethod(method.getNameAsString());
        testMethod.setAnnotations(method.getAnnotations());
        testMethod.setStatementsCount(method.getBody().isPresent() ? method.getBody().get().getStatements().size() : 0);
        testMethod.setLoc(calcLoc(method));
        if (!method.isAbstract()) {
            if (method.getBody().isPresent()) {
                if (method.getBody().get().getStatements().size() == 0)
                    testMethod.setSmell(true);
            }
        }
        testCodeElements.add(testMethod);
    }
}
