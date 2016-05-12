package haven;

import java.util.*;
import java.util.regex.*;

public class Game extends Widget {

    // Let's pretend that gameui() method will never return null
    // (there are a lot of such assumptions in the client's code anyway)

    // Code style is really a mess here...
    // This is a result of the mixed code style in other files
    // and the fact that I want JS-styled camelCase API functions

    /////////////////////////////
    // API functions
    /////////////////////////////

    public boolean studyCurio(String curioName) {

        WItem curiowitm = getCharInventoryWItem(curioName);
        if (curiowitm == null) {
            return false;
        }

        WItem studywitem = getStudyInventoryWItem(curioName);
        if (studywitem != null) {
            return false;
        }

        Inventory studyinvwdg = getStudyInventoryWidget();

        Coord freeCoords = getFreeCoords(studywitem, studyinvwdg);
        if (freeCoords == null) {
            return false;
        }

        curiowitm.item.wdgmsg("take", curiowitm.c);
        studyinvwdg.drop(Coord.z, freeCoords);
        return true;

    }

    public boolean dropItemFromHandToWindow(String windowName) {

        GameUI gui = gameui();

        if (gui.vhand == null) {
            return false;
        }

        Inventory destinvwdg = getInventoryWidget(windowName);
        if (destinvwdg == null) {
            return false;
        }

        Coord freeCoords = getFreeCoords(gui.vhand, destinvwdg);
        if (freeCoords == null) {
            return false;
        }

        gui.vhand.item.wdgmsg("take", gui.vhand.c);
        destinvwdg.drop(Coord.z, freeCoords);
        return true;

    }

    public String[] getInvItems() {

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return null;
        }

        List<String> invitems = new ArrayList<String>();

        List<WItem> charinvwitems = getInventoryWItems(charinvwdg);
        for (WItem witm : charinvwitems) {
            String witmbasename = getWItemBaseName(witm);
            if (witmbasename != null) {
                invitems.add(witmbasename);
            }
        }

