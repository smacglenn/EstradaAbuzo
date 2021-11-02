package EEventBus;

import common.Common;

public class ToastEvent {
    private Common.ACTION action;
    private boolean isFromFoodlist;

    public ToastEvent(Common.ACTION action, boolean isFromFoodlist) {
        this.action = action;
        this.isFromFoodlist = isFromFoodlist;
    }

    public Common.ACTION getAction() {
        return action;
    }

    public void setAction(Common.ACTION action) {
        this.action = action;
    }

    public boolean isFromFoodlist() {
        return isFromFoodlist;
    }

    public void setFromFoodlist(boolean fromFoodlist) {
        isFromFoodlist = fromFoodlist;
    }
}
