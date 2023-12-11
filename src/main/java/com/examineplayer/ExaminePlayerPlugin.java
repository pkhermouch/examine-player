package com.examineplayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatClient;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.http.api.chat.Task;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@PluginDescriptor(
	name = "Examine Player"
)
public class ExaminePlayerPlugin extends Plugin
{
	private final String REDIS_KEY_PREFIX = "examineplayer:";
	@Inject
	private Client client;

	@Inject
	private ExaminePlayerConfig config;

	@Inject
	private ChatClient chatClient;

	@Inject
	private ScheduledExecutorService executor;

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

	private void setExamineText() {
		// Since we don't have a generic API endpoint to store an arbitrary string in the Redis store so that other
		// clients can read information about this player, piggy-back off of the Slayer plugin's endpoint to store
		// slayer information.
		// Add a prefix to the player name so that our Redis keys are namespaced and won't ever collide with data
		// written by the Slayer plugin.
		final String playerName = client.getLocalPlayer().getName();
		final String examineText = config.getExamineText().substring(0, 140);
		executor.execute(() ->
		{
			try
			{
				chatClient.submitTask(
						String.format("%s%s", REDIS_KEY_PREFIX, playerName),
						examineText,
						50,
						100,
						"Atlantis");
			}
			catch (Exception ex)
			{
				log.warn("Unable to set examine text", ex);
			}
		});
	}

	private String getExamineText(String playerName) {
		final String keyName = String.format("%s%s", REDIS_KEY_PREFIX, playerName);
		Task task;
		try {
			task = chatClient.getTask(keyName);
		}
		catch (IOException e) {
			log.warn(String.format("Unable to get examine text for player %s", playerName), e);
			return "";
		}

		return task.getTask();
	}

	@Provides
	ExaminePlayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExaminePlayerConfig.class);
	}
}
