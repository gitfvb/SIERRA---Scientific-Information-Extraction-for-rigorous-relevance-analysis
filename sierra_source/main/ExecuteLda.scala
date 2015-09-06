package main
import edu.stanford.nlp.tmt.model.DirichletParams.fromDouble
import edu.stanford.nlp.tmt.model.lda.LDADataset
import edu.stanford.nlp.tmt.model.lda.LDAModelParams
import edu.stanford.nlp.tmt.stage.EstimatePerWordTopicDistributions
import edu.stanford.nlp.tmt.stage.InferCVB0DocumentTopicDistributions
import edu.stanford.nlp.tmt.stage.InferGibbsDocumentTopicDistributions
import edu.stanford.nlp.tmt.stage.LoadCVB0LDA
import edu.stanford.nlp.tmt.stage.LoadGibbsLDA
import edu.stanford.nlp.tmt.stage.LoadLDADocumentTopicDistributions
import edu.stanford.nlp.tmt.stage.InferCVB0DocumentTopicDistributions
import edu.stanford.nlp.tmt.stage.InferGibbsDocumentTopicDistributions
import edu.stanford.nlp.tmt.stage.QueryTopTerms
import edu.stanford.nlp.tmt.stage.QueryTopicUsage
import edu.stanford.nlp.tmt.stage.TrainCVB0LDA
import edu.stanford.nlp.tmt.stage.TrainGibbsLDA
import scalanlp.io.CSVFile.CSVFileAsParcel
import scalanlp.io.CSVFile
import scalanlp.pipes.Pipes.global.file
import scalanlp.stage.Parcel.data
import scalanlp.stage.text.DocumentMinimumLengthFilter
import scalanlp.stage.text.TermCounter
import scalanlp.stage.text.TermDynamicStopListFilter
import scalanlp.stage.text.TermMinimumDocumentCountFilter
import scalanlp.stage.text.TermStopList
import scalanlp.stage.text.TokenizeWith
import scalanlp.stage.Column
import scalanlp.stage.IDColumn
import scalanlp.text.tokenize.CaseFolder
import scalanlp.text.tokenize.MinimumLengthFilter
import scalanlp.text.tokenize.SimpleEnglishTokenizer
import scalanlp.text.tokenize.WordsAndNumbersOnlyFilter
import utilities.StringUtilities
import scala.io.Source
import scalanlp.text.tokenize.PorterStemmer
import scalanlp.stage.Take
import scalanlp.stage.Drop
import scalanlp.stage.text.TermStopListFilter
import scalanlp.text.tokenize.StopWordFilter
import scalanlp.stage.Columns



object ExecuteLda {


	
	def prepareData(directory: String, dataColumn: Integer, stoplist: java.util.List[String], language: String) = {
	  
		var stopwords: List[String] = List();
		val it = stoplist.iterator();
		while (it.hasNext()) {
		  stopwords = it.next() +: stopwords;
		}
		
	  
		// now load the data
		val source = CSVFile(directory + "converted_file.csv") ~> IDColumn(2) ~> Drop(1);
		
		val tokenizer = if (language == "en") {
				SimpleEnglishTokenizer() ~>            // tokenize on space and punctuation
				CaseFolder() ~>                        // lowercase everything
				WordsAndNumbersOnlyFilter() ~>         // ignore non-words and non-numbers
				PorterStemmer() ~>					   // stemming
				MinimumLengthFilter(3) ~>              // take terms with >=3 characters
				StopWordFilter(language) 				   // remove stopwords (only integrated with english)
		} else {
				SimpleEnglishTokenizer() ~>            // tokenize on space and punctuation
				CaseFolder() ~>                        // lowercase everything
				WordsAndNumbersOnlyFilter() ~>         // ignore non-words and non-numbers
				PorterStemmer() ~>					   // stemming
				MinimumLengthFilter(3)              // take terms with >=3 characters
		}
		
		var text = {
				source ~>                              // read from the source file
				Column(dataColumn) ~>                  // select column containing text
				TokenizeWith(tokenizer) ~>             // tokenize with tokenizer above
				TermStopListFilter(stopwords) ~>		   // remove words from the stoplist
				TermCounter() ~>                       // collect counts (needed below)
				TermMinimumDocumentCountFilter(4) ~>   // filter terms in <4 docs
				TermDynamicStopListFilter(0) ~>       // filter out 30 most common terms
				DocumentMinimumLengthFilter(5)         // take only docs with >=5 terms
		}
		
		text
	}
  
