package main.ASE;

import java.io.File;

import core.Asymob;
import metrics.MetricOperatorsSet;
import metrics.operators.bot.BotConfusingPhrases;
import metrics.operators.bot.BotSentiment;
import metrics.operators.bot.NumEntities;
import metrics.operators.bot.NumFlows;
import metrics.operators.bot.NumIntents;
import metrics.operators.bot.NumLanguages;
import metrics.operators.bot.globalEntities.AvgEntityLiterals;
import metrics.operators.bot.globalEntities.AvgEntitySynonyms;
import metrics.operators.bot.globalEntities.AvgEntityWordLen;
import metrics.operators.bot.globalIntents.AvgIntentCharsPerPhrase;
import metrics.operators.bot.globalIntents.AvgIntentNumPhrases;
import metrics.operators.bot.globalIntents.AvgIntentParameters;
import metrics.operators.bot.globalIntents.AvgIntentReadingTime;
import metrics.operators.bot.globalIntents.AvgIntentReqParameters;
import metrics.operators.bot.globalIntents.AvgIntentVerbPerPhrase;
import metrics.operators.bot.globalIntents.AvgIntentWordPerPhrase;
import metrics.operators.bot.globalflow.AveragePathsFlow;
import metrics.operators.bot.globalflow.AvgActionsPerFlow;
import metrics.operators.bot.globalflow.FlowAverageLen;
import metrics.operators.bot.globalflow.NumPaths;
import metrics.operators.entity.AverageSynonyms;
import metrics.operators.entity.EntityWordLenght;
import metrics.operators.entity.NumLiterals;
import metrics.operators.flow.FlowActionsAverage;
import metrics.operators.flow.FlowLength;
import metrics.operators.flow.FlowNumPaths;
import metrics.operators.intents.IntentAvgCharsPerPhrase;
import metrics.operators.intents.IntentAvgCosineSimilarity;
import metrics.operators.intents.IntentAvgNounsPerPhrase;
import metrics.operators.intents.IntentAvgVerbsPerPhrase;
import metrics.operators.intents.IntentAvgWordsPerPhrase;
import metrics.operators.intents.IntentMaxWordLen;
import metrics.operators.intents.IntentNumParameters;
import metrics.operators.intents.IntentNumPhrases;
import metrics.operators.intents.IntentReadabilityMetrics;
import metrics.operators.intents.IntentTrainingSentiment;
import metrics.reports.LatexReportGen;
import metrics.reports.MetricReportGenerator;
import metrics.reports.StringReport;
import metrics.reports.StringReportGen;

/**
 * Main method for extracting different chatbot metrics
 * @author Pablo C. Ca&ntildeizares
 *
 */
public class asymob_metrics_ASE {

	public static void main(String[] argv)
	{
		Asymob botTester;
		MetricOperatorsSet metricOps;
		StringReport metReport;
		MetricReportGenerator metricReport;
		
		metReport = new StringReport();
		metricReport = new LatexReportGen();
		botTester = new Asymob();
		metricOps = new MetricOperatorsSet();
		
		//Global metrics
		metricOps.insertMetric(new NumIntents());
		metricOps.insertMetric(new NumEntities());
		metricOps.insertMetric(new NumFlows());		
		metricOps.insertMetric(new NumPaths());
		metricOps.insertMetric(new AvgIntentReadingTime());
		metricOps.insertMetric(new BotConfusingPhrases());
		metricOps.insertMetric(new BotSentiment());
		
		//Intent metrics
		metricOps.insertMetric(new AvgIntentNumPhrases());
		metricOps.insertMetric(new AvgIntentWordPerPhrase());
		metricOps.insertMetric(new AvgIntentVerbPerPhrase());
		metricOps.insertMetric(new AvgIntentParameters());
		metricOps.insertMetric(new AvgIntentCharsPerPhrase());
		
		//Entity
		metricOps.insertMetric(new AvgEntityLiterals());
		metricOps.insertMetric(new AvgEntitySynonyms());
		metricOps.insertMetric(new AvgEntityWordLen());
		
		//Flow metrics
		metricOps.insertMetric(new FlowAverageLen());
		metricOps.insertMetric(new AveragePathsFlow());
		metricOps.insertMetric(new AvgActionsPerFlow());
		
		if(botTester.loadChatbot("model"+File.separator+"bikeShop.xmi"))
		//if(botTester.loadChatbot("chatbots"+File.separator+"conga"+File.separator+"bikeShop.xmi"))
		//if(botTester.loadChatbot("chatbots\\conga\\mysteryAnimal.xmi"))
		//if(botTester.loadChatbot("chatbots"+File.separator+"dialogFlow"+File.separator+"prebuilt"+File.separator+"Job-Interview.zip"))
		//if(botTester.loadChatbot("chatbots"+File.separator+"dialogFlow"+File.separator+"HOTEL-BOOKING-AGENT2.zip"))
		{
			if(botTester.measureMetrics(metricOps))
			{
				metReport = (StringReport) botTester.getMetricReport(metricReport);
				System.out.println(metReport.getReport());
			}
		}
		else
		{
			System.out.println("The file does not exists!");
		}
	}
}