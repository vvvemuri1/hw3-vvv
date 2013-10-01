package edu.cmu.deiis.casConsumers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Evaluation;

/**
 * The system will sort the answers according to their scores, and calculate 
 * precision at N (where N is the total number of correct answers).  
 * @author Vinay Vyas Vemuri <vvv@andrew.cmu.edu>
 */
public class EvaluationCasConsumer extends CasConsumer_ImplBase implements CasConsumer {

	private static final String PARAM_OUTPUTDIR = "OutputDirectory";
	
	public void initialize() throws ResourceInitializationException 
	{
		File mOutputDir = new File((String) getConfigParameterValue(PARAM_OUTPUTDIR));
		if (!mOutputDir.exists()) 
		{
			mOutputDir.mkdirs();
		}
	}
	
   /**
	* Sorts answers according to their scores and calculates precision.
	* @param aCAS Cas after it has been analyzed by analysis engines.
	*/
	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException 
	{
		JCas jcas;
		try 
		{
			jcas = aCAS.getJCas();
		} 
		catch (CASException e) 
		{
			throw new ResourceProcessException(e);
		}
		
	    FSIndex answerScoreIndex = jcas.getAnnotationIndex(AnswerScore.type);
	    Iterator answerScoreIter = answerScoreIndex.iterator();
	    
	    int answerStart = 0;
	    int answerEnd = 0;
	    
	    int correct = 0;
	    int i = 0;
	    
	    FSArray answerScores = new FSArray(jcas, answerScoreIndex.size());
	    
	    while(answerScoreIter.hasNext())
	    {
	      AnswerScore answerScore = (AnswerScore) answerScoreIter.next();
	      Answer answer = (Answer) answerScore.getAnswer();
	      
	      if (i == 0)
	      {
	        answerStart = answer.getBegin();
	      }
	      else if (i == answerScoreIndex.size() - 1)
	      {
	        answerEnd = answer.getEnd();
	      }
	      
	      answerScores.set(i++, answerScore);
	      if (answer.getIsCorrect())
	      {
	        correct++;
	      }
	    }
	    
	    Evaluation annotation = new Evaluation(jcas);
	    annotation.setBegin(answerStart);
	    annotation.setEnd(answerEnd);
	    annotation.setCasProcessorId(EvaluationCasConsumer.class.getName());
	    annotation.setConfidence(1.0f);
	    
	    // Sort answers
	    FSArray answers = sortAnswers(jcas, answerScoreIndex, answerScores);    
	    annotation.setSortedAnswers(answers);

	    // Compute Precision
	    float precision = ((float)correct)/(answerScores.size());
	    annotation.setPrecision(precision);
	    
	    annotation.addToIndexes();
	    
	    // Output CAS to file
	    FSIterator it = jcas.getAnnotationIndex(SourceDocumentInformation.type)
	    		.iterator();
	    if (it.hasNext())
	    {
	    	SourceDocumentInformation inFileLoc = (SourceDocumentInformation) it.next();
	    	URL fileLocUrI;
	    	
	    	try 
	    	{
				fileLocUrI = new URL(inFileLoc.getUri());
				File inFile = new File(fileLocUrI.getPath());
		    	String outFileName = inFile.getName();
		    	System.out.println(outFileName);
			} 
	    	catch (MalformedURLException e) 
			{
				System.out.println("Recieved Malformed URL " + inFileLoc.getUri());
			}
	    }
	}
	
  /**
   * Sorts Answers based on the answer scores.
   * @param jcas JCas object that provides access to the CAS.
   * @param answerScoreIndex Answer Score Annotation Index 
   * @param answerScores Unsorted array of answer scores
   * @return Sorted array of Answers.
   */
  private FSArray sortAnswers(JCas jcas, FSIndex answerScoreIndex, FSArray answerScores) 
  {  
	  ArrayList<AnswerScore> answerScoreList = new ArrayList(Arrays.asList(answerScores.toArray()));
	  Collections.sort(answerScoreList, 
			  new Comparator()
			  {
				@Override
				public int compare(Object o1, Object o2) 
				{
					AnswerScore as1 = (AnswerScore) o1;
					AnswerScore as2 = (AnswerScore) o2;
					return (int) (as2.getScore() - as1.getScore());
				}  
			  }
	  );
	  
	  FSArray answers = new FSArray(jcas, answerScoreIndex.size());
	  for (int j = 0; j < answerScoreList.size(); j++)
	  {
		  AnswerScore score = (AnswerScore)(answerScoreList.get(j));
		  answers.set(j, score.getAnswer());
	  }

	  return answers;
  }
}
