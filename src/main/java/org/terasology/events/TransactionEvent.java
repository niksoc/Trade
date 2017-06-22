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
package org.terasology.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

public class TransactionEvent implements Event {
    public final EntityRef seller;
    public final EntityRef item;
    public final int price;

    public TransactionEvent(TransactionRequestEvent event) {
        this.seller = event.seller;
        this.item = event.item;
        this.price = event.price;
    }

    public TransactionEvent(EntityRef seller, EntityRef item, int price) {
        this.seller = seller;
        this.item = item;
        this.price = price;
    }
}
