package haven;

import java.util.*;

public class Game extends Widget {

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

    private class Box {

        public Box(Coord topleft, Coord bottomright) {
            this.topleft = topleft;
            this.bottomright = bottomright;
        }

        public Coord topleft;
        public Coord bottomright;

    }

    private boolean boxesIntersect(Box lhs, Box rhs) {

        boolean intersect =
               lhs.topleft.x < rhs.bottomright.x
            && lhs.bottomright.x > rhs.topleft.x
            && lhs.topleft.y < rhs.bottomright.y
            && lhs.bottomright.y > rhs.topleft.y;
        return intersect;

    }

    public String[] getInvItems() {

        List<String> invitems = new ArrayList<String>();

        Inventory charinvwdg = getCharInventoryWidget();
        if (charinvwdg == null) {
            return null;
        }

        List<WItem> charinvwitems = getInventoryWItems(charinvwdg);
        for (WItem witm : charinvwitems) {
            String witmbasename = getItemBaseName(witm);
            invitems.add(witmbasename);
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
        gui.map.pfRightClick(gob, -1, 3, 0, null);

    }

    public boolean pickItem(long id) {

        Config.autopick = true;

        return mapObjectRightClick(id);

    }

    public void travelToHearthFire() {

        gameui().menu.wdgmsg("act", new Object[]{"travel", "hearth"});

    }

    public class MapObject {

        public MapObject(long id, Coord coords) {
            this.id = id;
            this.coords = coords;
        }

        public long id;
        public Coord coords;

    }

    public MapObject[] getMapObjects(String name) {

        List<MapObject> mapObjects = new ArrayList<MapObject>();

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                Resource res = gob.getres();
                if (res != null) {
                    if (res.basename().equals(name)) {
                        mapObjects.add(new MapObject(gob.id, gob.rc));
                    }
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

    public boolean transferItem(String itemBaseName) {

        WItem witm = getCharInventoryWItem(itemBaseName);
        if (witm == null) {
            return false;
        }

        witm.item.wdgmsg("transfer", Coord.z);

        return true;

    }

    public boolean transferItems(String itemsBaseName) {

        WItem witm = getCharInventoryWItem(itemsBaseName);
        if (witm == null) {
            return false;
        }

        witm.item.wdgmsg("transfer-identical", Coord.z);

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

    private Inventory getCharInventoryWidget() {

        Window invwnd = gameui().getwnd("Inventory");
        for (Widget invwdg = invwnd.lchild; invwdg != null; invwdg = invwdg.prev) {
            if (invwdg instanceof Inventory) {
                return (Inventory) invwdg;
            }
        }

        return null;

    }

    private Inventory getStudyInventoryWidget() {

        Window charsheet = gameui().getwnd("Character Sheet");
        for (Widget firstlvlwdg = charsheet.lchild; firstlvlwdg != null; firstlvlwdg = firstlvlwdg.prev) {
            for (Widget secondlvlwdg = firstlvlwdg.lchild; secondlvlwdg != null; secondlvlwdg = secondlvlwdg.prev) {
                if (secondlvlwdg instanceof Inventory && secondlvlwdg.parent instanceof Tabs.Tab) {
                    return (Inventory) secondlvlwdg;
                }
            }
        }

        return null;

    }

    public class AttentionInfo {

        public AttentionInfo(int max, int used) {
            this.max = max;
            this.used = used;
        }

        public int max;
        public int used;

    }

    private CharWnd.StudyInfo getStudyInfoWdg() {

        Window charsheet = gameui().getwnd("Character Sheet");
        for (Widget firstlvlwdg = charsheet.lchild; firstlvlwdg != null; firstlvlwdg = firstlvlwdg.prev) {
            for (Widget secondlvlwdg = firstlvlwdg.lchild; secondlvlwdg != null; secondlvlwdg = secondlvlwdg.prev) {
                if (secondlvlwdg instanceof CharWnd.StudyInfo && secondlvlwdg.parent instanceof Tabs.Tab) {
                    return (CharWnd.StudyInfo) secondlvlwdg;
                }
            }
        }

        return null;

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

                Resource res = ((WItem) witm).item.getres();
                if (res != null) {
                    if (res.basename().equals(itemsbasename)) {
                        witems.add((WItem) witm);
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
                String witmbasename = getItemBaseName(item);
                if (witmbasename.equals(itemBaseName)) {
                    return item;
                }

            }
        }

        return null;

    }

    private String getItemBaseName(WItem witm) {

        GItem ngitm = witm.item;
        Resource nres = ngitm.resource();
        if (nres == null) {
            return null;
        }
        return nres.basename();

    }

}
