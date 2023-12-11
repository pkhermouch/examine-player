package com.examineplayer;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Examine Player"
)
public class ExaminePlayerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExaminePlayerConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			log.info(String.format("Login screen - Examine text: %s", config.getExamineText()));
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("examineplayer"))
		{
			log.info(String.format("Config changed! Examine text: %s", config.getExamineText()));
		}
	}

	@Provides
	ExaminePlayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExaminePlayerConfig.class);
	}
}
