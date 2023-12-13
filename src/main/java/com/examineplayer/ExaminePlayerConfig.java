package com.examineplayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("examineplayer")
public interface ExaminePlayerConfig extends Config
{
	@ConfigItem(
			keyName = "examineText",
			name = "Examine text",
			description = "Your player's examine text. Max 50 characters. Only letters, numbers, spaces, hyphens and commas are allowed."
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

}
