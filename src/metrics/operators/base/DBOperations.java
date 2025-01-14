package metrics.operators.base;

import java.util.LinkedList;

import metrics.base.EMetricCategory;
import metrics.base.FloatMetricValue;
import metrics.base.IntegerMetricValue;
import metrics.base.MetricValue;
import metrics.db.ReadOnlyMetricDB;
import metrics.operators.EMetricOperator;

public class DBOperations {

	public static float getAverage(ReadOnlyMetricDB db, EMetricOperator metricIn) {
		int nElements, nParams;
		float fValue, fParams;
		LinkedList<MetricValue> metricResList;
		
		nElements=nParams=0;
		fValue = -1;
		fParams = 0;
		
		//access to the DB
		if(db != null)
		{
			metricResList = db.getIntentMetric(metricIn);
			
			if(metricResList != null && metricResList.size()>0)
			{
				for(MetricValue metricVal: metricResList)
				{
					if(metricVal != null && metricVal instanceof IntegerMetricValue)
					{
						nParams += ((IntegerMetricValue)metricVal).getIntValue();
						nElements++;
					}
					else if (metricVal != null && metricVal instanceof FloatMetricValue)
					{
						fParams += ((FloatMetricValue) metricVal).getFloatValue();
						nElements++;
					}
				}
				if(nParams >0 && nElements >0)
					fValue = (float)((float)nParams/(float)nElements);
				else if (fParams >0 && nElements >0)
					fValue = (float)((float)fParams/(float)nElements);
				else
					fValue =0;
			}
		}
		return fValue;
	}

	public static float getMin(ReadOnlyMetricDB db, EMetricOperator metricIn) {
		int nIntElements, nFloatElements, nParam, nMinimum;
		float fValue, fParams, fMinimum;
		LinkedList<MetricValue> metricResList;
		
		fMinimum = Float.MAX_VALUE;
		nMinimum=Integer.MAX_VALUE;
		nIntElements=nFloatElements=0;
		fValue = -1;
		fParams = 0;
		
		//access to the DB
		if(db != null)
		{
			metricResList = db.getIntentMetric(metricIn);
			
			if(metricResList != null && metricResList.size()>0)
			{
				for(MetricValue metricVal: metricResList)
				{
					if(metricVal != null && metricVal instanceof IntegerMetricValue)
					{
						
						nParam = ((IntegerMetricValue)metricVal).getIntValue();
						if(nParam<nMinimum)
							nMinimum = nParam;
						
						nIntElements++;
					}
					else if (metricVal != null && metricVal instanceof FloatMetricValue)
					{
						fParams += ((FloatMetricValue) metricVal).getFloatValue();
						
						if(fParams<fMinimum)
							fMinimum = fParams;
						
						nFloatElements++;
					}
				}
				if(nIntElements>0)
					fValue = nMinimum;
				else if (nFloatElements>0)
					fValue = fMinimum;
			}
		}
		return fValue;
	}
	
	public static float getAverage(ReadOnlyMetricDB db, EMetricCategory eCategory, EMetricOperator metricIn) {
		int nElements, nParams;
		float fValue, fParams;
		LinkedList<MetricValue> metricResList;
		
		nElements=nParams=0;
		fParams = 0;
		fValue = -1;
		//access to the DB
		if(db != null)
		{
			metricResList = extractMetricList(db, eCategory, metricIn);
			
			if (metricResList != null  && metricResList.size()>0)
			{
				for(MetricValue metricVal: metricResList)
				{
					if(metricVal != null && metricVal instanceof IntegerMetricValue)
					{
						nParams += ((IntegerMetricValue)metricVal).getIntValue();
						nElements++;
					}
					else if (metricVal != null && metricVal instanceof FloatMetricValue)
					{
						fParams += ((FloatMetricValue) metricVal).getFloatValue();
						nElements++;
					}
				}
				if(nParams >0 && nElements >0)
					fValue = (float)((float)nParams/(float)nElements);
				else if (fParams >0 && nElements >0)
					fValue = (float)((float)fParams/(float)nElements);
				else
					fValue =0;
			}
		}
		return fValue;
	}

	private static LinkedList<MetricValue> extractMetricList(ReadOnlyMetricDB db, EMetricCategory eCategory,
			EMetricOperator metricIn) {
		LinkedList<MetricValue> metricResList;
		switch(eCategory)
		{
		case eIntent:
			metricResList = db.getIntentMetric(metricIn);
			break;
		case eEntity:
			metricResList = db.getEntityMetric(metricIn);
			break;
		case eBot:
			metricResList = db.getBotMetrics();
		case eFlow:
			metricResList = db.getFlowMetric(metricIn);
			break;
		case eGlobalFlow:
			metricResList = db.getBotMetrics();
			break;
		default:
			metricResList = null;
			break;
		}
		return metricResList;
	}
}
