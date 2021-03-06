package org.sample.simplecqrs.query.serviceimpl;

import org.fornax.cartridges.sculptor.framework.event.DynamicMethodDispatcher;
import org.fornax.cartridges.sculptor.framework.event.Event;
import org.sample.simplecqrs.command.domain.InventoryItemCreated;
import org.sample.simplecqrs.command.domain.InventoryItemDeactivated;
import org.sample.simplecqrs.command.domain.InventoryItemRenamed;
import org.sample.simplecqrs.command.domain.ItemsCheckedInToInventory;
import org.sample.simplecqrs.command.domain.ItemsRemovedFromInventory;
import org.sample.simplecqrs.query.domain.InventoryItemDetails;
import org.sample.simplecqrs.query.exception.InventoryItemDetailsNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of InventoryItemDetailView.
 */
@Service("inventoryItemDetailView")
public class InventoryItemDetailViewImpl extends InventoryItemDetailViewImplBase {
    public InventoryItemDetailViewImpl() {
    }

    public void receive(Event event) {
        DynamicMethodDispatcher.dispatch(this, event, "handle");
    }

    public void handle(InventoryItemCreated event) {
        getInventoryItemDetailsRepository().save(new InventoryItemDetails(event.getItemId(), event.getName()));
    }

    public void handle(InventoryItemRenamed event) {
        InventoryItemDetails item = tryGetItem(event.getItemId());
        item.setName(event.getNewName());
        getInventoryItemDetailsRepository().save(item);
    }

    public void handle(InventoryItemDeactivated event) {
        InventoryItemDetails item = tryGetItem(event.getItemId());
        getInventoryItemDetailsRepository().delete(item);
    }

    public void handle(ItemsCheckedInToInventory event) {
        InventoryItemDetails item = tryGetItem(event.getItemId());
        item.setCurrentCount(item.getCurrentCount() + event.getCountChange());
        getInventoryItemDetailsRepository().save(item);
    }

    public void handle(ItemsRemovedFromInventory event) {
        InventoryItemDetails item = tryGetItem(event.getItemId());
        item.setCurrentCount(item.getCurrentCount() - event.getCountChange());
        getInventoryItemDetailsRepository().save(item);
    }

    private InventoryItemDetails tryGetItem(String itemId) {
        try {
            return getInventoryItemDetailsRepository().findByKey(itemId);
        } catch (InventoryItemDetailsNotFoundException e) {
            throw new IllegalStateException("Unknown item: " + itemId);
        }
    }
}
