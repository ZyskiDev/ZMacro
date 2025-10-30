package net.zyski.zmacro.client.api;

public class ZAPI {

    private final ZInventory zInventory = new ZInventory();
    private final ZConnection zConnection = new ZConnection();

    public ZInventory getInventory() {
        return zInventory;
    }
    public ZConnection getzConnection() {
        return zConnection;
    }

}
