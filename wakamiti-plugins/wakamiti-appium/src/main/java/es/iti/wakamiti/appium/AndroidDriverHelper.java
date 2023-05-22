package es.iti.wakamiti.appium;


import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.GsmCallActions;
import org.slf4j.Logger;

public class AndroidDriverHelper extends DriverHelper{

    private final AndroidDriver androidDriver;

    public AndroidDriverHelper(AndroidDriver driver, Logger logger) {
        super(driver, logger);
        this.androidDriver = driver;
    }

    @Override
    public void cancelCall(String currentCall) {
        androidDriver.makeGsmCall(currentCall, GsmCallActions.CANCEL);
    }


    @Override
    public void receiveCall(String number) {
        androidDriver.makeGsmCall(number, GsmCallActions.CALL);
    }


    @Override
    public void acceptIncomingCall(String number) {
        androidDriver.makeGsmCall(number, GsmCallActions.ACCEPT);
    }


    @Override
    public void rejectIncomingCall(String number) {
        androidDriver.makeGsmCall(number, GsmCallActions.CANCEL);
    }


    @Override
    public boolean isIncomingCall() {
        executeShellCommand("dumpsys telephony.registry");
        return false;
    }

}
