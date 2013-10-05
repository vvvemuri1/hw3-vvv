package edu.cmu.deiis.annotators;

import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
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
   * Assigns a score to each answer.
   * @param jcas JCas object that provides access to the CAS.
   */
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException 
  {
    FSIndex answerIndex = jcas.getAnnotationIndex(Answer.type);
    Iterator answerIter = answerIndex.iterator();

    while(answerIter.hasNext())
    {
      AnswerScore answerScore = new AnswerScore(jcas);
      Answer answer = (Answer) answerIter.next();

      answerScore.setCasProcessorId(AnswerScoringAnnotator.class.getName());
      answerScore.setBegin(answer.getBegin());
      answerScore.setEnd(answer.getEnd());
      answerScore.setAnswer(answer);
      
      /**
       * Using Standord NLP to compute score by averaging
       * score across tokens.
       */
      FSIndex tokenIndex = jcas.getAnnotationIndex(Token.type);
      Iterator tokenIter = tokenIndex.iterator();

      double total = 0;
      int numTokens = 0;
      
      while (tokenIter.hasNext())
      {
      	Token token = (Token) tokenIter.next();
      	total += token.getScore();
      	numTokens++;
      }
      
      answerScore.setScore(total/numTokens);
      answerScore.addToIndexes();
    }    
  }
}