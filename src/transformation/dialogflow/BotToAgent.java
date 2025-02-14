package transformation.dialogflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.EList;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import analyser.BotAnalyser;
import analyser.Conversor;
import analyser.TokenAnalyser;
import auxiliar.BotResourcesManager;
import auxiliar.Common;
import generator.Action;
import generator.Bot;
import generator.CompositeInput;
import generator.Entity;
import generator.EntityInput;
import generator.HTTPRequest;
import generator.Image;
import generator.Intent;
import generator.IntentInput;
import generator.IntentLanguageInputs;
import generator.KeyValue;
import generator.Language;
import generator.LanguageInput;
import generator.Literal;
import generator.Parameter;
import generator.ParameterReferenceToken;
import generator.RegexInput;
import generator.SimpleInput;
import generator.Text;
import generator.Token;
import generator.TrainingPhrase;
import generator.UserInteraction;
import transformation.ITransformation;
import transformation.dialogflow.agent.Agent;
import transformation.dialogflow.agent.Webhook;
import transformation.dialogflow.agent.entities.Entry;
import transformation.dialogflow.agent.intents.Data;
import transformation.dialogflow.agent.intents.Message;
import transformation.dialogflow.agent.intents.Response;
import zipUtils.Zip;

public class BotToAgent implements ITransformation{

	Conversor conversor;
	private BotResourcesManager botManager;
	private BotAnalyser botAnalyser;
	private TokenAnalyser tokAnalyser;
	private final String LAN_ENGLISH = "ENGLISH";
	private final String USER_SAYS_FILE_TAG= "_usersays_";
	private final String ENTRIES_FILE_TAG= "_entries_";
	private final String FOLDER_ENTITIES_TAG = "entities";
	private final String FOLDER_INTENTS_TAG = "intents";
	private final String JSON_DOT_TAG = ".json";
	private final String PACKAGE_TAG = "package";
	private final String AGENT_TAG = "agent";
	private Zip zip;
	
	String strOutputPath;
	
	public BotToAgent() {
		zip = null;
		botManager = new BotResourcesManager();
		conversor = new ConversorDialogFlow();
		tokAnalyser = new TokenAnalyser(conversor);
		botAnalyser = new BotAnalyser(conversor);		
	}
	@Override
	public boolean transform(Bot botIn, String strOutputPath) {
		boolean bRet;
		
		bRet =  false;
		this.strOutputPath = strOutputPath;
		
		try
		{
			//Agent
			exportAgent(botIn);
			
			//Package
			exportPackage();
			
			//Entities
			exportEntities(botIn.getEntities());
			
			//Intents: Training phrases
			exportTrainingPhrases(botIn.getIntents());
			
			//Actions: Bot responses
			exportFlows(botIn.getFlows());
			
			//Zip del archivo.
			//doZip();
		}
		catch(Exception e)
		{
			System.out.println("[transform] Exception while transforming the bot to DialogFlow");
			bRet = false;
		}
		return bRet;
	}


