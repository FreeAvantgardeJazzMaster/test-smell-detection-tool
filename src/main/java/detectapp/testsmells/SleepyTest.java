package detectapp.testsmells;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import detectapp.model.TestCodeElement;
import detectapp.model.TestMethod;
import detectapp.model.TestSmell;

import java.util.ArrayList;
import java.util.List;

public class SleepyTest extends TestSmell {

    private String name = "Sleepy Test";
    private List<TestCodeElement> testCodeElements;

    private boolean isHasSmell() {
        return hasSmell;
    }

    private void setHasSmell(boolean hasSmell) {
        this.hasSmell = hasSmell;
    }

    private boolean hasSmell = false;

    public SleepyTest() {
        this.testCodeElements = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TestCodeElement> getTestCodeElements() {
        return testCodeElements;
    }

    @Override
    public void visit(MethodDeclaration method, Void arg) {
        TestMethod testMethod = new TestMethod(method.getNameAsString());
        testMethod.setAnnotations(method.getAnnotations());
        testMethod.setStatementsCount(method.getBody().isPresent() ? method.getBody().get().getStatements().size() : 0);
        testMethod.setLoc(calcLoc(method));
        super.visit(method, arg);

        if (isHasSmell()) {
            testMethod.setSmell(true);
        }

        testCodeElements.add(testMethod);
        setHasSmell(false);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        if (n.getNameAsString().equals("sleep")) {
            if (n.getScope().isPresent()) {
                if (n.getScope().get() instanceof NameExpr) {
                    if (((NameExpr) n.getScope().get()).getNameAsString().equals("Thread")) {
                        setHasSmell(true);
                    }
                }
            }
        }
    }
}
