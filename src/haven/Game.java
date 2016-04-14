package haven;

public class Game extends Widget {

    public boolean studyCurio(String curioName) {

        Window invwnd = gameui().getwnd("Inventory");

        Window charsheet = gameui().getwnd("Character Sheet");
        Inventory studyinv = null;
        for (Widget firstWdg = charsheet.lchild; firstWdg != null; firstWdg = firstWdg.prev) {
            for (Widget secondWdg = firstWdg.lchild; secondWdg != null; secondWdg = secondWdg.prev) {
                if (secondWdg instanceof Inventory && secondWdg.parent instanceof Tabs.Tab) {
                    studyinv = (Inventory) secondWdg;
                }
            }
        }

        try {
            for (Widget invwdg = invwnd.lchild; invwdg != null; invwdg = invwdg.prev) {
                if (invwdg instanceof Inventory) {
                    Inventory inv = (Inventory) invwdg;
                    for (Widget witm = inv.lchild; witm != null; witm = witm.prev) {
                        if (witm instanceof WItem) {
                            GItem ngitm = ((WItem) witm).item;
                            Resource nres = ngitm.resource();
                            if (nres != null && nres.basename().equals(curioName)) {
                                ngitm.wdgmsg("take", witm.c);
                                studyinv.drop(Coord.z, new Coord(0, 0));
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
        } catch (Exception e) { // ignored
        }

        return false;

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

}
