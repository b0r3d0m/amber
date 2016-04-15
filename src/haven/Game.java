package haven;

import java.util.*;

public class Game extends Widget {

    public boolean studyCurio(String curioName) {

        Inventory charinvwdg = getCharInventoryWidget();
        Inventory studyinvwdg = getStudyInventoryWidget();
        if (charinvwdg == null || studyinvwdg == null) {
            return false;
        }

        List<WItem> charinvwitems = getInventoryWItems(charinvwdg);
        for (WItem witm : charinvwitems) {
            String witmbasename = getItemBaseName(witm);
            if (witmbasename.equals(curioName)) {
                witm.item.wdgmsg("take", witm.c);
                // TODO: Drop an item to the empty space, not to the top-left cell
                // TODO: Handle "not enough study points" situation
                studyinvwdg.drop(Coord.z, new Coord(0, 0));
                return true;
            }
        }

        return false;

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

    private String getItemBaseName(WItem witm) {

        GItem ngitm = witm.item;
        Resource nres = ngitm.resource();
        if (nres == null) {
            return null;
        }
        return nres.basename();

    }

}
