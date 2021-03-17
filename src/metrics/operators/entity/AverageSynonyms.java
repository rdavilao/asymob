package metrics.operators.entity;

import analyser.EntityAnalyser;
import generator.EntityInput;
import generator.LanguageInput;
import generator.SimpleInput;
import metrics.base.FloatMetricValue;
import metrics.base.IntegerMetricValue;
import metrics.operators.EMetricOperator;
import metrics.operators.base.EntityMetricBase;

public class AverageSynonyms extends EntityMetricBase{

	public AverageSynonyms() {
		super(EMetricOperator.eAverageSynonyms);
	}

	@Override
	public void calculateMetric() {
		
		int nSynonyms, nEntities;
		float fAverage;
		EntityAnalyser enAnalyser;
		
		//Initialise
		nSynonyms = nEntities = 0;
		enAnalyser = new EntityAnalyser();
		
		//Calculate
		try
		{
			for(LanguageInput lan: entityIn.getInputs())
			{
				for(EntityInput enIn: lan.getInputs())
				{
					if(enIn instanceof SimpleInput)
					{
						nSynonyms += enAnalyser.analyseNumSynonyms((SimpleInput)enIn);
						nEntities++;
					}
				}
			}
			fAverage = (float) nSynonyms/ (float)nEntities;
			metricRet = new FloatMetricValue(fAverage);
			metricRet.setMetricApplied(this);
		}catch(Exception e)
		{
			metricRet = null;
		}
		
		
	}

}