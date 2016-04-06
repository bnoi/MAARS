import java.io.File;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class findDeadCells {
	public findDeadCells(){
		CSVLoader loader = new CSVLoader();
		File trainingFile = new File("/home/tong/Documents/movies/noted_normAll.csv");
//		File testFile = new File("/home/tong/Documents/movies/testSet1_normAll.csv");
		File testFile = new File("/home/tong/Documents/movies/102/60x/25-03-2/CFP 0 frame fluo measure.xlscsv");		
		try {
			loader.setFile(trainingFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Instances trainingSet = null;
		try {
			trainingSet = loader.getDataSet();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		trainingSet.setClassIndex(5);
		System.out.println(trainingSet.size());
		Classifier eModel = (Classifier) new Logistic();
		try {
			eModel.buildClassifier(trainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Evaluation eTest = null;
		try {
			eTest = new Evaluation(trainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			eTest.evaluateModel(eModel, trainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String strSummary = eTest.toSummaryString();
		System.out.println(strSummary);
		
		// Get the confusion matrix
		double[][] cmMatrix = eTest.confusionMatrix();
		
		try {
			loader.setFile(testFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Instances testSet = null;
		try {
			testSet = loader.getDataSet();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int yesCount = 0;
		int noCount = 0;
		for (Instance i : testSet){
			try {
				double[] res = eModel.distributionForInstance(i);
				if (res[1]>0.9){
					System.out.println("first " + res[0] +" \nsec " + res[1] );
					System.out.println(i.value(0));
					yesCount++;
				}else{
					noCount++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Yes "  + yesCount);
		System.out.println("No " + noCount);
	}
	public static void main(String[] args) {
		new findDeadCells();
	}
}
