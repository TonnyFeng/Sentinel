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

import com.alibaba.csp.sentinel.context.Context;

/**
 * @author qinan.qn
 * @author jialiang.linjl
 */
public abstract class AbstractLinkedProcessorSlot<T> implements ProcessorSlot<T> {

    private AbstractLinkedProcessorSlot<?> next = null;

    @Override
    /**
     * 处理资源入口的触发方法
     * @param context 上下文信息，用于传递当前执行环境的上下文
     * @param resourceWrapper 资源包装器，包含被访问的资源信息
     * @param obj 调用参数，可以是任意类型的对象
     * @param count 访问计数，记录资源被访问的次数
     * @param prioritized 是否优先处理的标志位
     * @param args 可变参数列表，用于传递额外的参数
     * @throws Throwable 可能抛出的异常
     */
    public void fireEntry(Context context, ResourceWrapper resourceWrapper, Object obj, int count, boolean prioritized, Object... args)
        throws Throwable {
        // 如果存在下一个处理器，则调用其transformEntry方法进行转换处理
        if (next != null) {
            next.transformEntry(context, resourceWrapper, obj, count, prioritized, args);
        }
    }

    @SuppressWarnings("unchecked") // 抑制编译器 unchecked 转换警告
    /**
     * 转换资源条目的方法
     * @param context 上下文信息
     * @param resourceWrapper 资源包装器
     * @param o 要转换的对象
     * @param count 计数
     * @param prioritized 是否优先处理
     * @param args 可变参数
     * @throws Throwable 可能抛出的异常
     */
    void transformEntry(Context context, ResourceWrapper resourceWrapper, Object o, int count, boolean prioritized, Object... args)
        throws Throwable {
        T t = (T)o; // 将对象强制转换为泛型类型T
        // 调用抽象方法entry处理资源条目
        entry(context, resourceWrapper, t, count, prioritized, args); // 调用entry方法处理转换后的对象
    }

    @Override
    public void fireExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        if (next != null) {
            next.exit(context, resourceWrapper, count, args);
        }
    }

    public AbstractLinkedProcessorSlot<?> getNext() {
        return next;
    }

    public void setNext(AbstractLinkedProcessorSlot<?> next) {
        this.next = next;
    }

}
