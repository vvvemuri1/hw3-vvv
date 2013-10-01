package edu.cmu.deiis.casConsumers;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.resource.ResourceProcessException;

/**
 * This system takes a fully processed CAS from the annotators and prints
 * out the sorted answers and the precision score.
 * @author vvvemuri1
 */
public class EvaluationCasConsumer extends CasConsumer_ImplBase implements CasConsumer 
{
	/**
	 * 
	 */
	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException 
	{
		/// TODO
	}
}
