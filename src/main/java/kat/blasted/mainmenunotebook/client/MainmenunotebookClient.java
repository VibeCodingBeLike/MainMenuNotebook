package kat.blasted.mainmenunotebook.client;

import kat.blasted.mainmenunotebook.storage.StickyNoteManager;
import net.fabricmc.api.ClientModInitializer;

public class MainmenunotebookClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Pre-load sticky notes config
        StickyNoteManager.getInstance().load();
    }
}
