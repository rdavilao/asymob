package metrics.base;

import metrics.db.MetricDataBase;
import metrics.operators.EMetricOperator;

public abstract class Metric {

	EMetricOperator metric;
	EMetricCategory category;
	
	protected MetricDataBase db;
	protected MetricValue metricRet;
	
	public Metric(EMetricOperator metric, EMetricCategory category)
	{
		this.db = null;
		this.metric = metric;
		this.category = category;
	}
	public EMetricCategory getCategory() {
		return category;
	}
	public void setCategory(EMetricCategory category) {
		this.category = category;
	}
	
	public String getMetricName()
	{
		return metric != null ? metric.name() : "null";
	}
	
	public MetricValue getResults() {
		return metricRet;
	}
	
	public void reset()
	{
		metricRet = null;
	}
	public abstract void calculateMetric();
}
