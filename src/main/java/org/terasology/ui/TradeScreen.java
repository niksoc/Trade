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
package org.terasology.ui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.SellerComponent;
import org.terasology.components.TradeComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.events.TradeInitiatedEvent;
import org.terasology.events.TransactionRequestEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
@RegisterSystem
public class TradeScreen extends BaseInteractionScreen {
    private static final Logger logger = LoggerFactory.getLogger(TradeScreen.class);

    private UIList<ItemSelectionInfo> itemList;
    private UIButton cancelButton;
    private UIButton saveButton;

    private EntityRef seller;
    private SellerComponent sellerComponent;
    private int availableMoney;
    private List<ItemSelectionInfo> itemSelectionInfoList = new ArrayList<>();

    @In
    private LocalPlayer localPlayer;
    @In
    private NUIManager nuiManager;

    @Override
    public void initialise() {
        itemList = find("itemList", UIList.class);
        itemList.setItemRenderer(new AbstractItemRenderer<ItemSelectionInfo>() {
            public String getString(ItemSelectionInfo value) {
                return value.item.getKey().getParentPrefab().getName()
                        + " : " + value.item.getValue() +"$";
            }

            @Override
            public void draw(ItemSelectionInfo value, Canvas canvas) {
                if (value.selected) {
                    canvas.setMode("enabled");
                } else if (value.item.getValue() < availableMoney) {
                    canvas.setMode("available");
                } else {
                    canvas.setMode("disabled");
                }
                canvas.drawText(getString(value), canvas.getRegion());
            }

            @Override
            public Vector2i getPreferredSize(ItemSelectionInfo value, Canvas canvas) {
                String text = getString(value);
                return new Vector2i(canvas.getCurrentStyle().getFont().getWidth(text), canvas.getCurrentStyle().getFont().getLineHeight());
            }
        });

        itemList.subscribe(this::onItemSelect);

        cancelButton = find("cancelButton", UIButton.class);
        saveButton = find("buyButton", UIButton.class);
        if (saveButton != null) {
            saveButton.subscribe(this::onBuyButton);
        }

        if (cancelButton != null) {
            cancelButton.subscribe(this::onCancelButton);
        }
    }

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        if (!localPlayer.getCharacterEntity().hasComponent(TradeComponent.class)) {
            localPlayer.getCharacterEntity().addComponent(new TradeComponent());
        }

        availableMoney = localPlayer.getCharacterEntity().getComponent(TradeComponent.class).money;

        seller = interactionTarget;
        sellerComponent = interactionTarget.getComponent(SellerComponent.class);

        TradeInitiatedEvent tradeInitiatedEvent = new TradeInitiatedEvent(seller);
        localPlayer.getCharacterEntity().send(tradeInitiatedEvent);

        itemSelectionInfoList.clear();

        for (Map.Entry<EntityRef, Integer> itemAndPrice : tradeInitiatedEvent.catalogue.entrySet()) {
            ItemSelectionInfo itemSelectionInfo = new ItemSelectionInfo();
            itemSelectionInfo.item = itemAndPrice;
            itemSelectionInfoList.add(itemSelectionInfo);
        }

        itemList.setList(itemSelectionInfoList);
    }

    private void onItemSelect(UIWidget list, ItemSelectionInfo item) {
        if (item.selected) {
            item.selected = false;
        } else if (item.item.getValue() < availableMoney) {
            item.selected = true;
        }
    }

    private void onBuyButton(UIWidget button) {
        for(ItemSelectionInfo itemSelectionInfo : itemSelectionInfoList) {
            if(itemSelectionInfo.selected) {
                localPlayer.getCharacterEntity().send(new TransactionRequestEvent(seller,
                        itemSelectionInfo.item.getKey(), itemSelectionInfo.item.getValue()));
            }
        }
        getManager().popScreen();
    }

    private void onCancelButton(UIWidget button) {
        getManager().popScreen();
    }

    private static final class ItemSelectionInfo {
        Map.Entry<EntityRef, Integer> item;
        boolean selected = false;
    }


}