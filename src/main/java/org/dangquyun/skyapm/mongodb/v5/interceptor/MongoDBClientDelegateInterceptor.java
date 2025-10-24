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
 *
 */

package org.dangquyun.skyapm.mongodb.v5.interceptor;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.dangquyun.skyapm.mongodb.v5.support.MongoRemotePeerHelper;

import com.mongodb.internal.connection.Cluster;

public class MongoDBClientDelegateInterceptor implements InstanceConstructorInterceptor, InstanceMethodsAroundInterceptor {

    private static final ILog LOGGER = LogManager.getLogger(MongoDBClientDelegateInterceptor.class);
    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
         
        Cluster cluster = (Cluster) allArguments[1];
        String remotePeer = MongoRemotePeerHelper.getRemotePeer(cluster);
        // objInst.setSkyWalkingDynamicField(remotePeer);
        if(LOGGER.isDebugEnable()) {
            LOGGER.debug("MongoDBClientDelegateInterceptor onConstruct remotePeer: {}", remotePeer);    
        }
        try {
            Class<?> mongoClusterImplClass = Class.forName("com.mongodb.client.internal.MongoClusterImpl");

            // 获取目标方法
            Method getOperationExecutorMethod = mongoClusterImplClass.getDeclaredMethod("getOperationExecutor");
            getOperationExecutorMethod.setAccessible(true);

            // 调用目标实例的方法
            Object operationExecutorObj = getOperationExecutorMethod.invoke(objInst); 
            LOGGER.debug("operationExecutorObj class: {}", operationExecutorObj.getClass().getName()); 
            if (operationExecutorObj instanceof EnhancedInstance) {
                // pass remotePeer to OperationExecutor, which has be enhanced as EnhancedInstance
                // See: org.apache.skywalking.apm.plugin.mongodb.v3.define.v37.MongoDBOperationExecutorInstrumentation
                EnhancedInstance operationExecutorInstance = (EnhancedInstance) operationExecutorObj;
                operationExecutorInstance.setSkyWalkingDynamicField(remotePeer);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to get OperationExecutor from MongoClusterImpl", e);
        } 
    }

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) {
        // do nothing
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        Object ret) {
        // LOGGER.debug("OperationExecutor afterMethod called");    
        // OperationExecutor executor = (OperationExecutor) allArguments[allArguments.length - 1];
        // if (executor instanceof EnhancedInstance) {
        //     // pass remotePeer to OperationExecutor, which has be enhanced as EnhancedInstance
        //     // See: org.apache.skywalking.apm.plugin.mongodb.v3.define.v37.MongoDBOperationExecutorInstrumentation
        //     EnhancedInstance retInstance = (EnhancedInstance) ret;
        //     String remotePeer = (String) objInst.getSkyWalkingDynamicField();
        //     if (LOGGER.isDebugEnable()) {
        //         LOGGER.debug("Mark OperationExecutor remotePeer: {}", remotePeer);
        //     }
        //     retInstance.setSkyWalkingDynamicField(remotePeer);
        // }
        // return ret;
        // do nothing
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
        Class<?>[] argumentsTypes, Throwable t) {
        // do nothing
    }
}
