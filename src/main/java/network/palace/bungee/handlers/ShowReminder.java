package network.palace.bungee.handlers;

import network.palace.bungee.PalaceBungee;

/**
 * Created by Marc on 1/15/17.
 */
public class ShowReminder implements Runnable {
    private String msg = "";

    public ShowReminder(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            PalaceBungee.getMessageHandler().sendStaffMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
