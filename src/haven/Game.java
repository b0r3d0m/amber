package haven;

import java.util.*;

public class Game extends Widget {

    public boolean studyCurio(String curioName) {

        Inventory charinvwdg = getCharInventoryWidget();
        Inventory studyinvwdg = getStudyInventoryWidget();
        if (charinvwdg == null || studyinvwdg == null) {
            return false;
        }

        WItem curiowitm = getInventoryWItem(charinvwdg, curioName);
        if (curiowitm == null) {
            return false;
        }

        WItem studywitem = getInventoryWItem(studyinvwdg, curioName);
        if (studywitem != null) {
            return false;
        }

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

        Coord topleft;
        Coord bottomright;

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

    public boolean pickItem(long id) {

        Config.autopick = true;

        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {

                if (gob.id == id) {

                    GameUI gui = gameui();
                    gui.map.pfRightClick(gob, -1, 3, 0, null);
                    return true;

                }

            }
        }

        return false;

    }

    public void travelToHearthFire() {

        gameui().menu.wdgmsg("act", new Object[]{"travel", "hearth"});

    }

    public void quit() {

        System.exit(0);

    }

    public void logout() {

        GameUI gui = gameui();
        gui.act("lo");
        if (gui != null & gui.map != null)
            gui.map.canceltasks();

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

    private List<WItem> getInventoryWItems(Inventory invwdg) {

        List<WItem> witems = new ArrayList<WItem>();

        for (Widget witm = invwdg.lchild; witm != null; witm = witm.prev) {
            if (witm instanceof WItem) {
                witems.add((WItem) witm);
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