	private void exportPackage() {
		
		String jsonString;
		transformation.dialogflow.agent.Package packageIn;
		
		packageIn = new transformation.dialogflow.agent.Package();
		packageIn.setVersion("1.0.0");
		try {
			jsonString = jsonifyObject(packageIn);
			exportData(this.strOutputPath, PACKAGE_TAG+JSON_DOT_TAG, jsonString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
	}
	private void exportAgent(Bot botIn) {
		Agent agent;
		String strName, jsonString;
		Webhook webHook;
		HTTPRequest request;
		List<String> langList;
		
		try
		{
			//Initialisation
			agent = new Agent();
			request = null; //TODO: Esto hay que saber de donde lo sacamos.
			
			//language
			strName = botIn.getLanguages().get(0).getName();
			strName = conversor.convertLanguageToAgent(strName);		
			agent.setLanguage(strName);
			
			//defaultTimezone
			agent.setDefaultTimezone("Europe/Madrid");
			
			//Webhook
			webHook = processWebHook(request);
			agent.setWebhook(webHook);
			
			//Private, Customclassifier, minconfidence
			agent.setPrivate(false);
			agent.setCustomClassifierMode("use.after");
			agent.setMlMinConfidence(0.3);
			
			//Supported languages
			langList = processLanguages(botIn.getLanguages()); 
			agent.setSupportedLanguages(langList);
			
			//OneplatformApiversion, 
			agent.setOnePlatformApiVersion("v2");
			agent.setAnalyzeQueryTextSentiment(false);
			agent.setKnowledgeServiceConfidenceAdjustment(-0.4);
			agent.setDialogBuilderMode(false);
			agent.setBaseActionPackagesUrl("");
			
			//Converting the Object to JSONString
			jsonString = jsonifyObject(agent);
					
			//Export
			exportData(this.strOutputPath, AGENT_TAG+JSON_DOT_TAG, jsonString);
			
			//Add to the zip file
			//TODO:
		}
		catch(Exception e)
		{
		}
	}
	private List<String> processLanguages(EList<Language> languages) {
		List<String> listRet;
		String strLanIndex;
		
		listRet = null;
		if(languages != null)
		{
			listRet = new LinkedList<String>();
			for(Language lan: languages)
			{
				strLanIndex = lan.getName();
				if(conversor != null)
					strLanIndex = conversor.convertLanguageToAgent(strLanIndex);
				
				listRet.add(strLanIndex);
			}
		}
		return listRet;
	}
	private Webhook processWebHook(HTTPRequest request) {
		Webhook webHook;
		Map<String, String> rqHeaders;
		String strKey, strValue;
		Token tokValue;
		webHook = new Webhook();
		if(request == null)
		{
			webHook.setUrl("");
			webHook.setUsername("");
			webHook.setHeaders(null);
			webHook.setAvailable(false);
			webHook.setUseForDomains(false);
			webHook.setCloudFunctionsEnabled(false);
			webHook.setCloudFunctionsInitialized(false);
		}
		else
		{
			webHook.setUrl(request.getURL());
			if(request.getHeaders() != null)
			{
				rqHeaders = new HashMap<String, String>();
				
				for(KeyValue keyVal: request.getHeaders())
				{
					strKey = keyVal.getKey();
					tokValue = keyVal.getValue();
					strValue = tokAnalyser.getTokenText(tokValue);
					
					if(strKey != null && strValue != null)
					{
						rqHeaders.put(strKey, strValue);
					}
				}
				webHook.setHeaders(rqHeaders);			
			}
			else
				webHook.setHeaders(null);
			
			webHook.setAvailable(true);
			webHook.setUseForDomains(false);
			webHook.setCloudFunctionsEnabled(false);
			webHook.setCloudFunctionsInitialized(false);	
		}
		
		return webHook;
	}
	private void exportEntities(EList<Entity> entities) {
		int nEntity;
		
		nEntity = 0;
		
		if(entities != null)
		{
			for(Entity entity: entities)
			{
				try
				{
					exportEntity(entity);
				}
				catch(Exception e)
				{
					System.out.printf("[exportEntities] - Exception while exporting entity [%d]\n", nEntity);
				}
				nEntity++;
			}
		}
	}
	private void exportEntity(Entity entityIn) throws Exception {
		String strEntityName, strLanguage;
		transformation.dialogflow.agent.entities.Entity entityOut;
		Entry entryOut;
		List<Entry> entryList;
		
		if(entityIn == null)
			throw new Exception("The entity is null");
		
		//Create the entry list
		entryList = new LinkedList<Entry>();
		
		//We need to export both the entity and all the languages with its inputs
		strEntityName = entityIn.getName();
		
		//First, process and save the entity
		entityOut = processEntity(entityIn);
		saveEntityFile(strEntityName+JSON_DOT_TAG, entityOut);
		
		//Next,  process and save each input
		for(LanguageInput lan: entityIn.getInputs())
		{
			strLanguage = lan.getLanguage().getName();
			strLanguage = conversor.convertLanguageToAgent(strLanguage);
			for(EntityInput entityInput : lan.getInputs())
			{
				entryOut = processEntityInput(entityInput);				
				entryList.add(entryOut);
			}
			saveEntryInputFile(strEntityName+ENTRIES_FILE_TAG+strLanguage+JSON_DOT_TAG, entryList);
		}
	}

	private transformation.dialogflow.agent.entities.Entry  processEntityInput(EntityInput entityInput) {
		transformation.dialogflow.agent.entities.Entry  entryRet;
		
		entryRet = null;
		
		entryRet =new Entry();
		
		if(entityInput instanceof SimpleInput)
		{
			SimpleInput simpleInput;
			LinkedList<String> synonymList;
			synonymList = new LinkedList<String>();
			simpleInput = (SimpleInput)entityInput;
			entryRet.setValue(simpleInput.getName());
			
			synonymList.addAll(simpleInput.getValues());
			entryRet.setSynonyms(synonymList);
		}
		else if(entityInput instanceof CompositeInput)
		{
			CompositeInput compositeInput;
			
			compositeInput = (CompositeInput) entityInput;
			//compositeInput.get
			//entryRet.setSynonyms(compositeInput.getExpresion());
		}else if(entityInput instanceof RegexInput)
		{
			RegexInput regexInput;
			
			regexInput = (RegexInput) entryRet;
			
			//TODO: Not done
		}
		return entryRet;
	}
	private void saveEntryInputFile(String strEntryName, List<Entry> entryList) throws JsonProcessingException {
		String jsonString;
		
		//Converting the Object to JSONString
		jsonString = jsonifyObject(entryList);
		
		//Export
		exportData(this.strOutputPath+ File.separator+this.FOLDER_ENTITIES_TAG, strEntryName, jsonString);
	}
	private void saveEntityFile(String strEntityName,
			transformation.dialogflow.agent.entities.Entity entityOut) throws JsonProcessingException {
		String jsonString;
		jsonString = jsonifyObject(entityOut);
		
		//Export
		exportData(this.strOutputPath+ File.separator+this.FOLDER_ENTITIES_TAG, strEntityName, jsonString);
	}

	private transformation.dialogflow.agent.entities.Entity processEntity(Entity entity) {
		transformation.dialogflow.agent.entities.Entity entityOut;
		
		entityOut = new transformation.dialogflow.agent.entities.Entity();
		
		//Id
		entityOut.setId(Common.generateType1UUID().toString());
		//Name
		entityOut.setName(entity.getName());
		//Overridable
		entityOut.setOverridable(true);
		//Is enum
		entityOut.setEnum(false);
		//Is regex
		entityOut.setRegexp(false);
		//Automated expansion
		entityOut.setAutomatedExpansion(true);
		//Fuzzy extraction
		entityOut.setAllowFuzzyExtraction(true);
		
		//TODO: En el export de xtend tenemos: «IF BotGenerator.entityType(entity) === BotGenerator.REGEX»
		return entityOut;
	}
	private void exportFlows(EList<UserInteraction> flows) {
		List<Pair<UserInteraction, Action>> indexList;
		HashMap<UserInteraction,List<Action>> intentActionMap;
		UserInteraction interaction;
		List<Action> actionList;

		if(flows != null)
		{
			intentActionMap = new HashMap<UserInteraction,List<Action>>();
			//Group the user interactions in a map
			/*for(UserInteraction userIn: flows)
			{
				//indexList = botAnalyser.plainActionTree(userIn);
				//processIntentListIntoMap(intentActionMap, indexList);
				createTransitionFiles(userIn, "");
			}*/
			 for(UserInteraction userIn: flows)
			{
				indexList = botAnalyser.plainActionTree(userIn);
				processIntentListIntoMap(intentActionMap, indexList);				
			}
			//Now, for each intent and its associated list of actions: we export it!
			Iterator<?> iterator = intentActionMap.entrySet().iterator();
	        while (iterator.hasNext()) {
	          Map.Entry me2 = (Map.Entry) iterator.next();
	          System.out.println("Key: "+me2.getKey() + " & Value: " + me2.getValue());
	          interaction = (UserInteraction)me2.getKey();
	          actionList = (List<Action>) me2.getValue();
	          
		        try {
					exportInteraction(interaction, actionList);
				} catch (Exception e) {
					e.printStackTrace();
				}
	        } 
		}
		
	}
	private void createTransitionFiles(UserInteraction userIn, String string) {
		Intent intent;
		
		if(userIn != null)
		{
			intent = userIn.getIntent();
			
			if(userIn.getTarget() != null)
			{
				for(UserInteraction user: userIn.getTarget().getOutcoming())
				{
					//indexList = botAnalyser.plainActionTree(userIn);
					//processIntentListIntoMap(intentActionMap, indexList);
					createTransitionFiles(user, "");
				}
			}
		}
	}
	private void exportInteraction(UserInteraction userInter, List<Action> actionList) throws Exception {
		String strName, jsonString;
		List<Response> jsonList;
		transformation.dialogflow.agent.intents.Intent intentJSON;
		Intent intent;
		
		try
		{
			intent = userInter.getIntent();
			
			//TODO: Here its necessary to check if we have previously loaded the Chabot
			//In this case, we try to re-use all the data included, instead of creating new data.
			intentJSON = new transformation.dialogflow.agent.intents.Intent();
			
			//Name
			strName = intent.getName();
			intentJSON.setName(strName);
			
			//ID
			intentJSON.setId(Common.generateType1UUID_String());
			
			//Contexts
			/*if(userInter.getSrc() != null)
				createContext();
			else*/
					
			//TODO: Rellenar
			
			//Responses
			jsonList = createResponseList(intent, actionList);
			intentJSON.setResponses(jsonList);
			
			//transform to JSON
			jsonString = jsonifyObject(intentJSON);
			
			exportData(this.strOutputPath+File.separator+FOLDER_INTENTS_TAG, strName+JSON_DOT_TAG, jsonString);
		}
		catch(Exception e)
		{
			System.out.println("Exception while exporting intent: "+e.getMessage());
		}
	}
	private List<Response> createResponseList(Intent intent, List<Action> actionList) {
		List<Response> resList;
		List<Message> messageList;
		List<String> speech, textList;
		Response response;
		Message message;
		int nType;
		boolean bWebHook;
		String strMessage, strLan;
		Language lan;
		
		response = new Response();
		message = new Message();
		resList = new LinkedList<Response>();
		messageList = new LinkedList<Message>();
		speech = new LinkedList<String>();

		strLan = "";
		bWebHook = false;
		textList = null;
		
		//TODO: Revisar esto, no se si estara bien
		
		//reset context
		//Ver por aqui
		
		//action
		
		//ResetContexts
		response.setResetContexts(false);
		
		//affectedContexts
		//TODO:
		
		//Parameters!
		//TODO: 
		
		//Messages
		for(Action action: actionList)
		{
			if(action != null)
			{
				
				if(action!= null)
				{
					if(action instanceof Text)
					{
						message.setType("0");
						message.setCondition("");
						
						textList = botAnalyser.extractAllActionPhrasesByRef(action);
						if(textList != null)
							speech.addAll(textList);
						/*
						for(TextLanguageInput texIn: ((Text) action).getInputs())
						{
							textList = null;
							strLan = conversor.convertLanguageToAgent(strLan);
							message.setLang(strLan);							
							
							//strMessage = texIn.
							
						}*/
						
					}
					else if(action instanceof Image)
					{
						message.setType("3");
					}
					else if(action instanceof HTTPRequest)
					{
						bWebHook = true;
					}
				}
			}
		}

		//Add speech to the message
		message.setSpeech(speech);
		//Message list to response
		response.setMessages(messageList);
		resList.add(response);
		
		return resList;
	}
	private void processIntentListIntoMap(HashMap<UserInteraction, List<Action>> intentActionMap,
			List<Pair<UserInteraction, Action>> indexList) {
		List<Action> actionList;
		Action action;
		UserInteraction intent;
		
		action = null;
		intent = null;
		actionList = null;
		if(intentActionMap!= null && indexList != null)
		{
			for(Pair<UserInteraction, Action> pairIntentAction: indexList)
			{				
				if (pairIntentAction != null)
				{
					intent = (UserInteraction) pairIntentAction.getLeft();
					action = (Action) pairIntentAction.getRight();
				}
				
				if(intent!= null && action != null)
				{
					if(action instanceof Text)
					{
						actionList = null;
						//TODO: Danger! Temporally filtered by textimpl
						if(intentActionMap.containsKey(intent))
						{
							actionList = intentActionMap.get(intent);
						}
						if(actionList==null)
							actionList = new LinkedList<Action>();
						
						actionList.add(action);
						intentActionMap.put(intent, actionList);
					}
				}
			}
		}
	}
	//Nombre: Nombre_intent + '_usersays_' + lan [Default Fallback Intent_usersays_en.json]
	private void exportTrainingPhrases(EList<Intent> intentsList) 
	{
		int nPhrase;
		
		nPhrase = 0;
		if(intentsList != null)
		{
			for(Intent intent: intentsList)
			{
				try {
					exportTrainingPhrases(intent);
				} catch (Exception e) {
					System.out.printf("[exportTrainingPhrases] Exception while processing TP: %d [%s]\n", nPhrase, e.getMessage());
				}
				nPhrase++;
			}
		}
	}

	private void exportTrainingPhrases(Intent intentIn) throws Exception{
		List<IntentLanguageInputs> listLanguages;
		String strIntenName;
		
		if(intentIn == null)
			throw new Exception("exportTrainingPhrases - input intentIn is null");
		
		strIntenName = intentIn.getName();
		//Analyse the different languages
		listLanguages = intentIn.getInputs();
		
		for (IntentLanguageInputs intentLan : listLanguages)
			exportIntentLanguage(strIntenName, intentLan);			
	}

	private void exportIntentLanguage(String strIntenName, IntentLanguageInputs intentLan) throws JsonProcessingException {
		EList<IntentInput> inputList;
		String strName, strLan, jsonString;
		List<transformation.dialogflow.agent.intents.TrainingPhrase> trainingPhrasesList;
		ObjectMapper mapper;
			
		//ID
		inputList = intentLan.getInputs();
		strName = intentLan.getLanguage().getName();
		
		strLan = conversor.convertLanguageToAgent(strName);
		
		trainingPhrasesList = processTrainingPhrases(inputList, strLan);
		
		jsonString = jsonifyObject(trainingPhrasesList);
		
		strName = strIntenName+USER_SAYS_FILE_TAG+strLan+JSON_DOT_TAG;
		
		//transform to JSON
		/*mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		//jsonifyObject(objIn)
	    //Converting the Object to JSONString			
		jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonList);*/
		
		exportData(this.strOutputPath+File.separator+FOLDER_INTENTS_TAG, strName, jsonString);
	}
	private void exportData(String strOutput, String strName, String jsonString) {
		File fileToSave;
		BufferedWriter writer;
		
		if(strName != null && jsonString!=null)
		{
			fileToSave = Common.fileWithDirectoryAssurance(strOutput, strName);
			
			try {
				writer = new BufferedWriter(new FileWriter(fileToSave));
				writer.write(jsonString);
				writer.flush();		
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("[exportData] Error exporting file: "+strName);
			}
		}
	}
	//Cada TrainingPhrase tiene:
	/*"id": "c20be499-5dd0-475f-bedd-a895f76f3aec",
		    "data": [
		      {
		        "text": "tell me some jokes",
		        "userDefined": false
		      }
		    ],
		    "isTemplate": false,
		    "count": 0,
		    "lang": "en",
		    "updated": 0
		  },
	 */
	private List<transformation.dialogflow.agent.intents.TrainingPhrase> processTrainingPhrases(EList<IntentInput> inputList, String strLan) {
		TrainingPhrase trainPhrase;
		UUID trainingId;
		Data tokenJson;
		transformation.dialogflow.agent.intents.TrainingPhrase trainingJSON;
		List<Data> jsonList;
		List<transformation.dialogflow.agent.intents.TrainingPhrase> retList;
		
		retList = new LinkedList<transformation.dialogflow.agent.intents.TrainingPhrase>();
		//Find all the inputs and process them
		for (IntentInput input : inputList) 
		{
			jsonList = new LinkedList<Data>();
			trainingJSON = new transformation.dialogflow.agent.intents.TrainingPhrase();
			if(input instanceof TrainingPhrase)
			{
				trainPhrase = (TrainingPhrase) input;
				
				//Generate UUID and assign it to the objetct
				trainingId = Common.generateType1UUID();
				trainingJSON.setId(trainingId.toString());
				
				//Process the Tokens
				for(Token tokIn: trainPhrase.getTokens())
				{
					tokenJson = transformTokenJson(tokIn);
					jsonList.add(tokenJson);
				}
				trainingJSON.setData(jsonList);
				trainingJSON.setTemplate(false);
				trainingJSON.setCount(0);
				trainingJSON.setLang(strLan);
				trainingJSON.setUpdated(0);
				
				retList.add(trainingJSON);
			}
		}
		
		return retList;
	}


	private Data transformTokenJson(Token tokIn) {
		ParameterReferenceToken paramRefIn;
		Parameter param;
		String strText, strName, strMeta;
		Literal litIn;
		Data tokenJson;
		
		tokenJson = new Data();
		strText=strMeta=strText=strName=null;
		
		if(tokIn != null)
		{
			if (tokIn instanceof Literal) 
			{
				//process as literal
				litIn = (Literal) tokIn;
				
				if(litIn != null)
					strText = litIn.getText();
				
				tokenJson.setText(strText);
				tokenJson.setUserDefined(false);
			}
			else if(tokIn instanceof ParameterReferenceToken)
			{
				paramRefIn = (ParameterReferenceToken) tokIn;
				
				if(paramRefIn != null)
				{
					strText = paramRefIn.getTextReference();
					param = paramRefIn.getParameter();
					
					if(param != null)
					{
						strName = param.getName();
						if(param.getEntity() != null)
							strMeta = param.getEntity().getName();
					}
						
					if(conversor!=null)
					{
						strMeta = conversor.convertReferenceToAgent(strName);
					}
					tokenJson.setText(strText);
					tokenJson.setAlias(strName);
					tokenJson.setMeta(strMeta);
					tokenJson.setUserDefined(true);
				}
			}
			
			
		}
		
		return tokenJson;
	}
	
	@Override
	public boolean transform(String strPathIn, String strPathOut) {
		boolean bRet;
		Bot bot;
		
		bRet = false;
		if(strPathIn != null && botManager != null)
		{
			if(botManager.loadChatbot(strPathIn))
			{
				bot = botManager.getCurrentBot();
				bRet = transform(bot, strPathOut);
			}
		}
		return bRet;
	}
	private String jsonifyObject(Object objIn)
			throws JsonProcessingException {
		String jsonString;
		ObjectMapper mapper;
		
		mapper = new ObjectMapper();
		//transform to JSON
		mapper.setSerializationInclusion(Include.NON_NULL);
		
	    //Converting the Object to JSONString			
		jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objIn);
		
		return jsonString;
	}
	/*
	public LinkedList<String> extractAllActionPhrases(Action actionIn, boolean bRef) {

		LinkedList<String> retList;

		retList = null;

		if(actionIn != null)
		{
			//text
			if (actionIn instanceof Text)
			{								
				retList = handleTextAction(actionIn, bRef);
			}
			//image
			else if (actionIn instanceof Image)
			{
				handleImageAction(actionIn);
			}
			//HttpRequest
			else if(actionIn instanceof HTTPRequest)
			{
				//TODO: Ver como enfocarlo y terminar esta parte
			}
			//HttpResponse
			else if(actionIn instanceof HTTPResponse)
			{
				//TODO: Ver como enfocarlo y terminar esta parte

			}
		}
		return retList;
	}

	private void handleImageAction(Action actionIn) {
		// TODO Auto-generated method stub

	}

	private LinkedList<Data> handleTextAction(Action actionIn, boolean bRef) {
		Text actionText;
		EList<TextLanguageInput> textLanInputList;
		EList<TextInput> textInputList;
		LinkedList<Data> retList, auxList;
		actionText = (Text) actionIn;

		retList = null;
		if(actionIn != null)
		{
			textLanInputList = actionText.getInputs();

			if(textLanInputList != null)
			{
				retList = new LinkedList<String>();
				for(TextLanguageInput textLanIn: textLanInputList)
				{
					textInputList = textLanIn.getInputs();
					if(textInputList != null)
					{
						for(TextInput textIn: textInputList)
						{
							auxList = extractPhrasesFromTextActionByRef(textIn);
							retList.addAll(auxList);
						}
					}
				}	
			}
		}

		return retList;
	}	*/

	

	
}