	def ldaLearnModelOnData(directory: String, algo: String, numberOfTopics: Integer, topicSmoothing: Double, termSmoothing: Double, dataColumn: Integer, numberOfmaxIterations: Integer, stopwordsList: java.util.List[String], language: String) {
	  
	  val filename = "converted_file.csv";
	  
	  // now load the data
		val source = CSVFile(directory + filename) ~> IDColumn(2);
		var text = prepareData(directory, dataColumn, stopwordsList, language);		
	  
	  // turn the text into a dataset ready to be used with LDA
		var dataset = LDADataset(text);

		// define the model parameters
		val params = LDAModelParams(numTopics = numberOfTopics, dataset = dataset,
				topicSmoothing = topicSmoothing, termSmoothing = termSmoothing);

		// Name of the output model folder to generate
		val modelPath = file(directory + "lda-"+dataset.signature+"-"+params.signature);

		// execute the algorithm
		if(algo == "CVB0LDA") {

			// Trains the model: the model (and intermediate models) are written to the
			// output folder.  If a partially trained model with the same dataset and
			// parameters exists in that folder, training will be resumed.
			TrainCVB0LDA(params, dataset, output=modelPath, maxIterations=numberOfmaxIterations); 
		} else if (algo == "Gibbs") {
			// To use the Gibbs sampler for inference, instead use
			TrainGibbsLDA(params, dataset, output=modelPath, maxIterations=numberOfmaxIterations); 
		}
	}
	
	
	
	
	def ldaExecuteModelOnData(directory: String, algo: String, dataColumn: Integer, modelpath: String, stopwordsList: java.util.List[String], language: String) {

		val filename = "converted_file.csv";
		val numberOfTopTerms = 50;

	  // now load the data
		val source = CSVFile(directory + filename) ~> IDColumn(2);
		var text = prepareData(directory, dataColumn, stopwordsList, language);	


		// Name of the output model folder to generate
		//		val modelPath = file(directory + "lda-"+dataset.signature+"-"+params.signature);
		val modelPath = file(modelpath);

		println("Loading "+modelPath);
		val model = if(algo == "CVB0LDA")
			LoadCVB0LDA(modelPath);
		else // then it must be gibbs
			LoadGibbsLDA(modelPath);

		// Base name of output files to generate
		val output = file(directory, source.meta[java.io.File].getName.replaceAll(".csv",""));

		// turn the text into a dataset ready to be used with LDA
		val dataset = LDADataset(text, termIndex = model.termIndex);

		
		val perDocTopicDistributions = if(algo == "CVB0LDA")
			InferCVB0DocumentTopicDistributions(LoadCVB0LDA(modelPath), dataset);
		else // then it must be gibbs
			InferGibbsDocumentTopicDistributions(LoadGibbsLDA(modelPath), dataset);

		println("Writing topic usage to "+output+"-usage.csv");
		val usage = QueryTopicUsage(model, dataset, perDocTopicDistributions);
		CSVFile(output+"-usage.csv").write(usage);
		
		println("Writing document distributions to "+output+"-document-topic-distributions.csv");
		CSVFile(output+"-document-topic-distributions.csv").write(perDocTopicDistributions);

		println("Estimating per-doc per-word topic distributions");
		val perDocWordTopicDistributions = EstimatePerWordTopicDistributions(
				model, dataset, perDocTopicDistributions);

		println("Writing top terms to "+output+"-top-terms.csv");
		val topTerms = QueryTopTerms(model, dataset, perDocWordTopicDistributions, numTopTerms=numberOfTopTerms);
		CSVFile(output+"-top-terms.csv").write(topTerms);

	}
	