        return invitems.toArray(new String[invitems.size()]);

    }

    public Coord getPlayerCoords() {

        GameUI gui = gameui();
        Gob player = gui.map.player();
        return player.rc;

    }

    public void goTo(int x, int y) {

        GameUI gui = gameui();
        gui.map.pfLeftClick(new Coord(x, y), null);

    }

    public boolean pickItem(long id) {

        Config.autopick = true;

        return mapObjectRightClick(id);

    }

    public void travelToHearthFire() {

        gameui().menu.wdgmsg("act", new Object[]{"travel", "hearth"});

    }

    public boolean liftObject(long id) {

        GameUI gui = gameui();

        gui.menu.wdgmsg("act", new Object[]{"carry"});

        waitForHandCursor(1000);

        Gob gob = findGobWithId(id);
        if (gob == null) {
            return false;
        }

        // Actually pfRightClick function can do both left and right clicks depending on the arguments
        gui.map.pfRightClick(
            gob,
            -1, // meshid -- should be -1,
                // (unless we want to click house doors -- then we'll need to pass a correct id for the door's mesh)
            1,  // clickb -- either 1 for left mouse button or 3 for right
            0,  // modflags -- 0, or 1 if we want to simulate clicking with the Shift key pressed
                // (useful when putting things into a stockpile for example)
            null
        );

        return true;

    }

    public void mapRightClick(int x, int y) {

        // Unfortunately, there's no way to use MapView::pfRightClick function w/o passing a Gob object
        // and pathfinder doesn't work in such cases like placing a boat

        gameui().map.wdgmsg(
            "click",
            Coord.z,
            new Coord(x, y),
            3, // mouse button (1 for left, 2 for right)
            0  // modflags (0 -- no Shift or such stuff)
        );

    }

    public MapObject[] getMapObjects(String name) {

        List<MapObject> mapObjects = new ArrayList<MapObject>();

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                Resource res = gob.getres();
                if (res != null) {
                    if (res.basename().equals(name)) {
                        mapObjects.add(new MapObject(res.basename(), res.name, gob.id, gob.rc));
                    }
                }

            }
        }

        return mapObjects.toArray(new MapObject[mapObjects.size()]);

    }

    public MapObject[] getMapObjectsByFullName(String fullName) {

        List<MapObject> mapObjects = new ArrayList<MapObject>();

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                Resource res = gob.getres();
                if (res != null) {
                    if (res.name.equals(fullName)) {
                        mapObjects.add(new MapObject(res.basename(), res.name, gob.id, gob.rc));
                    }
                }

            }
        }

        return mapObjects.toArray(new MapObject[mapObjects.size()]);

    }

    public MapObject[] getAllMapObjects() {

        List<MapObject> mapObjects = new ArrayList<MapObject>();

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                Resource res = gob.getres();
                if (res != null) {
                    mapObjects.add(new MapObject(res.basename(), res.name, gob.id, gob.rc));
                }

            }
        }

        return mapObjects.toArray(new MapObject[mapObjects.size()]);

    }

    public boolean mapObjectRightClick(long id) {

        Gob gob = findGobWithId(id);
        if (gob == null) {
            return false;
        }

        gobRightClick(gob);

        return true;

    }

    public String[] getItemsFrom(String windowName) {

        Inventory invwdg = getInventoryWidget(windowName);
        if (invwdg == null) {
            return null;
        }

        List<String> invitems = new ArrayList<String>();

        List<WItem> invwitems = getInventoryWItems(invwdg);
        for (WItem witm : invwitems) {
            String witmbasename = getWItemBaseName(witm);
            if (witmbasename != null) {
                invitems.add(witmbasename);
            }
        }

        return invitems.toArray(new String[invitems.size()]);

    }

    public boolean transferItemFrom(String windowName, String itemBaseName) {

        Inventory invwdg = getInventoryWidget(windowName);
        if (invwdg == null) {
            return false;
        }

        WItem witm = getInventoryWItem(invwdg, itemBaseName);
        if (witm == null) {
            return false;
        }

        transferWItem(witm);

        return true;

    }

    public boolean transferItemsFrom(String windowName, String itemsBaseName) {

        Inventory invwdg = getInventoryWidget(windowName);
        if (invwdg == null) {
            return false;
        }

        WItem witm = getInventoryWItem(invwdg, itemsBaseName);
        if (witm == null) {
            return false;
        }

        transferIdenticalWItems(witm);

        return true;

    }

    public boolean transferItem(String itemBaseName) {

        WItem witm = getCharInventoryWItem(itemBaseName);
        if (witm == null) {
            return false;
        }

        transferWItem(witm);

        return true;

    }

    public boolean transferItems(String itemsBaseName) {

        WItem witm = getCharInventoryWItem(itemsBaseName);
        if (witm == null) {
            return false;
        }

        transferIdenticalWItems(witm);

        return true;

    }

    public void quit() {

        System.exit(0);

    }

    public void logout() {

        GameUI gui = gameui();
        if (gui == null) {
            return;
        }

        gui.act("lo");

        if (gui.map != null) {
            gui.map.canceltasks();
        }

    }

    public AttentionInfo getCharAttentionInfo() {

        CharWnd.StudyInfo studyInfoWdg = getStudyInfoWdg();
        if (studyInfoWdg == null) {
            return null;
        }

        int maxAttention = ui.sess.glob.cattr.get("int").comp;

        int usedAttention = 0;
        for (GItem item : studyInfoWdg.study.children(GItem.class)) {
            try {
                Curiosity ci = ItemInfo.find(Curiosity.class, item.info());
                if (ci != null) {
                    usedAttention += ci.mw;
                }
            } catch (Loading l) {
            }
        }

        return new AttentionInfo(maxAttention, usedAttention);

    }

    public void drink() {

        GameUI gui = gameui();
        Window eqwnd = gui.getwnd("Equipment");
        if (eqwnd == null) {
            return;
        }

        String[] waterContainersBaseNames = { "bucket-water", "waterskin", "waterflask", "kuksa-full" };

        for (Widget firstlvlwdg = eqwnd.lchild; firstlvlwdg != null; firstlvlwdg = firstlvlwdg.prev) {
            for (Widget secondlvlwdg = firstlvlwdg.lchild; secondlvlwdg != null; secondlvlwdg = secondlvlwdg.prev) {
                if (secondlvlwdg instanceof WItem) {
                    WItem witm = (WItem) secondlvlwdg;

                    String witmBaseName = getWItemBaseName(witm);
                    if (witmBaseName == null) {
                        continue;
                    }

                    if (Arrays.asList(waterContainersBaseNames).contains(witmBaseName)) {

                        drink(witm);

                        Integer stam = getStamina();
                        if (stam == 100) {
                            return;
                        }

                    }
                }
            }
        }

        for (String waterContainerBaseName : waterContainersBaseNames) {
            List<WItem> waterContainers = getCharInventoryWItems(waterContainerBaseName);
            if (waterContainers == null) {
                continue;
            }

            for (WItem waterContainer : waterContainers) {

                drink(waterContainer);

                Integer stam = getStamina();
                if (stam == 100) {
                    return;
                }

            }
        }

    }

    public HPInfo getHP() {

        HPInfo hpinfo = new HPInfo(-1, -1);

        List<IMeter.Meter> meters = gameui().getmeters("hp");
        if (meters == null) {
            return hpinfo;
        }

        hpinfo.hhp = meters.get(0).a;
        hpinfo.shp = meters.get(1).a;

        return hpinfo;

    }

    public int getStamina() {

        IMeter.Meter stam = gameui().getmeter("stam", 0);
        if (stam == null) {
            return -1;
        }

        return stam.a;

    }

    public int getEnergy() {

        IMeter.Meter nrj = gameui().getmeter("nrj", 0);
        if (nrj == null) {
            return -1;
        }

        return nrj.a;

    }

    public boolean eat(String itemBaseName) {

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return false;
        }

        WItem witm = getInventoryWItem(charinvwdg, itemBaseName);
        if (witm == null) {
            return false;
        }

        eat(witm);

        return true;

    }

    public boolean eatFrom(String windowName, String itemBaseName) {

        Inventory invwdg = getInventoryWidget(windowName);
        if (invwdg == null) {
            return false;
        }

        WItem witm = getInventoryWItem(invwdg, itemBaseName);
        if (witm == null) {
            return false;
        }

        eat(witm);

        return true;

    }

    public boolean takeItemFromStockpile() {

        Window stockpilewnd = gameui().getwnd("Stockpile");
        if (stockpilewnd == null) {
            return false;
        }

        ISBox isb = getISBox(stockpilewnd);
        if (isb == null) {
            return false;
        }

        isb.wdgmsg("xfer");

        return true;

    }

    public ISBox.ISBoxInfo getStockpileInfo() {

        Window stockpilewnd = gameui().getwnd("Stockpile");
        if (stockpilewnd == null) {
            return null;
        }

        ISBox isb = getISBox(stockpilewnd);
        if (isb == null) {
            return null;
        }

        return isb.getISBoxInfo();

    }

    public boolean createStockpileWithItem(String itemBaseName, int x, int y) {

        WItem witm = getCharInventoryWItem(itemBaseName);
        if (witm == null) {
            return false;
        }

        // I think that there should be a better way to do it
        // but this is what I can think of at the moment

        GItem itm = witm.item;

        itm.wdgmsg("take", witm.c);

        try {

            Thread.sleep(500);

            GameUI gui = gameui();

            gui.map.wdgmsg("itemact", Coord.z, new Coord(x, y), 0); // 0 for modflags (no Shift or such stuff)

            Thread.sleep(500);

            gui.map.wdgmsg(
                "place",
                new Coord(x, y),
                -180, // direction
                1,    // mouse button (1 for left, 3 for right)
                0     // modflags (0 -- no Shift or such stuff)
            );

        } catch (InterruptedException ie) {
            return false;
        }

        return true;

    }

    public String[] getEquippedItems() {

        Equipory equipory = gameui().getequipory();
        if (equipory == null) {
            return null;
        }

        List<String> equippedItems = new ArrayList<String>();

        for (Widget wdg = equipory.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof WItem) {
                WItem witm = (WItem) wdg;

                String witmBaseName = getWItemBaseName(witm);
                if (witmBaseName == null) {
                    continue;
                }

                equippedItems.add(witmBaseName);
            }
        }

        return equippedItems.toArray(new String[equippedItems.size()]);

    }

    public boolean equipItem(String itemBaseName) {

        WItem witmtoequip = getCharInventoryWItem(itemBaseName);
        if (witmtoequip == null) {
            return false;
        }

        Equipory equipory = gameui().getequipory();
        if (equipory == null) {
            return false;
        }

        witmtoequip.item.wdgmsg("take", witmtoequip.c);

        equipory.wdgmsg("drop", -1);

        return true;

    }

    public boolean unequipItem(String itemBaseName) {

        Equipory equipory = gameui().getequipory();
        if (equipory == null) {
            return false;
        }

        for (Widget wdg = equipory.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof WItem) {
                WItem witm = (WItem) wdg;

                String witmBaseName = getWItemBaseName(witm);
                if (witmBaseName == null) {
                    continue;
                }

                if (witmBaseName.equals(itemBaseName)) {
                    witm.item.wdgmsg("transfer", witm.c);
                    return true;
                }
            }
        }

        return false;

    }

    public boolean takeItem(String itemBaseName) {

        WItem witm = getCharInventoryWItem(itemBaseName);
        if (witm == null) {
            return false;
        }

        witm.item.wdgmsg("take", witm.c);

        return true;

    }

    public boolean takeItemFromWindow(String windowName, String itemBaseName) {

        List<Inventory> invwdgs = getInventoryWidgets(windowName);
        if (invwdgs == null) {
            return false;
        }

        for (Inventory invwdg : invwdgs) {
            WItem witm = getInventoryWItem(invwdg, itemBaseName);
            if (witm != null) {
                witm.item.wdgmsg("take", witm.c);
                return true;
            }
        }

        return false;

    }

    public void waitForPf() {

        try {
            gameui().map.pfthread.join();
        } catch (InterruptedException e) {
            // ignored
        }

    }

    public boolean sendAreaChatMessage(String msg) {

        return sendMessageToChat("Area Chat", msg);

    }

    public boolean sendPartyChatMessage(String msg) {

        return sendMessageToChat("Party", msg);

    }

    public boolean sendVillageChatMessage(String msg) {

        return sendMessageToChat("Village", msg);

    }

    public boolean sendPrivateChatMessage(String to, String msg) {

        GameUI gui = gameui();
        for (BuddyWnd.Buddy buddy : gui.buddies) {
            if (buddy.name.equals(to)) {
                buddy.chat();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            return false;
        }

        return sendMessageToChat(to, msg);

    }

    public int getSpeed() {

        Speedget speedwdg = getSpeedWdg();
        if (speedwdg == null) {
            return -1;
        }

        return speedwdg.cur;

    }

    public boolean setSpeed(int speed) {

        if (speed < 0) {
            return false;
        }

        Speedget speedwdg = getSpeedWdg();
        if (speedwdg == null) {
            return false;
        }

        if (speed > speedwdg.max) {
            return false;
        }

        speedwdg.set(speed);
        return true;

    }

    public boolean useMenuAction(String[] hotkeys) {

        GameUI gui = gameui();

        Map<Character, Glob.Pagina> hotmap = gui.menu.gethotmap();
        for (String hotkey : hotkeys) {

            Glob.Pagina pagina = hotmap.get(Character.toUpperCase(hotkey.charAt(0)));
            if (pagina == null) {
                gui.menu.reset();
                return false;
            }

            gui.menu.use(pagina, true);

            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                gui.menu.reset();
                return false;
            }

        }

        gui.menu.reset();
        return true;

    }

    public boolean craft(String itemBaseName) {

        gameui().menu.wdgmsg("act", new Object[]{"craft", itemBaseName});

        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            return false;
        }

        Window craftwnd = gameui().getwnd("Crafting");
        if (craftwnd == null) {
            return false;
        }

        craftwnd.lchild.wdgmsg("make", 0); // 0 -- "Craft", 1 -- "Craft all"

        return true;

    }

    public boolean craftAll(String itemBaseName) {

        gameui().menu.wdgmsg("act", new Object[]{"craft", itemBaseName});

        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            return false;
        }

        Window craftwnd = gameui().getwnd("Crafting");
        if (craftwnd == null) {
            return false;
        }

        craftwnd.lchild.wdgmsg("make", 1); // 0 -- "Craft", 1 -- "Craft all"

        return true;

    }

    public boolean chooseFlowerMenuOption(String option) {

        for (Widget wdg = ui.root.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof FlowerMenu) {
                FlowerMenu flmenu = (FlowerMenu) wdg;
                return flmenu.chooseoptwithname(option);
            }
        }

        return false;

    }

    public boolean dropItem(String itemBaseName) {

        WItem witm = getCharInventoryWItem(itemBaseName);
        if (witm == null) {
            return false;
        }

        witm.item.wdgmsg("drop", Coord.z);

        return true;

    }

    public boolean dropItemFromHand() {

        GameUI gui = gameui();

        if (gui.vhand == null) {
            return false;
        }

        gui.map.wdgmsg("drop", Coord.z, gui.map.player().rc, 0);

        return true;

    }

    public boolean useItemFromHandOnObject(long id) {

        GameUI gui = gameui();

        if (gui.vhand == null) {
            return false;
        }

        Gob gob = findGobWithId(id);
        if (gob == null) {
            return false;
        }

        gui.map.wdgmsg(
            "itemact",
            Coord.z,
            gob.rc,
            0, // 0 for modflags (no Shift or such stuff)
            0,
            (int) gob.id,
            gob.rc,
            0, // Overlay ID
            -1 // Mesh ID
        );

        return true;

    }

    public boolean useItemFromHandOnCoords(int x, int y) {

        GameUI gui = gameui();

        if (gui.vhand == null) {
            return false;
        }

        gui.map.wdgmsg("itemact", Coord.z, new Coord(x, y), 0);

        return true;

    }

    public String getBarrelContent(long id) {

        Gob gob = findGobWithId(id);
        if (gob == null) {
            return null;
        }

        Resource res = gob.getres();
        if (res == null || !res.basename().equals("barrel")) {
            return null;
        }

        for (Gob.Overlay ol : gob.ols) {
            Resource olRes = ol.res.get();
            if (olRes == null) {
                continue;
            }

            String olResBaseName = olRes.basename();
            if (olResBaseName.startsWith("barrel")) {
                return olResBaseName.substring(olResBaseName.indexOf("-") + 1);
            }
        }

        return "empty";

    }

    public Double getBarrelLiters(long id) {

        Gob gob = findGobWithId(id);
        if (gob == null) {
            return null;
        }

        Resource res = gob.getres();
        if (res == null || !res.basename().equals("barrel")) {
            return null;
        }

        if (!mapObjectRightClick(id)) {
            return null;
        }

        Window barrelwnd = waitForWindow("Barrel", 5000);
        if (barrelwnd == null) {
            return null;
        }

        for (Widget wdg = barrelwnd.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof Label) {
                Label label = (Label) wdg;
                String barrelContentText = label.text.text;

                // Some examples:
                // Contents: Empty.
                // Contents: 22.90 l of Water.
                // Contents: 16.33 l of Honey.
                // Contents: 0.91 l of Sheepsmilk.
                Pattern p = Pattern.compile("Contents: ([\\d\\.]+).*");
                Matcher m = p.matcher(barrelContentText);
                if (m.find()) {
                    System.out.println(m.group(1));
                    return Double.parseDouble(m.group(1));
                }
            }
        }

        return 0.0;

    }

    public int getGrowthStage(long id) {

        Gob gob = findGobWithId(id);
        if (gob == null) {
            return -1;
        }

        ResDrawable rd = gob.getattr(ResDrawable.class);
        if (rd == null || rd.sdt.eom()) {
            return -1;
        }

        Resource res = gob.getres();
        if (res == null) {
            return -1;
        }

        int stage = rd.sdt.peekrbuf(0);
        if (res.name.startsWith("gfx/terobjs/plants") && !res.name.endsWith("trellis")) {
            return stage + 1;
        } else if (res.name.startsWith("gfx/terobjs/trees") || res.name.startsWith("gfx/terobjs/bushes")) {
            return stage;
        }

        return -1;

    }

    public MapObject[] getHighlightedMapObjects() {

        List<MapObject> mapObjects = new ArrayList<MapObject>();

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                GobHighlight highlight = gob.getattr(GobHighlight.class);
                if (highlight == null) {
                    continue;
                }

                Resource res = gob.getres();
                if (res == null) {
                    continue;
                }

                mapObjects.add(new MapObject(res.basename(), res.name, gob.id, gob.rc));

            }
        }

        return mapObjects.toArray(new MapObject[mapObjects.size()]);

    }

    public void waitForTaskToFinish() {

        GameUI gui = gameui();
        try {
            while (gui.prog >= 0) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            // ignored
        }

    }

    public boolean feastEat(String itemBaseName) {

        if (!activateFeast()) {
            return false;
        }

        if (!waitForForkCursor(1000)) {
            return false;
        }

        return takeItemFromWindow("Table", itemBaseName);

    }

    public boolean openTable(long id) {

        // pfRightClick function doesn't work with tables

        Gob gob = findGobWithId(id);
        if (gob == null) {
            return false;
        }

        GameUI gui = gameui();

        if (gob.ols != null && gob.ols.size() > 0) {
            Gob.Overlay ol = gob.ols.iterator().next();

            gui.map.wdgmsg(
                    "click",        // Action
                    Coord.z,        // Actually we need to pass gob's window coordinates as the second argument (i.e., gob.sc)
                                    // but Coord.z works too
                    gob.rc,         // Actual coord we clicked
                    3,              // Mouse button (3 for RMB)
                    0,              // Modflags (0 for nothing)
                    1,              // It seems that this argument tells server whether we clicked gob with overlay or not
                                    // 0 -- without overlay, 1 -- with overlay
                    (int) gob.id,   // Gob's ID
                                    // We need to cast it from long to int. Otherwise we will get "java.lang.RuntimeException: Cannot encode a class java.lang.Long as TTO"
                    gob.rc,         // Gob's coords
                    ol.id,          // Overlay ID
                    -1              // Meshid (should be -1 for non-door gobs)
            );
        } else {
            gui.map.wdgmsg("click", Coord.z, gob.rc, 3, ui.modflags(), 0, (int) gob.id, gob.rc, 0, -1);
        }

        return true;

    }

    public Rect getCharInvSize() {

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return null;
        }

        int width = charinvwdg.sz.x / 33;
        int height = charinvwdg.sz.y / 33;
        return new Rect(width, height);

    }

    public int[][] getCharInvCellsUsageMatrix() {

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return null;
        }

        List<WItem> charinvwitms = getCharInventoryWItems();
        if (charinvwitms == null) {
            return null;
        }

        Coord cellsz = new Coord(33, 33);

        int[][] used = new int[charinvwdg.sz.x / cellsz.x][charinvwdg.sz.y / cellsz.y];

        for (WItem witm : charinvwitms) {

            Coord topLeftCoords = new Coord(witm.c.x / 33, witm.c.y / 33);
            Coord bottomRightCoords = new Coord(
                    (witm.c.x + witm.sz.x - 2) / 33,
                    (witm.c.y + witm.sz.y - 2) / 33
            );
            for (int x = topLeftCoords.x; x <= bottomRightCoords.x; ++x) {
                for (int y = topLeftCoords.y; y <= bottomRightCoords.y; ++y) {
                    used[x][y] = 1;
                }
            }

        }

        return used;

    }

    public int getFreeCharInvCellsCount() {

        int[][] charInvCellsUsageMatrix = getCharInvCellsUsageMatrix();
        if (charInvCellsUsageMatrix == null) {
            return -1;
        }

        int freeCharInvCellsCount = 0;
        for (int x = 0; x < charInvCellsUsageMatrix.length; ++x) {
            for (int y = 0; y < charInvCellsUsageMatrix[0].length; ++y) {
                if (charInvCellsUsageMatrix[x][y] == 0) {
                    ++freeCharInvCellsCount;
                }
            }
        }

        return freeCharInvCellsCount;

    }

    /////////////////////////////
    // API-related classes
    /////////////////////////////

    public class AttentionInfo {

        public AttentionInfo(int max, int used) {
            this.max = max;
            this.used = used;
        }

        public int max;
        public int used;

    }

    static public class MapObject {

        public MapObject(String name, String fullName, long id, Coord coords) {
            this.name = name;
            this.fullName = fullName;
            this.id = id;
            this.coords = coords;
        }

        public String name;
        public String fullName;
        public long id;
        public Coord coords;

    }

    public static class HPInfo {

        public HPInfo(int shp, int hhp) {
            this.shp = shp;
            this.hhp = hhp;
        }

        public int shp;
        public int hhp;

    }

    public static class Rect {

        public Rect(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int width;
        public int height;

    }

    /////////////////////////////
    // Internal helper classes
    /////////////////////////////

    private class Box {

        public Box(Coord topleft, Coord bottomright) {
            this.topleft = topleft;
            this.bottomright = bottomright;
        }

        public Coord topleft;
        public Coord bottomright;

    }

    /////////////////////////////
    // Internal helper functions
    /////////////////////////////

    private boolean boxesIntersect(Box lhs, Box rhs) {

        boolean intersect =
               lhs.topleft.x < rhs.bottomright.x
            && lhs.bottomright.x > rhs.topleft.x
            && lhs.topleft.y < rhs.bottomright.y
            && lhs.bottomright.y > rhs.topleft.y;
        return intersect;

    }

    private Gob findGobWithId(long id) {

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                if (gob.id == id) {

                    return gob;

                }

            }
        }

        return null;

    }

    private void gobRightClick(Gob gob) {

        GameUI gui = gameui();
        gui.map.pfRightClick(
            gob, // Gob object to click on
            -1,  // meshid -- should be -1,
                 // (unless we want to click house doors -- then we'll need to pass a correct id for the door's mesh)
            3,   // clickb -- either 1 for left mouse button or 3 for right
            0,   // modflags -- 0, or 1 if we want to simulate clicking with the Shift key pressed
                 // (useful when putting things into a stockpile for example)
            null
        );

    }

    private WItem getCharInventoryWItem(String itemBaseName) {

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return null;
        }

        WItem witm = getInventoryWItem(charinvwdg, itemBaseName);
        if (witm == null) {
            return null;
        }

        return witm;

    }

    private WItem getStudyInventoryWItem(String itemBaseName) {

        Inventory studyinvwdg = getStudyInventoryWidget();
        if (studyinvwdg == null) {
            return null;
        }

        WItem witm = getInventoryWItem(studyinvwdg, itemBaseName);
        if (witm == null) {
            return null;
        }

        return witm;

    }

    private Inventory getCharInventoryWidget() {

        Window invwnd = gameui().getwnd("Inventory");
        if (invwnd == null) {
            return null;
        }

        for (Widget invwdg = invwnd.lchild; invwdg != null; invwdg = invwdg.prev) {
            if (invwdg instanceof Inventory) {
                return (Inventory) invwdg;
            }
        }

        return null;

    }

    private Inventory getStudyInventoryWidget() {

        Window charsheet = gameui().getwnd("Character Sheet");
        if (charsheet == null) {
            return null;
        }

        for (Widget firstlvlwdg = charsheet.lchild; firstlvlwdg != null; firstlvlwdg = firstlvlwdg.prev) {
            for (Widget secondlvlwdg = firstlvlwdg.lchild; secondlvlwdg != null; secondlvlwdg = secondlvlwdg.prev) {
                if (secondlvlwdg instanceof Inventory && secondlvlwdg.parent instanceof Tabs.Tab) {
                    return (Inventory) secondlvlwdg;
                }
            }
        }

        return null;

    }

    private Inventory getInventoryWidget(String windowName) {

        Window invwnd = gameui().getwnd(windowName);
        if (invwnd == null) {
            return null;
        }

        for (Widget wdg = invwnd.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof Inventory) {
                return (Inventory) wdg;
            }
        }

        return null;

    }

    private List<Inventory> getInventoryWidgets(String windowName) {

        Window invwnd = gameui().getwnd(windowName);
        if (invwnd == null) {
            return null;
        }

        List<Inventory> invwdgs = new ArrayList<Inventory>();
        for (Widget wdg = invwnd.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof Inventory) {
                invwdgs.add((Inventory) wdg);
            }
        }

        return invwdgs;

    }

    private CharWnd.StudyInfo getStudyInfoWdg() {

        Window charsheet = gameui().getwnd("Character Sheet");
        if (charsheet == null) {
            return null;
        }

        for (Widget firstlvlwdg = charsheet.lchild; firstlvlwdg != null; firstlvlwdg = firstlvlwdg.prev) {
            for (Widget secondlvlwdg = firstlvlwdg.lchild; secondlvlwdg != null; secondlvlwdg = secondlvlwdg.prev) {
                if (secondlvlwdg instanceof CharWnd.StudyInfo && secondlvlwdg.parent instanceof Tabs.Tab) {
                    return (CharWnd.StudyInfo) secondlvlwdg;
                }
            }
        }

        return null;

    }

    private List<WItem> getCharInventoryWItems() {

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return null;
        }

        return getInventoryWItems(charinvwdg);

    }

    private List<WItem> getCharInventoryWItems(String itemsBaseName) {

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return null;
        }

        return getInventoryWItems(charinvwdg, itemsBaseName);

    }

    private List<WItem> getStudyInventoryWItems() {

        Inventory studyinvwdg = getStudyInventoryWidget();
        if (studyinvwdg == null) {
            return null;
        }

        return getInventoryWItems(studyinvwdg);

    }

    private List<WItem> getStudyInventoryWItems(String itemsBaseName) {

        Inventory studyinvwdg = getStudyInventoryWidget();
        if (studyinvwdg == null) {
            return null;
        }

        return getInventoryWItems(studyinvwdg, itemsBaseName);

    }

    private List<WItem> getInventoryWItems(Inventory invwdg) {

        List<WItem> witems = new ArrayList<WItem>();

        for (Widget witm = invwdg.lchild; witm != null; witm = witm.prev) {
            if (witm instanceof WItem) {
                witems.add((WItem) witm);
            }
        }

        return witems;

    }

    private List<WItem> getInventoryWItems(Inventory invwdg, String itemsbasename) {

        List<WItem> witems = new ArrayList<WItem>();

        for (Widget witm = invwdg.lchild; witm != null; witm = witm.prev) {
            if (witm instanceof WItem) {

                WItem item = (WItem) witm;
                String witmbasename = getWItemBaseName(item);
                if (witmbasename != null) {
                    if (witmbasename.equals(itemsbasename)) {
                        witems.add(item);
                    }
                }

            }
        }

        return witems;

    }

    private WItem getInventoryWItem(Inventory invwdg, String itemBaseName) {

        for (Widget witm = invwdg.lchild; witm != null; witm = witm.prev) {
            if (witm instanceof WItem) {

                WItem item = (WItem) witm;
                String witmbasename = getWItemBaseName(item);
                if (witmbasename != null) {
                    if (witmbasename.equals(itemBaseName)) {
                        return item;
                    }
                }

            }
        }

        return null;

    }

    private String getWItemBaseName(WItem witm) {

        GItem ngitm = witm.item;
        Resource nres = ngitm.resource();
        if (nres == null) {
            return null;
        }
        return nres.basename();

    }

    private void transferWItem(WItem witm) {

        witm.item.wdgmsg("transfer", Coord.z);

    }

    private void transferIdenticalWItems(WItem witm) {

        witm.item.wdgmsg("transfer-identical", witm.item);

    }

    private void drink(WItem waterContainer) {

        Config.autodrink = true;

        waterContainer.item.wdgmsg("iact", waterContainer.c, 0); // 0 for modflags (no Shift or similar things)

        GameUI gui = gameui();

        try {
            Thread.sleep(1000);
            while (gui.prog >= 0) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            return;
        }

    }

    private void eat(WItem foodWitm) {

        Config.autoeat = true;

        foodWitm.item.wdgmsg("iact", foodWitm.c, 0); // 0 for modflags (no Shift or similar things)

    }

    private ISBox getISBox(Window wnd) {

        for (Widget w = wnd.lchild; w != null; w = w.prev) {
            if (w instanceof ISBox) {
                return (ISBox) w;
            }
        }

        return null;

    }

    private ChatUI.EntryChannel getChat(String chatName) {

        GameUI gui = gameui();
        for (Widget wdg = gui.chat.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof ChatUI.EntryChannel) {
                ChatUI.EntryChannel chat = (ChatUI.EntryChannel) wdg;
                if (chat.name().equals(chatName)) {
                    return chat;
                }
            }
        }

        return null;

    }

    private boolean sendMessageToChat(String chatName, String msg) {

        ChatUI.EntryChannel areaChat = getChat(chatName);
        if (areaChat == null) {
            return false;
        }

        areaChat.send(msg);

        return true;

    }

    private boolean waitForHandCursor(long timeoutMillisecs) {
        return waitForCursor("gfx/hud/curs/hand", timeoutMillisecs);
    }

    private boolean waitForForkCursor(long timeoutMillisecs) {
        return waitForCursor("gfx/hud/curs/eat", timeoutMillisecs);
    }

    private boolean waitForCursor(String cursorName, long timeoutMillisecs) {

        long millisecsPassed = 0;

        Resource curs = null;
        do {

            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                return false;
            }

            millisecsPassed += 50;
            if (millisecsPassed >= timeoutMillisecs) {
                return false;
            }

            curs = ui.root.getcurs();

        } while (curs == null || !curs.name.equals(cursorName));

        return true;

    }

    private Window waitForWindow(String windowName, long timeoutMillisecs) {

        long millisecsPassed = 0;

        Window barrelwnd = null;
        do {

            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                return null;
            }

            millisecsPassed += 50;
            if (millisecsPassed >= timeoutMillisecs) {
                return null;
            }

            barrelwnd = gameui().getwnd(windowName);

        } while (barrelwnd == null);

        return barrelwnd;

    }

    private Speedget getSpeedWdg() {

        GameUI gui = gameui();

        for (Widget firstlvlwdg = gui.lchild; firstlvlwdg != null; firstlvlwdg = firstlvlwdg.prev) {
            for (Widget secondlvlwdg = firstlvlwdg.lchild; secondlvlwdg != null; secondlvlwdg = secondlvlwdg.prev) {
                if (secondlvlwdg instanceof Speedget) {
                    return (Speedget) secondlvlwdg;
                }
            }
        }

        return null;

    }

    private Coord getFreeCoords(WItem witm, Inventory invwdg) {

        List<WItem> invwitems = getInventoryWItems(invwdg);
        if (invwitems.isEmpty()) {
            return Coord.z;
        }

        Coord invtilesz = new Coord(33, 33);
        for (int x = 1; x < invwdg.sz.x; x += invtilesz.x) {
            for (int y = 1; y < invwdg.sz.y; y += invtilesz.y) {

                if (x + witm.sz.x > invwdg.sz.x
                        || y + witm.sz.y > invwdg.sz.y) {
                    continue;
                }

                boolean isIntersect = false;
                for (WItem invwitm : invwitems) {

                    Box witmbox = new Box(
                            new Coord(invwitm.c.x, invwitm.c.y),
                            new Coord(invwitm.c.x + invwitm.sz.x, invwitm.c.y + invwitm.sz.y)
                    );
                    Box desiredbox = new Box(
                            new Coord(x, y),
                            new Coord(x + witm.sz.x, y + witm.sz.y)
                    );

                    if (boxesIntersect(witmbox, desiredbox)) {
                        isIntersect = true;
                        break;
                    }

                }

                if (!isIntersect) {
                    return new Coord(x, y);
                }

            }
        }

        return null;

    }

    private boolean activateFeast() {

        Window tablewnd = gameui().getwnd("Table");
        if (tablewnd == null) {
            return false;
        }

        for (Widget wdg = tablewnd.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof Button) {
                Button btn = (Button) wdg;
                btn.wdgmsg("activate");
                return true;
            }
        }

        return false;

    }

}
