package solid.humank.coffeeshop.inventories.applicationservices;

import solid.humank.coffeeshop.coffee.datacontracts.messages.MakeCoffeeMsg;

public class ConfirmInventorySvc {
    public boolean notAvailableFor(MakeCoffeeMsg request) {

				//TODO actually call Inventory Web api, by JAX-RS package call
        return false;
    }
}
