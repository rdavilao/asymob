package analyser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;


import generator.Action;
import generator.Bot;
import generator.BotInteraction;
import generator.HTTPRequest;
import generator.HTTPResponse;
import generator.Image;
import generator.Intent;
import generator.IntentInput;
import generator.IntentLanguageInputs;
import generator.Literal;
import generator.ParameterReferenceToken;
import generator.ParameterToken;
import generator.Text;
import generator.TextInput;
import generator.TextLanguageInput;
import generator.Token;
import generator.TrainingPhrase;
import generator.UserInteraction;

public class BotAnalyser {

	FlowAnalyser flowAnalyser;
	
	public BotAnalyser()
	{
		flowAnalyser = new FlowAnalyser();
	}
	private void extractAllIntentInputs(Bot botIn)
	{
		List<Intent> listIntent;
		List<IntentLanguageInputs> listLanguages;
		if(botIn != null)
		{
			//
			listIntent =  botIn.getIntents();
			
			for (Intent intent : listIntent) {
				listLanguages = intent.getInputs();
			}
		}
	}
	
	public LinkedList<String> extractAllIntentPhrases(Intent intentIn)
	{
		LinkedList<String> retList;
		EList<IntentLanguageInputs> listLanguages;
		EList<IntentInput> inputList;
		TrainingPhrase trainIn;
		EList<Token> tokenList;

		//TODO: Refactor y bien
		
		retList = null;
		if(intentIn != null)
		{
			retList = new LinkedList<String>();
			//Analyse the different languages
			listLanguages = intentIn.getInputs();

			for (IntentLanguageInputs intentLan : listLanguages) {

				if(intentLan != null)
				{
					//System.out.println("extractAllPhrases - Analysing intent in language "+intentLan.getLanguage().getLiteral());
					inputList = intentLan.getInputs();

					//Find all the inputs and process them
					if(inputList != null)
					{
						for (IntentInput input : inputList) {
							
							if (input instanceof TrainingPhrase) {
								trainIn = (TrainingPhrase) input;

								if(trainIn != null)
								{
									tokenList = trainIn.getTokens();
									
									String strPhrase;
									
									strPhrase = "";
									for(Token tokIn: tokenList)
									{
										strPhrase+=getTokenText(tokIn);
										
									}
									if(!strPhrase.isEmpty() && !strPhrase.isBlank())
										retList.add(strPhrase);
								}
							}
						}
					}			
				}
			}
		}	
		
		return retList;
	}
	
	private String getTokenText(Token token) {
		String strText;
		Literal litIn;
		ParameterReferenceToken paramRefIn;
		
		//Initially, the returning string is null
		strText = null;
		
		if(token != null)
		{
			if (token instanceof Literal) 
			{
				//process as literal
				litIn = (Literal) token;
				
				if(litIn != null)
					strText = litIn.getText();
			}
			else if(token instanceof ParameterReferenceToken)
			{
				paramRefIn = (ParameterReferenceToken) token;
				
				if(paramRefIn != null)
					strText = 	paramRefIn.getTextReference();
			}
		}

		return strText;
	}

	public EList<Action> extractActionList(UserInteraction userActIn) {
		EList<Action> retList;
		BotInteraction botInteraction;
		
		retList = null;
		if(userActIn !=null)
		{
			botInteraction = userActIn.getTarget();
			
			if(botInteraction != null)
				retList = botInteraction.getActions();
			
			if(botInteraction.getOutcoming() != null)
			{
				//Here we have another intent with a action list
			}
				
		}
		return retList;
	}

	public LinkedList<String> extractAllActionPhrases(Action actIndex, Intent intent) {
		return flowAnalyser != null ? flowAnalyser.extractAllActionPhrases(actIndex) : null;
	}
	

}