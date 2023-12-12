package com.examineplayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("examineplayer")
public interface ExaminePlayerConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
			keyName = "examineText",
			name = "Examine text",
			description = "Your player's examine text. Max 140 characters"
	)
	default String getExamineText()
	{
		return "";
	}

	@ConfigItem(
			keyName = "examineText",
			name = "",
			description = ""
	)
	void setExamineText(String key);

	@ConfigItem(
			keyName = "logPlayerExamineText",
			name = "Log player examine text",
			description = "Toggle to log the current player's examine text"
	)
	default boolean logPlayerExamineText()
	{
		return false;
	}

	@ConfigItem(
			keyName = "syncPlayerExamineText",
			name = "Sync player examine text",
			description = "Toggle to sync the current player's examine text"
	)
	default boolean syncPlayerExamineText()
	{
		return false;
	}


}
