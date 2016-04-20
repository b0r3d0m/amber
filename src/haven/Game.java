package haven;

import java.util.*;

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

        List<WItem> studyinvwitems = getInventoryWItems(studyinvwdg);
        if (studyinvwitems.isEmpty()) {

            curiowitm.item.wdgmsg("take", curiowitm.c);
            studyinvwdg.drop(Coord.z, new Coord(0, 0));
            return true;

        }

        Coord invtilesz = new Coord(33, 33);
        for (int x = 1; x < studyinvwdg.sz.x; x += invtilesz.x) {
            for (int y = 1; y < studyinvwdg.sz.y; y += invtilesz.y) {

                if (x + curiowitm.sz.x > studyinvwdg.sz.x
                    || y + curiowitm.sz.y > studyinvwdg.sz.y) {
                    continue;
                }

                boolean isIntersect = false;
                for (WItem witm : studyinvwitems) {

                    Box witmbox = new Box(
                        new Coord(witm.c.x, witm.c.y),
                        new Coord(witm.c.x + witm.sz.x, witm.c.y + witm.sz.y)
                    );
                    Box desiredbox = new Box(
                        new Coord(x, y),
                        new Coord(x + curiowitm.sz.x, y + curiowitm.sz.y)
                    );

                    if (boxesIntersect(witmbox, desiredbox)) {
                        isIntersect = true;
                        break;
                    }

                }

                if (!isIntersect) {
                    curiowitm.item.wdgmsg("take", curiowitm.c);
                    studyinvwdg.drop(Coord.z, new Coord(x, y));
                    return true;
                }

            }
        }

        return false;

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

    public MapObject[] getMapObjects(String name) {

        List<MapObject> mapObjects = new ArrayList<MapObject>();

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                Resource res = gob.getres();
                if (res != null) {
                    if (res.basename().equals(name)) {
                        mapObjects.add(new MapObject(res.basename(), gob.id, gob.rc));
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
                    mapObjects.add(new MapObject(res.basename(), gob.id, gob.rc));
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

            gui.map.wdgmsg("itemact", Coord.z, new Coord(x, y), 0);

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
        witm.item.wdgmsg("take", witm.c);

        return false;

    }

    public void waitForPf() {

        try {
            gameui().map.pfthread.join();
        } catch (InterruptedException e) {
            // ignored
        }

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

    public class MapObject {

        public MapObject(String name, long id, Coord coords) {
            this.name = name;
            this.id = id;
            this.coords = coords;
        }

        public String name;
        public long id;
        public Coord coords;

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

}
