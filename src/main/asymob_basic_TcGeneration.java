package main;

import core.Asymob;

public class asymob_basic_TcGeneration {

	public static void main(String[] args) {
		
		Asymob botTester;
		String strPathIn, strPathOut;
		botTester = new Asymob();
		
		if(args.length>2)
		{
			strPathIn = args[0];
			strPathOut = args[1];
		}
		else
		{
			// /PERSONAL/TRABAJO/MISO/asymob/chatbots/conga/bikeShop.xmi
			// /PERSONAL/TRABAJO/MISO/PRUEBA/proyectosGod/Cinebot/test-conga/cinebot/cinebot.xmi
			// /PERSONAL/TRABAJO/MISO/PRUEBA/proyectosGod/cooking-assistant/test-conga/cooking-assistant.xmi
			// /PERSONAL/TRABAJO/MISO/
			strPathIn = "/PERSONAL/TRABAJO/MISO/asymob/chatbots/conga/bikeShop.xmi";
			strPathOut = "/PERSONAL/TRABAJO/MISO/asymob-output";			
		}
		
		if(botTester.loadChatbot(strPathIn))
		{
			
			// Medium
			// Exhaustive
			// Basic			
			String method = "Medium";
			botTester.printBotSummary();
			botTester.generateTestCases(strPathOut, method);
		}
	}
}
