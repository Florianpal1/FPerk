package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.objects.gui.Action;
import fr.florianpal.fperk.objects.gui.Barrier;

import java.util.ArrayList;
import java.util.List;

public class AbstractGuiConfiguration {

    protected List<Barrier> barrierBlocks = new ArrayList<>();
    protected List<Barrier> previousBlocks = new ArrayList<>();
    protected List<Barrier> nextBlocks = new ArrayList<>();
    protected List<Barrier> closeBlocks = new ArrayList<>();

    protected List<Action> actionBlocks = new ArrayList<>();

    protected int size = 27;
    protected String nameGui = "";

    public List<Barrier> getBarrierBlocks() {
        return barrierBlocks;
    }

    public List<Barrier> getPreviousBlocks() {
        return previousBlocks;
    }

    public List<Barrier> getNextBlocks() {
        return nextBlocks;
    }

    public List<Barrier> getCloseBlocks() {
        return closeBlocks;
    }

    public int getSize() {
        return size;
    }

    public String getNameGui() {
        return nameGui;
    }

    public List<Action> getActionBlocks() {
        return actionBlocks;
    }
}
