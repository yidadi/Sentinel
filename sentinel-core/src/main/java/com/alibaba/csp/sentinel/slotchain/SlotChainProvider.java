/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slotchain;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;
import java.util.ServiceLoader;

/**
 * A provider for creating slot chains via resolved slot chain builder SPI.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class SlotChainProvider {

    private static volatile SlotChainBuilder slotChainBuilder = null;

    private static final ServiceLoader<SlotChainBuilder> LOADER = ServiceLoader.load(SlotChainBuilder.class);

    /**
     * The load and pick process is not thread-safe, but it's okay since the method should be only invoked
     * via {@code lookProcessChain} in {@link com.alibaba.csp.sentinel.CtSph} under lock.
     *
     * @return new created slot chain
     */
    public static ProcessorSlotChain newSlotChain() {
        if (slotChainBuilder != null) {
            return slotChainBuilder.build();
        }

        resolveSlotChainBuilder();

        return slotChainBuilder.build();
    }

    private static void resolveSlotChainBuilder() {
        for (SlotChainBuilder builder : LOADER) {
            if (builder.getClass() != DefaultSlotChainBuilder.class) {
                slotChainBuilder = builder;
                break;
            }
        }
        if (slotChainBuilder == null){
            // No custom builder, using default.
            slotChainBuilder = new DefaultSlotChainBuilder();
        }

        RecordLog.info("[SlotChainProvider] Global slot chain builder resolved: "
            + slotChainBuilder.getClass().getCanonicalName());
    }

    private SlotChainProvider() {}
}
