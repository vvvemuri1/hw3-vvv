package edu.cmu.deiis.annotators;

import java.util.Iterator;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.cleartk.ne.type.NamedEntity;
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
       * Using Standord NLP NamedEntity annotations to compute score.
       */
      JFSIndexRepository repository = jcas.getJFSIndexRepository();
      FSIterator iter = repository.getAllIndexedFS(NamedEntity.type);

      double total = 0;
      int numTokens = 0;
      
      while (iter.hasNext())
      {
    	NamedEntity namedEntity = (NamedEntity) iter.next();
      	total += namedEntity.getScore();
      	System.out.println(total);
      	numTokens++;
      }
      
      answerScore.setScore(total/numTokens);
      answerScore.addToIndexes();
    }    
  }
}