/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.remoting;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.alipay.remoting.log.BoltLoggerFactory;

/**
 * <pre>
 * 通过command code维护command和command处理器之间的关系。
 * </pre>
 * 
 * Manager of processors<br>
 * Maintains the relationship between command and command processor through command code.
 * 
 * @author jiangping
 * @version $Id: ProcessorManager.java, v 0.1 Sept 6, 2015 2:49:47 PM tao Exp $
 */
public class ProcessorManager {
    private static final Logger                                  logger         = BoltLoggerFactory.getLogger("CommonDefault");
    
    // 命令码与处理器的映射
    private ConcurrentHashMap<CommandCode, RemotingProcessor<?>> cmd2processors = new ConcurrentHashMap<CommandCode, RemotingProcessor<?>>(4);

    // 默认处理器
    private RemotingProcessor<?>                                 defaultProcessor;

    /** The default executor, if no executor is set for processor, this one will be used */
    private ExecutorService                                      defaultExecutor;

    private int                                                  minPoolSize    = SystemProperties.default_tp_min_size();

    private int                                                  maxPoolSize    = SystemProperties.default_tp_max_size();

    private int                                                  queueSize      = SystemProperties.default_tp_queue_size();

    private long                                                 keepAliveTime  = SystemProperties.default_tp_keepalive_time();

    public ProcessorManager() {
    	// 默认处理线程池
        defaultExecutor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize), new NamedThreadFactory("Bolt-default-executor", true));
    }

    /**
     * Register processor to process command that has the command code of cmdCode.
     * 
     * @param cmdCode
     * @param processor
     */
    public void registerProcessor(CommandCode cmdCode, RemotingProcessor<?> processor) {
        if (this.cmd2processors.contains(cmdCode)) {
            logger.warn("Processor for cmd={} is already registered, the processor is {}, and changed to {}",
                    cmdCode, cmd2processors.get(cmdCode).getClass().getName(), processor.getClass().getName());
        }
        this.cmd2processors.put(cmdCode, processor);
    }

    /**
     * Register the default processor to process command with no specific processor registered.
     * 
     * @param processor
     */
    public void registerDefaultProcessor(RemotingProcessor<?> processor) {
        if (this.defaultProcessor == null) {
            this.defaultProcessor = processor;
        } else {
            throw new IllegalStateException("The defaultProcessor has already been registered: " + this.defaultProcessor.getClass());
        }
    }

    /**
     * Get the specific processor with command code of cmdCode if registered, otherwise the default processor is returned.
     * 
     * @param cmdCode
     * @return
     */
    public RemotingProcessor<?> getProcessor(CommandCode cmdCode) {
        RemotingProcessor<?> processor = this.cmd2processors.get(cmdCode);
        if (processor != null) {
            return processor;
        }
        return this.defaultProcessor;
    }

    /**
     * Getter method for property <tt>defaultExecutor</tt>.
     * 
     * @return property value of defaultExecutor
     */
    public ExecutorService getDefaultExecutor() {
        return defaultExecutor;
    }

    /**
     * Set the default executor.
     * 
     * @param executor
     */
    public void registerDefaultExecutor(ExecutorService executor) {
        this.defaultExecutor = executor;
    }

}