	def sliceData(directory: String, dataColumn: Integer, sliceColumn: Int, stopwordsList: java.util.List[String], modelpath: String, algo: String, language: String) {

//		val algo = "CVB0LDA";
	    val filename = "converted_file.csv";
		val numberOfTopTerms = 50;
		

		val documentTopicDistribution = "document-topic-distributions.csv";
		

		// now load the data
		val source = CSVFile(directory + filename) ~> IDColumn(2);
		var text = prepareData(directory, dataColumn, stopwordsList, language);	
		
		// Name of the output model folder to generate
		//		val modelPath = file(directory + "lda-"+dataset.signature+"-"+params.signature);
		val modelPath = file(modelpath);


		println("Loading "+modelPath);
		val model = if(algo == "CVB0LDA")
			LoadCVB0LDA(modelPath);
		else // then it must be gibbs
			LoadGibbsLDA(modelPath);
		

		// turn the text into a dataset ready to be used with LDA
		val dataset = LDADataset(text, termIndex = model.termIndex);

		// define fields from the dataset we are going to slice against
		val slice = source ~> Column(sliceColumn); 
		
		// Base name of output files to generate
		val output = file(directory, source.meta[java.io.File].getName.replaceAll(".csv",""));

		println("Loading document distributions");
		val perDocTopicDistributionsSliced = LoadLDADocumentTopicDistributions(
				CSVFile(modelPath,documentTopicDistribution));
//		val perDocTopicDistributionsSliced = InferCVB0DocumentTopicDistributions(model, dataset);
//		val perDocTopicDistributionsSliced = InferGibbsDocumentTopicDistributions(model, dataset);
		// This could be InferDocumentTopicDistributions(model, dataset)
		// for a new inference dataset.  Here we load the training output.

		println("Writing topic usage to "+output+"-sliced-usage.csv");
		val usageSliced = QueryTopicUsage(model, dataset, perDocTopicDistributionsSliced, grouping=slice);
		CSVFile(output+"-sliced-usage.csv").write(usageSliced);

		
		println("Estimating per-doc per-word topic distributions");
		val perDocWordTopicDistributionsSliced = EstimatePerWordTopicDistributions(model, dataset, perDocTopicDistributionsSliced);
		println("Writing top terms to "+output+"-sliced-top-terms.csv");
		val topTermsSliced = QueryTopTerms(model, dataset, perDocWordTopicDistributionsSliced, numTopTerms=numberOfTopTerms, grouping=slice);
		CSVFile(output+"-sliced-top-terms.csv").write(topTermsSliced);

	}
	
	def calculateNumberOfTopics(directory: String, numberOfTopics: Array[Int], dataColumn: Integer, numberOfmaxIterations: Integer, numberOfTrainingData: Double, numberOfTopicSmoothing: Double, numberOfTermSmoothing: Double, algo: String, stopwordsList: java.util.List[String], language: String) {
	  
	  val filename = "converted_file.csv";
	  
		//***********************
		// EXAMPLE 5 - Selecting model parameters
		//***********************

	  	// now load the data
		val source = CSVFile(directory + filename) ~> IDColumn(2);
		var text = prepareData(directory, dataColumn, stopwordsList, language);
	  
		//set aside 80 percent of the input text as training data ...
		val numTrain: Int = text.data.size * (numberOfTrainingData * 10).intValue() / 10;

		// build a training dataset
		val training = LDADataset(text ~> Take(numTrain));

		// build a test dataset, using term index from the training dataset 
		val testing  = LDADataset(text ~> Drop(numTrain));

		// a list of pairs of (number of topics, perplexity)
		var scores = List.empty[(Int,Double)];

		
		// loop over various numbers of topics, training and evaluating each model
		for (numTopics <- numberOfTopics) {
			val params = LDAModelParams(numTopics = numTopics, dataset = training, topicSmoothing = numberOfTopicSmoothing, termSmoothing = numberOfTermSmoothing);
			val output = file("lda-"+training.signature+"-"+params.signature);
			
			// execute the algorithm
			if(algo == "CVB0LDA") {

				// Trains the model: the model (and intermediate models) are written to the
				// output folder.  If a partially trained model with the same dataset and
				// parameters exists in that folder, training will be resumed.
				val model = TrainCVB0LDA(params, training, output=null, maxIterations=numberOfmaxIterations);
				println("[perplexity] computing at "+numTopics);
				val perplexity = model.computePerplexity(testing);
//				println("[perplexity] perplexity at "+numTopics+" topics: "+perplexity);
				scores :+= (numTopics, perplexity);
			} else if (algo == "Gibbs") {
				
			  // To use the Gibbs sampler for inference, instead use
				val model = TrainGibbsLDA(params, training, output=null, maxIterations=numberOfmaxIterations);
				println("[perplexity] computing at "+numTopics);
				val perplexity = model.computePerplexity(testing);
//				println("[perplexity] perplexity at "+numTopics+" topics: "+perplexity);
				scores :+= (numTopics, perplexity);
			}
			
			// output every iteration
			for ((numTopics,perplexity) <- scores) {
				println("[perplexity] perplexity at "+numTopics+" topics: "+perplexity);
			}
			
		}
		
		println("---------------------------------------------");
		println("Results of all iterations:");
		for ((numTopics,perplexity) <- scores) {
			println("[perplexity] perplexity at "+numTopics+" topics: "+perplexity);
		}

	}
}