package detectapp;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import detectapp.model.*;
import detectapp.testsmells.*;
import detectapp.utils.Config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class TestSmellDetector {

    private List<TestSmell> testSmells;

    public TestSmellDetector() { }

    private void initSmells(String productionFilePath){
        testSmells = new ArrayList<>();
        for (String smellType : Config.getSmellsTypes()) {
            switch (smellType.toLowerCase()){
                case "complexity" :
                    testSmells.add(new Complexity());
                    break;
                case "duplicatedassertion":
                    testSmells.add(new AssertionRoulette());
                    break;
                case "eagertest" :
                    if (productionFilePath != null)
                        testSmells.add(new EagerTest(productionFilePath));
                    break;
                case "emptytest" :
                    testSmells.add(new EmptyTest());
                    break;
                case "exceptioncatchingthrowingtest" :
                    testSmells.add(new ExceptionCatchingThrowingTest());
                    break;
                case "mysteryguest" :
                    testSmells.add(new MysteryGuest());
                    break;
                case "objectcreationoutsidesetup" :
                    testSmells.add(new ObjectCreationOutsideSetUp());
                    break;
                case "resourceoptimism" :
                    testSmells.add(new RescourceOptimism());
                    break;
                case "sleepytest" :
                    testSmells.add(new SleepyTest());
                    break;
                case "missingassertion" :
                    testSmells.add(new MissingAssertion());
                    break;
            }
        }
    }

    public TestFile detectSmells(TestFile testFile, TestProductionFile testProductionFile) throws FileNotFoundException{
        initSmells(testProductionFile.getFilePath());
        //initSmells(null);
        FileInputStream fis = new FileInputStream(testFile.getFilePath());
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = null;
        try {
            cu = javaParser.parse(fis);
        } catch (Exception e) {
            System.out.println(testFile.getFileName());
        }

        for (TestSmell testSmell : testSmells) {
            try {
                cu.accept(testSmell, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            testFile.addTestSmell(testSmell);
        }
        return testFile;
    }
}
