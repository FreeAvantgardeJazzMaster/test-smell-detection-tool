package detectapp.utils;

import com.github.javaparser.ast.expr.AnnotationExpr;
import detectapp.model.TestCodeElement;
import detectapp.model.TestFile;
import detectapp.model.TestSmell;
import detectapp.model.FileFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.XML;

public class OutputParser {
    private String outputPath;
    private FileFormat format;
    private List<TestFile> testFiles;

    public OutputParser(List<TestFile> testfiles, String outputPath, FileFormat format) {
        this.outputPath = getFullOutputFilePath(outputPath, format);
        this.format = format;
        this.testFiles = testfiles;
    }

    public void buildOutputFile() {
        JSONObject jsonObject = parseTestFiles(testFiles);
        File file = new File(outputPath);
        try {
            switch (this.format) {
                case json:
                    FileUtils.writeStringToFile(file, jsonObject.toString());
                    break;
                case xml:
                    FileUtils.writeStringToFile(file, XML.toString(jsonObject));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject parseTestFiles(List<TestFile> testFiles) {
        JSONObject testFilesJsonObject = new JSONObject();
        JSONArray testFilesJsonArray = new JSONArray();
        for (TestFile testFile : testFiles) {
            //testFile = filterTestFile(testFile);
            JSONObject testCodeElementsJsonObject = new JSONObject();
            JSONArray testCodeElementsJsonArray = new JSONArray();

            Map<String, List<TestSmell>> testCodeElementListMap = new HashMap<>();
            Map<String, List<AnnotationExpr>> annotations = new HashMap<>();
            Map<String, Integer> statementsCounts = new HashMap<>();
            Map<String, Integer> locs = new HashMap<>();

            for (TestSmell testSmell : testFile.getTestSmells()) {
                for (TestCodeElement testCodeElement : testSmell.getTestCodeElements()) {
                    if (testCodeElementListMap.containsKey(testCodeElement.getName())) {
                        if (testCodeElement.isSmell())
                            testCodeElementListMap.get(testCodeElement.getName()).add(testSmell);
                    } else {
                        if (testCodeElement.isSmell())
                            testCodeElementListMap.put(testCodeElement.getName(), new ArrayList<>(Arrays.asList(testSmell)));
                        else
                            testCodeElementListMap.put(testCodeElement.getName(), new ArrayList<>());
                    }

                    if (!annotations.containsKey(testCodeElement.getName())) {
                        if (testCodeElement.getAnnotations() != null)
                            annotations.put(testCodeElement.getName(), testCodeElement.getAnnotations());
                        else
                            annotations.put(testCodeElement.getName(), new ArrayList<>());
                    }

                    if (!statementsCounts.containsKey(testCodeElement.getName())) {
                        statementsCounts.put(testCodeElement.getName(), testCodeElement.getStatementsCount());
                    }

                    if (!locs.containsKey(testCodeElement.getName())) {
                        locs.put(testCodeElement.getName(), testCodeElement.getLoc());
                    }
                }
            }

            for (Map.Entry<String, List<TestSmell>> entry : testCodeElementListMap.entrySet()) {
                JSONObject testCodeElementJO = new JSONObject();
                testCodeElementJO.put("name", entry.getKey());
                testCodeElementJO.put("statements", statementsCounts.get(entry.getKey()));
                testCodeElementJO.put("loc", locs.get(entry.getKey()));

                JSONArray ja = new JSONArray();
                for (TestSmell testSmell : entry.getValue())
                    ja.put(testSmell.getName());
                testCodeElementJO.put("test-smells", ja);

                ja = new JSONArray();
                for (AnnotationExpr annotationExpr : annotations.get(entry.getKey()))
                    ja.put(annotationExpr.getName());
                testCodeElementJO.put("annotations", ja);

                if (annotations.get(entry.getKey()).stream().filter(a -> a.getName().toString().equals("Test")).count() != 0)
                    testCodeElementsJsonArray.put(testCodeElementJO);

            }
            testCodeElementsJsonObject.put("name", testFile.getFileName());
            testCodeElementsJsonObject.put("test-methods", testCodeElementsJsonArray);
            testFilesJsonArray.put(testCodeElementsJsonObject);
        }
        testFilesJsonObject.put("test-files", testFilesJsonArray);
        return testFilesJsonObject;
    }

    private TestFile filterTestFile(TestFile testFile) {
        for (TestSmell testSmell : testFile.getTestSmells()) {
            testSmell.getTestCodeElements().removeIf(e -> !e.isSmell());
        }
        return testFile;
    }

    private String getFullOutputFilePath(String outputPath, FileFormat fileFormat) {
        return outputPath + "//result." + (fileFormat == FileFormat.xml ? "xml" : "json");
    }


}