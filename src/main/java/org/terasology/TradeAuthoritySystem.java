/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology;

import org.terasology.components.SellerComponent;
import org.terasology.components.TradeComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.events.PriceDeterminerEvent;
import org.terasology.events.TradeInitiatedEvent;
import org.terasology.events.TransactionEvent;
import org.terasology.events.TransactionRequestEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TradeAuthoritySystem extends BaseComponentSystem {
    private static final int UNLIMITED_MONEY = -1;

    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;

    @ReceiveEvent(components = {TradeComponent.class})
    public void onTradeInitiated(TradeInitiatedEvent event, EntityRef buyer) {
        SellerComponent sellerComponent = event.seller.getComponent(SellerComponent.class);
        for(Prefab itemPrefab : sellerComponent.itemsOnSale) {
            EntityRef item = entityManager.create(itemPrefab);
            PriceDeterminerEvent priceDeterminerEvent = new PriceDeterminerEvent(item, buyer);
            event.seller.send(priceDeterminerEvent);
            event.catalogue.put(item, priceDeterminerEvent.getPrice());
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void onTransactionRequest(TransactionRequestEvent event, EntityRef buyer, TradeComponent tradeComponent) {
        int buyerMoney = tradeComponent.money;
        if(buyerMoney < event.price) {
            event.consume();
            return;
        }
        buyer.send(new TransactionEvent(event));
    }

    @ReceiveEvent
    public void onTransaction(TransactionEvent event, EntityRef buyer) {
        TradeComponent tradeComponent = buyer.getComponent(TradeComponent.class);
        tradeComponent.money -= event.price;
        buyer.saveComponent(tradeComponent);

        event.item.send(new GiveItemEvent(buyer));
    }
}
