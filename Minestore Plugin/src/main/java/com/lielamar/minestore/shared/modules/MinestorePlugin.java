package com.lielamar.minestore.shared.modules;

import com.lielamar.minestore.shared.encryption.EncryptionKey;
import com.lielamar.minestore.shared.handlers.PlayerHandler;
import com.lielamar.minestore.shared.handlers.RequestHandler;
import com.lielamar.minestore.shared.handlers.SocketServerHandler;

import javax.annotation.Nullable;

public interface MinestorePlugin {

    @Nullable EncryptionKey getEncryptionKey();
    RequestHandler getRequestHandler();
    SocketServerHandler getSocketServerHandler();
    PlayerHandler getPlayerHandler();

}
