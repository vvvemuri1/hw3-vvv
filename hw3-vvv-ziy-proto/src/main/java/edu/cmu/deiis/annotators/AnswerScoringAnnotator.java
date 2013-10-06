package edu.cmu.deiis.annotators;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.cleartk.token.type.Token;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;

/**
 * The system will incorporate a component that will assign an answer 
 * score annotation to each answer. The answer score annotation will
 * record the score assigned to the answer.
 * @author Vinay Vyas Vemuri <vvv@andrew.cmu.edu>
 */
public class AnswerScoringAnnotator extends JCasAnnotator_ImplBase 
{
  /**
   * Assigns a score to each answer using Standord NLP Token annotations.
   * @param jcas JCas object that provides access to the CAS.
   */
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException 
  {
	  FSIndex answerIndex = jcas.getAnnotationIndex(Answer.type);
	  Iterator answerIter = answerIndex.iterator();
	  
	  ArrayList<Answer> answers = new ArrayList<Answer>();
	  while(answerIter.hasNext())
	  {
		  answers.add((Answer) answerIter.next());
	  }
	  
	  JFSIndexRepository repository = jcas.getJFSIndexRepository();
	  FSIterator tokenIter = repository.getAllIndexedFS(Token.type);
	  
	  // Setup
	  ArrayList<String> question = new ArrayList<String>();
	  ArrayList<String> questionPOS = new ArrayList<String>();
	  ArrayList<ArrayList<String>> answerList = new  ArrayList<ArrayList<String>>();
	  ArrayList<ArrayList<String>> answersPOS = new  ArrayList<ArrayList<String>>();
	  ArrayList<ArrayList<Integer>> tokenBegins = new  ArrayList<ArrayList<Integer>>();
	  
	  ArrayList<AnswerScore> answersScores = new  ArrayList<AnswerScore>();
	  
	  boolean isAnswer = false;
	  int answerId = -1;
	  
	  while (tokenIter.hasNext())
	  {
		Token token = (Token) tokenIter.next();
		
		if (token.getCoveredText().equals("Q"))
		{
			isAnswer = false;
			continue;
		}
		else if (token.getCoveredText().equals("A"))
		{
			isAnswer = true;
			answerId++;
			
			answerList.add(new ArrayList<String>());
			answersPOS.add(new ArrayList<String>());
			tokenBegins.add(new ArrayList<Integer>());
						
			AnswerScore score = new AnswerScore(jcas);
			score.setScore(0);
			score.setConfidence(1.0);
			answersScores.add(score);
			
			continue;
		}
		else if (token.getCoveredText().equals("0") || token.getCoveredText().equals("1"))
		{
			continue;
		}
		
		if (isAnswer)
		{
			answerList.get(answerId).add(token.getCoveredText());
			answersPOS.get(answerId).add((token.getPos()));
			tokenBegins.get(answerId).add((token.getBegin()));
		}
		else
		{
			question.add(token.getCoveredText());
			questionPOS.add(token.getPos());
		}    
	  }	  

	  // Determine question and answer scores
	  determineScores(question, answerList, answersScores);        

	  matchAnswerToAnswerScore(answers, tokenBegins, answersScores);
  }
  
  	/**
  	 * Determine the scores of all the answers corresponding to a question.
  	 * @param question The Question for which we are attempting to score answers
  	 * @param answerList List of answers being scored.
  	 * @param answersScores List that will contain the fully populated answer scores.
  	 */
	private void determineScores(ArrayList<String> question,
			ArrayList<ArrayList<String>> answerList,
			ArrayList<AnswerScore> answersScores) 
	{
		System.out.println("Size = " + answerList.size());
		for (int i = 0; i < answerList.size(); i++)
		{
		  ArrayList<Boolean> alreadySeenToken = new ArrayList<Boolean>(answerList.get(i).size());
		  for (int k = 0; k < answerList.get(i).size(); k++)
		  {
			  alreadySeenToken.add(false);
		  }
		    
		  for (int j = 0; j < question.size(); j++)
		  {
			  for (int k = 0; k < answerList.get(i).size(); k++)
			  {
				  if (answerList.get(i).get(k).equals(question.get(j)))
				  {
					  if (k < alreadySeenToken.size())
					  {
						  alreadySeenToken.set(k, true);
						  answersScores.get(i).setScore(answersScores.get(i).getScore() + 1);
					  }
				  }
			  }    		  
		  }
		       	  
		  answersScores.get(i).addToIndexes();
		}
	}

	/**
	 * Matches an AnswerScore object to the corresponding answer.
	 * @param answers Array of answers
	 * @param tokenBegins List of beginnings of all tokens across all answers.
	 * @param answersScores List of answer scores.
	 */
	private void matchAnswerToAnswerScore(ArrayList<Answer> answers,
			ArrayList<ArrayList<Integer>> tokenBegins,
			ArrayList<AnswerScore> answersScores) 
	{
		for (int i = 0; i < answers.size(); i++)
		  {
			  for (int j = 0; j < tokenBegins.size(); j++)
			  {
				  if (answers.get(i).getBegin() == (tokenBegins.get(j).get(0)))
				  {
					  answersScores.get(j).setAnswer(answers.get(i));
					  answersScores.get(j).setBegin(answers.get(i).getBegin());
					  answersScores.get(j).setEnd(answers.get(i).getEnd());
					  answersScores.get(j).setConfidence(1.0);
				  }
			  }
		  }
	}    
}