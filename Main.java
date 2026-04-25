
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import static org.dreambot.api.methods.widget.Widgets.get;

@ScriptManifest(author = "Num5", name = "Edgeville Furnace", version = 2.0, description = "Glass Maker",
        category = Category.CRAFTING)
public class Main extends AbstractScript {
    private final Area bankArea = new Area(3098, 3498, 3095, 3494);
    private final Area smeltArea = new Area(3104, 3502, 3110, 3495);

    private enum State {
        SMELTING, BANK
    }

    @Override
    public int onLoop() {
        switch (getState()) {
            case BANK:
                if (!Players.getLocal().isMoving() && !Inventory.contains("Soda Ash") && !bankArea.contains(Players.getLocal())) {
                    Walking.walk(bankArea.getRandomTile());
                    Sleep.sleep(2500,3500);
                }

                if (bankArea.contains(Players.getLocal()) && !Bank.isOpen()) {
                    GameObject bankBooth = GameObjects.closest("Bank booth");
                    if (bankBooth != null && !Bank.isOpen() && !Players.getLocal().isMoving()) {
                        bankBooth.interact("Bank");
                        Sleep.sleepUntil(Bank::isOpen, 5000);
                        Sleep.sleep(500,1000);
                    }
                    Bank.depositAllItems();
                    Sleep.sleep(200,500);
                    Bank.withdraw("Bucket of sand", 14);
                    Bank.withdraw("Soda ash", 14);
                    if (Inventory.contains("Soda ash") && Inventory.contains("Bucket of sand")) {
                        Bank.close();
                        Sleep.sleep(500,1000);
                    }
                }
                break;

            case SMELTING:
                GameObject furnace = GameObjects.closest("Furnace");
                if (!smeltArea.contains(Players.getLocal().getTile())){
                    Walking.walk(smeltArea.getRandomTile());
                    Sleep.sleep(2500,3500);
                }
                while(Inventory.contains("Bucket of sand") && Inventory.contains("Soda ash")){
                    if (furnace != null &&  !Players.getLocal().isMoving() && !Players.getLocal().isAnimating()) {
                        furnace.interact("Smelt");
                    }
                    Sleep.sleep(1000,2000);
                    WidgetChild widget = Widgets.get(270, 14);
                    if (widget != null){
                        widget.interact("Make");
                        Sleep.sleepUntil(() -> Inventory.count("Molten glass") == 14, 19000);
                    }
                }
                break;
        }
        return (int) (Math.random() * (1500 - 1000) + 1000);  // Random sleep between 1000-1500 ms
    }

    private State getState () {
        // Check if Soda Ash is missing, and the player is not animating or in the bank area
        if (!Inventory.contains("Soda ash") && !Players.getLocal().isAnimating() && !bankArea.contains(Players.getLocal().getTile())) {
            return State.BANK;
        }
        // Check if both Soda Ash and Bucket of Sand are in inventory and player is not moving or already at smelt area
        else if (Inventory.contains("Soda ash") && Inventory.contains("Bucket of sand")) {
            return State.SMELTING;
        }
        // If no conditions are met, default to BANK or smelting area (based on your setup)
        else {
            return State.BANK;  // Default to banking if no other state is valid
        }
    }
}


