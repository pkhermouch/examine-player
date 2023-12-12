package com.examineplayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
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
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@PluginDescriptor(
	name = "Examine Player",
	enabledByDefault = false
)
public class ExaminePlayerPlugin extends Plugin
{
	private final String REDIS_KEY_PREFIX = "examineplayer:";
	private final Integer MAX_TEXT_LENGTH = 50;
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
			setExamineText();
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
			if (event.getKey().equals("examineText")) {
				log.info(String.format("Config changed! Examine text: %s", config.getExamineText()));
				setExamineText();
			} else if (event.getKey().equals("logPlayerExamineText")) {
				final Player player = client.getLocalPlayer();
				if (player == null || player.getName() == null) {
					log.info("No local player available");
				} else {
					log.info(String.format("Current player text: %s", getExamineText(player.getName())));
				}
			} else if (event.getKey().equals("syncPlayerExamineText")) {
				setExamineText();
				log.info("Synced current player's examine text");
			}
		}
	}

	private void setExamineText() {
		// Since we don't have a generic API endpoint to store an arbitrary string in the Redis store so that other
		// clients can read information about this player, piggy-back off of the Slayer plugin's endpoint to store
		// slayer information.
		// Add a prefix to the player name so that our Redis keys are namespaced and won't ever collide with data
		// written by the Slayer plugin.
		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null) {
			return;
		}
		final String playerName = localPlayer.getName();
		final String examineText = config.getExamineText();
		if (examineText.isEmpty() || examineText.isBlank()) {
			return;
		}
		final String trimmedText = examineText.length() > MAX_TEXT_LENGTH ? examineText.substring(0, MAX_TEXT_LENGTH) : examineText;
		executor.execute(() ->
		{
			try
			{
				chatClient.submitTask(
						String.format("%s%s", REDIS_KEY_PREFIX, playerName),
						trimmedText,
						50,
						100,
						"Atlantis");
			}
			catch (Exception ex)
			{
				log.warn("Unable to set examine text");
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
			log.warn(String.format("Unable to get examine text for player %s", playerName));
			return getTextNotFoundMessage();
		}

		if (task == null || task.getTask() == null || task.getTask().isEmpty()) {
			return getTextNotFoundMessage();
		}

		return task.getTask();
	}

	private String getTextNotFoundMessage() {
		int val = new Random().nextInt(3);
		switch (val) {
			case 0:
				return "After observing the player closely, you don't notice anything of interest.";
			case 1:
				return "Upon close examination, you are able to deduce little of note about the player.";
			case 2:
			default:
				return "You squint and strain your ears, but the player exhibits no distinguishing characteristics or behavior.";
		}
	}

	@Provides
	ExaminePlayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExaminePlayerConfig.class);
	}
}
