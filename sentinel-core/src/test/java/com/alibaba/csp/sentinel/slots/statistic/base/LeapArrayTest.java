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
package com.alibaba.csp.sentinel.slots.statistic.base;

import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertSame;

/**
 * @author Eric Zhao
 */
public class LeapArrayTest extends AbstractTimeBasedTest {
    
    @Test
    public void testGetValidHead() {
        //单个窗口时间长度
        int windowLengthInMs = 100;
        //此{@link LeapArray}的总时间间隔（毫秒）
        int intervalInMs = 1000;
        //滑动窗口的桶计数
        int sampleCount = intervalInMs / windowLengthInMs;
        LeapArray<AtomicInteger> leapArray = new LeapArray<AtomicInteger>(sampleCount, intervalInMs) {
            @Override
            public AtomicInteger newEmptyBucket(long time) {
                return new AtomicInteger(0);
            }

            /**
             * 将给定铲斗重置为提供的开始时间，并重置该值。
             * 参数：
             * windowWrap–当前存储桶
             * startTime–存储桶的开始时间（以毫秒为单位）
             * 返回： 给定开始时间的新清洁铲斗 sentinel-core
             * @return
             */
            @Override
            protected WindowWrap<AtomicInteger> resetWindowTo(WindowWrap<AtomicInteger> windowWrap, long startTime) {
                windowWrap.resetTo(startTime);
                windowWrap.value().set(0);
                return windowWrap;
            }
        };
        
        WindowWrap<AtomicInteger> expected1 = leapArray.currentWindow();
        expected1.value().addAndGet(1);
        sleep(windowLengthInMs);
        WindowWrap<AtomicInteger> expected2 = leapArray.currentWindow();
        expected2.value().addAndGet(2);
        for (int i = 0; i < sampleCount - 2; i++) {
            sleep(windowLengthInMs);
            leapArray.currentWindow().value().addAndGet(i + 3);
        }

        assertSame(expected1, leapArray.getValidHead());
        sleep(windowLengthInMs);
        assertSame(expected2, leapArray.getValidHead());
    }

}