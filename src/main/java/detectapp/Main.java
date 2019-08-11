package detectapp;

import detectapp.model.*;
import detectapp.utils.Config;
import detectapp.utils.OutputParser;

import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException {
        Config.getConfig("src//main//config//config.json");

        List<TestFile> testFiles = new ArrayList<>();
        List<TestProductionFile> testProductionFiles = new ArrayList<>();
        List<TestFile> analyzedFiles = new ArrayList<>();
        TestSmellDetector testSmellDetector = new TestSmellDetector();

        File testFolder = new File(Config.getTestFolderPath());

        if (testFolder.exists() && testFolder.isDirectory()) {
            for (File file : testFolder.listFiles()) {
                if (!file.isDirectory()){
                    testFiles.add(new TestFile(file.getPath()));
                    testProductionFiles.add(new TestProductionFile(getCorrespondingProductionFilePath(file)));
                }
            }
        }
        int count = 0;
        for (TestFile testFile : testFiles) {
            analyzedFiles.add(testSmellDetector.detectSmells(testFile, testProductionFiles.get(count)));
            count += 1;
        }



        System.out.println("end.");
        OutputParser outputParser = new OutputParser(testFiles, Config.getOutputPath(), Type.json);
        outputParser.buildOutputFile();
    }

    private static String getCorrespondingProductionFilePath(File testFile) throws IOException {
        File productionFolder = new File(Config.getProductionFolderPath());
        if (productionFolder.exists() && productionFolder.isDirectory()) {
            for (File productionFile : productionFolder.listFiles()) {
                if (FilenameUtils.removeExtension(testFile.getName()).toLowerCase()
                        .contains(FilenameUtils.removeExtension(productionFile.getName()).toLowerCase()))
                    return productionFile.getPath();
            }

            List<FilePair> filePairs = Config.getFilePairs();
            for (FilePair filePair : filePairs){
                if (filePair.getTestFilePath().toLowerCase().equals(testFile.getName().toLowerCase()))
                    return filePair.getProductionFilePath();
            }
        }
        return null;
    }
}
