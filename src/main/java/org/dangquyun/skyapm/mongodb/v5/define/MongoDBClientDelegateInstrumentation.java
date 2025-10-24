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

package org.dangquyun.skyapm.mongodb.v5.define;

import static org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.*;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.*;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.dangquyun.skyapm.mongodb.v5.interceptor.MongoDBClientDelegateInterceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Enhance {@code com.mongodb.client.internal.MongoClientDelegate} instance, and intercept {@code
 * com.mongodb.client.internal.MongoClientDelegate#getOperationExecutor()}, this is the only way to get
 * OperationExecutor which is unified entrance of execute mongo command. Inject the remotePeer into enhanced OperationExecutor.
 * <p>
 * support: 4.0.0 or higher
 *
 * @see MongoDBOperationExecutorInstrumentation
 * @see MongoDBClientDelegateInterceptor
 */
public class MongoDBClientDelegateInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String WITNESS_CLASS = "com.mongodb.client.internal.MongoClusterImpl";

    private static final String ENHANCE_CLASS = "com.mongodb.client.internal.MongoClusterImpl";

    private static final String INTERCEPTOR_CLASS = "org.dangquyun.skyapm.mongodb.v5.interceptor.MongoDBClientDelegateInterceptor";

    @Override
    protected String[] witnessClasses() {
        return new String[] {WITNESS_CLASS};
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[] {
            new ConstructorInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getConstructorMatcher() {
                    return takesArgumentWithType(1, "com.mongodb.internal.connection.Cluster");
                }

                @Override
                public String getConstructorInterceptor() {
                    return INTERCEPTOR_CLASS;
                }
            }
        };
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[0];
    }

}
