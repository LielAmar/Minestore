package com.lielamar.minestore.shared.handlers.requests.types;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.minestore.bukkit.handlers.BukkitRequestHandler;
import com.lielamar.minestore.shared.handlers.requests.Request;
import com.lielamar.minestore.shared.modules.CustomPlayer;
import com.lielamar.minestore.shared.modules.MinestorePlugin;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public abstract class AuthenticationRequest extends Request {

    private final Timer timer;
    private String playerName;
    private UUID playerUUID;

    private TimerTask task;

    public AuthenticationRequest(MinestorePlugin plugin, Socket socket, int protocolVersion, int requestId, JSONObject data) {
        super(plugin, socket, protocolVersion, requestId, data);
        this.timer = new Timer();

        loadRequestByVersion();
    }

    @Override
    public void closeRequest() {
        super.closeRequest();

        if(task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void loadRequestByVersion() {
        switch(getProtocolVersion()) {
            default:
                playerName = getData().getString("player_name");
                playerUUID = UUID.fromString(SpigotUtils.fixUUID(getData().getString("player_uuid")));
                break;
        }
    }

    @Override
    public void runRequest() {
        this.task = new TimerTask() {

            Object targetPlayer = preTimer(playerUUID);
            CustomPlayer customPlayer = null;

            int time = 300;

            @Override
            public void run() {
                if(time <= 0 || getSocket().isClosed()) {
                    this.cancel();

                    getPlugin().getRequestHandler().removeRequest(AuthenticationRequest.this);
                    return;
                }

                this.time--;

                if(targetPlayer != null) {
                    if(customPlayer == null) {
                        customPlayer = loadCustomPlayer(playerUUID);
                    } else {
                        if(customPlayer.hasAuthenticated()) {
                            try {
                                JSONObject response = buildResponse(getProtocolVersion(), getRequestId(), true);
                                BukkitRequestHandler.sendJSONToSocket(getSocket(), getPlugin().getEncryptionKey(), response);
                                sendMessage(targetPlayer, "§bYou have authenticated your current store purchase!");

                                // Resetting the player & letting them know
                                customPlayer.setAuthenticated(false);
                                postTimer(playerUUID);

                                // Canceling the timer
                                closeRequest();
                                timer.cancel();
                                cancel();
                            } catch(IOException exception) {
                                exception.printStackTrace();
                            }
                        }
                    }
                } else {
                    targetPlayer = preTimer(playerUUID);
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }


    /**
     * Builds a response (as a Json Object) using the protocol version, the request id and an authenticated statement.
     *
     * @param protocol_version   Version of the protocol
     * @param request_id         Id of the request
     * @param authenticated      Whether the player authenticated
     * @return                   A JSONObject object to send back to the socket
     */
    public static JSONObject buildResponse(int protocol_version, int request_id, boolean authenticated) {
        JSONObject response = new JSONObject();
        response.put("protocol_version", protocol_version);
        response.put("request_id", request_id);
        JSONObject response_data = new JSONObject();
        response_data.put("authenticated", authenticated);
        response.put("data", response_data);

        return response;
    }

    /**
     * Loads the custom player object from the given uuid object
     *
     * @param uuid   UUID of the target player to get the custom player object of
     * @return       CustomPlayer object
     */
    @Nullable
    public CustomPlayer loadCustomPlayer(UUID uuid) {
        CustomPlayer customPlayer = getPlugin().getPlayerHandler().getPlayer(uuid);

        if(customPlayer != null)
            customPlayer.setAuthenticated(false);

        return customPlayer;
    }


    @Nullable
    public abstract Object preTimer(UUID uuid);
    public abstract void postTimer(UUID uuid);
    public abstract void sendMessage(Object targetPlayer, String message);
}