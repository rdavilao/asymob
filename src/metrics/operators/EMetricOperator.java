package metrics.operators;

public enum EMetricOperator {

	//Global
	eNumEntities, eNumPhrases, eAverageLength, eSentiment, eAvgParameters, 
	eNumLanguages, eNumIntents, eNumFlows, eNumPaths, eGlobalAveragePathFlow,
	eGlobalAvgEntityLiterals,  eGlobalAvgEntitySynonyms, eGlobalEntityWordLen,
	eGlobalFlowAvgActions, eGlobalAvgIntentPhrases, eGlobalAvgIntentWordPerPhrase,
	eGlobalAvgIntentParameters, eGlobalAvgIntentCharsPerPhrase, eGlobalAvgReadingTime, 
	eGlobalAvgReqIntentParameters,
	
	//Entities
	eEntityAvgSynonyms, eEntityWordLen, eEntityNumLiterals,
	
	//Flows
	eFlowNumPaths, eFlowLength, eFlowActionsAverage,
	
	//Intents
	eIntentNumPhrases, eIntentAvgWordsPerPhrase, eIntentAvgCharsPerPhrase, 
	eIntentNumParameters, eIntentAvgNounsPerPhrase, eIntentAvgVerbsPerPhrase,
	ePositiveSentiment,eIntentMaxWordLen, eReadabilityMetrics, eIntentAvgCosineSimilarity, 
	eIntentNumReqParameters, 
	
	//Extended
	eExtended,         
}
